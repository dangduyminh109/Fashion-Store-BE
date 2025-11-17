package com.fashion_store.service;

import com.fashion_store.Utils.DbSchemaConfig;
import com.fashion_store.Utils.SecurityUtils;
import com.fashion_store.dto.chat.request.ChatRequest;
import com.fashion_store.dto.chat.request.ConversationHistory;
import com.fashion_store.dto.product.response.ProductVariantResponse;
import com.fashion_store.entity.Conversation;
import com.fashion_store.entity.Customer;
import com.fashion_store.enums.ChatRole;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.ConversationMapper;
import com.fashion_store.repository.ConversationRepository;
import com.fashion_store.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fashion_store.Utils.SqlValidator;
import com.fashion_store.dto.chat.response.ChatResponse;
import com.fashion_store.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatService {
    final SqlValidator sqlValidator;
    final DataSource dataSource;
    final ProductRepository productRepository;
    final ConversationRepository conversationRepository;
    final ConversationMapper conversationMapper;

    @Value("${gemini.api.key:}")
    String geminiApiKey;

    @Value("${gemini.endpoint}")
    String geminiEndpoint;

    final ObjectMapper objectMapper = new ObjectMapper();
    final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    final int defaultLimit = 5;
    private final CustomerRepository customerRepository;

    public List<ConversationHistory> getHistory() {
        List<ConversationHistory> conversationHistory = Collections.emptyList();
        String customerId = SecurityUtils.getCurrentUserId();
        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
            List<Conversation> conversations = conversationRepository.findTop15ByCustomerIdOrderByCreatedAtDesc(customerId);
            conversationHistory = conversationMapper.toConversationHistory(conversations);
        }
        return conversationHistory;
    }

    public ChatResponse handleMessage(ChatRequest chatRequest) {
        try {
            // load lịch sửa hội thoại
            List<ConversationHistory> conversationHistory = chatRequest.getConversationHistory();
            String customerId = SecurityUtils.getCurrentUserId();
            Customer customer = null;
            if (customerId != null && !customerId.equals("anonymousUser")) {
                customer = customerRepository.findById(customerId).orElse(null);
            }

            boolean isProductQuery = isProductRelated(chatRequest.getMessage());
            List<ProductVariantResponse> products = Collections.emptyList();

            if (isProductQuery) {
                String schemaJson = buildSchemaJsonDynamic();
                List<String> examples = loadFewShotExamples();
                String nl2sqlPrompt = buildNL2SQLPrompt(schemaJson, examples, chatRequest.getMessage(), conversationHistory);

                String rawResp = callGemini(nl2sqlPrompt);
                String sql = extractTextFromResponse(rawResp);
                if (sql != null) sql = sql.trim();

                if (sql == null || sql.isEmpty()) {
                    String rawAnswer = generateNaturalAnswer(chatRequest.getMessage(), products, conversationHistory);
                    String naturalAnswer = extractTextFromResponse(rawAnswer);
                    if (customer != null) {
                        conversationRepository.save(Conversation.builder()
                                .role(ChatRole.CUSTOMER)
                                .message(chatRequest.getMessage())
                                .customer(customer)
                                .build());
                        conversationRepository.save(Conversation.builder()
                                .role(ChatRole.ASSISTANT)
                                .message(naturalAnswer)
                                .customer(customer)
                                .build());
                    }
                    return ChatResponse.builder()
                            .answer(naturalAnswer)
                            .build();
                }

                if (!sqlValidator.isSafe(sql)) {
                    return ChatResponse.builder()
                            .answer("Không thể tạo truy vấn an toàn từ câu hỏi của bạn. Hãy hỏi lại chi tiết hơn.")
                            .build();
                }

                List<Map<String, Object>> rows = productRepository.querySql(sql);
                ProductVariantResponse bestProduct = selectClosestProduct(mapRowsToProducts(rows), chatRequest.getMessage());
                products = bestProduct != null ? List.of(bestProduct) : Collections.emptyList();
            }

            String rawAnswer = generateNaturalAnswer(chatRequest.getMessage(), products, conversationHistory);
            String naturalAnswer = extractTextFromResponse(rawAnswer);
            if (customer != null) {
                conversationRepository.save(Conversation.builder()
                        .role(ChatRole.CUSTOMER)
                        .message(chatRequest.getMessage())
                        .customer(customer)
                        .build());
                conversationRepository.save(Conversation.builder()
                        .role(ChatRole.ASSISTANT)
                        .message(naturalAnswer)
                        .customer(customer)
                        .build());
            }

            return ChatResponse.builder()
                    .answer(naturalAnswer)
                    .build();

        } catch (Exception e) {
            return ChatResponse.builder()
                    .answer("Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.")
                    .build();
        }
    }

    boolean isProductRelated(String userMessage) throws Exception {
        if (userMessage == null || userMessage.trim().isEmpty()) return false;
        String msg = userMessage.toLowerCase(Locale.ROOT);

        String[] productKeywords = new String[]{
                "áo", "quần", "giày", "mũ", "váy", "size", "kích cỡ", "size:", "color", "màu", "giá", "price",
                "tìm", "tìm kiếm", "tìm sản phẩm", "sản phẩm", "shop", "bán", "mua", "show", "sản phẩm nào", "còn hàng"
        };
        String[] nonProductKeywords = new String[]{
                "xin chào", "chào", "hi", "hello", "cảm ơn", "thanks", "bạn khỏe", "khỏe", "tạm biệt", "bye"
        };

        for (String k : productKeywords) {
            if (msg.contains(k)) return true;
        }
        for (String k : nonProductKeywords) {
            if (msg.contains(k)) return false;
        }

        String prompt = """
                You are a classifier for a fashion e-commerce assistant (Fashion Store).
                Determine if the user's message is asking about products, searching, filtering, or requesting product details.
                Reply with exactly "YES" if it is product-related, otherwise reply exactly "NO".
                
                Examples:
                User: "Tìm áo size M" -> YES
                User: "Xin chào" -> NO
                User: "Bạn có giảm giá không?" -> NO
                User: "Cho mình các sản phẩm váy giá dưới 700000" -> YES
                
                User: "%s"
                """.formatted(escapeQuotes(userMessage));

        String rawResp = callGemini(prompt);
        String txt = extractTextFromResponse(rawResp).trim().toUpperCase(Locale.ROOT);

        if (txt.startsWith("YES") || txt.contains("YES")) return true;
        if (txt.startsWith("NO") || txt.contains("NO")) return false;

        return false;
    }

    String buildSchemaJsonDynamic() throws Exception {
        Connection conn = dataSource.getConnection();
        DatabaseMetaData meta = conn.getMetaData();

        JSONObject root = new JSONObject();

        ResultSet tables = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME").toLowerCase();

            if (!DbSchemaConfig.ALLOWED_TABLES.contains(tableName)) continue;

            JSONArray columnsJson = new JSONArray();
            ResultSet columns = meta.getColumns(conn.getCatalog(), null, tableName, "%");

            while (columns.next()) {
                columnsJson.put(columns.getString("COLUMN_NAME").toLowerCase());
            }

            root.put(tableName, columnsJson);
        }

        return root.toString(2);
    }

    List<String> loadFewShotExamples() {
        List<String> examples = new ArrayList<>();

        examples.add("""
                User: "Tìm áo size X"
                SQL:
                SELECT p.name, p.description, p.slug, b.name AS brand, \
                GROUP_CONCAT(img.url SEPARATOR ',') AS images, c.name AS category, \
                CASE WHEN v.promotional_price IS NOT NULL AND NOW() BETWEEN v.promotion_start_time AND v.promotion_end_time \
                THEN v.promotional_price ELSE v.sale_price END AS price \
                FROM products p \
                JOIN variants v ON v.product_id = p.id \
                JOIN variant_attribute_value vav ON vav.variant_id = v.id \
                JOIN attribute_values av ON av.id = vav.attribute_value_id \
                JOIN attributes a ON a.id = av.attribute_id \
                JOIN product_images img ON p.id = img.product_id \
                JOIN categories c ON c.id = p.category_id \
                JOIN brands b ON b.id = p.brand_id \
                WHERE p.status = true AND p.is_deleted = false \
                AND EXISTS ( \
                SELECT 1 FROM variant_attribute_value vav2 \
                JOIN attribute_values av2 ON av2.id = vav2.attribute_value_id \
                JOIN attributes a2 ON a2.id = av2.attribute_id \
                WHERE vav2.variant_id = v.id AND LOWER(a2.name) = LOWER('size') AND LOWER(av2.value) = LOWER('X') \
                ) \
                GROUP BY p.id, p.name, v.id, p.slug, b.name, c.name, v.promotional_price, v.sale_price, v.promotion_start_time, v.promotion_end_time \
                LIMIT 5;""");

        examples.add("""
                User: "Cho mình các sản phẩm váy công sở giá dưới 700000"
                SQL:
                SELECT p.name, p.description, p.slug, b.name AS brand, \
                GROUP_CONCAT(img.url SEPARATOR ',') AS images, c.name AS category, \
                CASE WHEN v.promotional_price IS NOT NULL AND NOW() BETWEEN v.promotion_start_time AND v.promotion_end_time \
                THEN v.promotional_price ELSE v.sale_price END AS price \
                FROM products p \
                JOIN variants v ON v.product_id = p.id \
                JOIN product_images img ON p.id = img.product_id \
                JOIN categories c ON c.id = p.category_id \
                JOIN brands b ON b.id = p.brand_id \
                WHERE p.status = true AND p.is_deleted = false \
                AND p.name LIKE '%váy%' \
                AND CASE WHEN v.promotional_price IS NOT NULL AND NOW() BETWEEN v.promotion_start_time AND v.promotion_end_time \
                THEN v.promotional_price ELSE v.sale_price END < 700000 \
                GROUP BY p.id, p.name, v.id, p.slug, b.name, c.name, v.promotional_price, v.sale_price, v.promotion_start_time, v.promotion_end_time \
                LIMIT 5;""");

        examples.add("""
                User: "Tìm áo khoác nữ màu đỏ size L, sắp xếp theo giá tăng dần"
                SQL:
                SELECT p.name, p.description, p.slug, b.name AS brand, \
                GROUP_CONCAT(img.url SEPARATOR ',') AS images, c.name AS category, \
                CASE WHEN v.promotional_price IS NOT NULL AND NOW() BETWEEN v.promotion_start_time AND v.promotion_end_time \
                THEN v.promotional_price ELSE v.sale_price END AS price \
                FROM products p \
                JOIN variants v ON v.product_id = p.id \
                JOIN variant_attribute_value vav ON vav.variant_id = v.id \
                JOIN attribute_values av ON av.id = vav.attribute_value_id \
                JOIN attributes a ON a.id = av.attribute_id \
                JOIN product_images img ON p.id = img.product_id \
                JOIN categories c ON c.id = p.category_id \
                JOIN brands b ON b.id = p.brand_id \
                WHERE p.status = true AND p.is_deleted = false \
                AND LOWER(a.name) = LOWER('color') AND LOWER(av.value) = LOWER('red') \
                AND EXISTS ( \
                SELECT 1 FROM variant_attribute_value vav2 \
                JOIN attribute_values av2 ON av2.id = vav2.attribute_value_id \
                JOIN attributes a2 ON a2.id = av2.attribute_id \
                WHERE vav2.variant_id = v.id AND LOWER(a2.name) = LOWER('size') AND LOWER(av2.value) = LOWER('L') \
                ) \
                GROUP BY p.id, p.name, v.id, p.slug, b.name, c.name, v.promotional_price, v.sale_price, v.promotion_start_time, v.promotion_end_time \
                ORDER BY price ASC \
                LIMIT 5;""");

        return examples;
    }

    List<String> loadFewShotExamplesNatural() {
        List<String> examples = new ArrayList<>();

        examples.add("""
                    User: "Tìm quần tây casual cho mặc công sở"
                    Response: "<p>Bạn có thể tham khảo quần tây casual tại Fashion Store, phù hợp mặc công sở, đi học hoặc phối đồ thời trang:</p>
                    <div class=\\"product-card\\" style=\\"font-family:Arial,Helvetica,sans-serif;color:#222\\">
                        <div class=\\"card-row\\" style=\\"border:1px solid #eee;border-radius:8px;padding:10px;margin-bottom:12px;background:#fff;box-shadow:0 1px 2px rgba(0,0,0,0.04);\\">
                            <div class=\\"thumb\\" style=\\"width:84px;height:84px;flex:0 0 84px;margin-right:12px;display:flex;align-items:center;justify-content:center;\\">
                                <img src=\\"https://example.com/trousers.jpg\\" alt=\\"Quần tây Casual A\\" style=\\"max-width:84px;max-height:84px;border-radius:6px;object-fit:cover;\\">
                            </div>
                            <div class=\\"meta\\" style=\\"flex:1;\\">
                                <div class=\\"title\\" style=\\"font-size:14px;margin-bottom:6px;\\">
                                    <a href=\\"/product/quan-tay-casual-a\\" style=\\"color:#0b66c3;text-decoration:none;font-weight:600;\\">Quần tây Casual A</a>
                                </div>
                                <div class=\\"desc\\" style=\\"font-size:13px;color:#444;margin-bottom:8px;\\">
                                    <ul style=\\"margin:0;padding-left:16px;\\">
                                        <li>Vải co giãn, thoáng mát</li>
                                        <li>Form ôm vừa, phù hợp đi làm</li>
                                    </ul>
                                </div>
                                <div class=\\"price\\" style=\\"text-align: end;font-size:16px;font-weight:700;color:#111;\\">399.000đ</div>
                            </div>
                        </div>
                    </div>"
                """);

        examples.add("""
                    User: "Tìm áo polo size M"
                    Response: "<p>Chúng tôi có một số mẫu áo polo phù hợp với size M, bạn có thể xem qua:</p>
                    <div class=\\"product-card\\" style=\\"font-family:Arial,Helvetica,sans-serif;color:#222\\">
                        <div class=\\"card-row\\" style=\\"border:1px solid #eee;border-radius:8px;padding:10px;margin-bottom:12px;background:#fff;box-shadow:0 1px 2px rgba(0,0,0,0.04);\\">
                            <div class=\\"thumb\\" style=\\"width:84px;height:84px;flex:0 0 84px;margin-right:12px;display:flex;align-items:center;justify-content:center;\\">
                                <img src=\\"https://res.cloudinary.com/demo/polo.jpg\\" alt=\\"Áo Polo Xanh\\" style=\\"max-width:84px;max-height:84px;border-radius:6px;object-fit:cover;\\">
                            </div>
                            <div class=\\"meta\\" style=\\"flex:1;\\">
                                <div class=\\"title\\" style=\\"font-size:14px;margin-bottom:6px;\\">
                                    <a href=\\"/product/ao-polo-xanh\\" style=\\"color:#0b66c3;text-decoration:none;font-weight:600;\\">Áo Polo Xanh</a>
                                </div>
                                <div class=\\"desc\\" style=\\"font-size:13px;color:#444;margin-bottom:8px;\\">
                                    <ul style=\\"margin:0;padding-left:16px;\\">
                                        <li>Chất liệu cotton thoáng mát</li>
                                        <li>Form regular, dễ phối đồ</li>
                                    </ul>
                                </div>
                                <div class=\\"price\\" style=\\"text-align: end;font-size:16px;font-weight:700;color:#111;\\">377.150đ</div>
                            </div>
                        </div>
                    </div>"
                """);

        examples.add("""
                    User: "Xin chào"
                    Response: "<p>Chào bạn! Fashion Store rất vui được hỗ trợ. Bạn cần tư vấn về sản phẩm, hay có thắc mắc gì không ạ?</p>"
                """);

        examples.add("""
                    User: "Cho mình xem áo mới"
                    Response: "<p>Dưới đây là một số mẫu áo mới tại Fashion Store mà bạn có thể tham khảo:</p>
                    <div class=\\"product-card\\" style=\\"font-family:Arial,Helvetica,sans-serif;color:#222\\">
                        <div class=\\"card-row\\" style=\\"border:1px solid #eee;border-radius:8px;padding:10px;margin-bottom:12px;background:#fff;box-shadow:0 1px 2px rgba(0,0,0,0.04);\\">
                            <div class=\\"thumb\\" style=\\"width:84px;height:84px;flex:0 0 84px;margin-right:12px;display:flex;align-items:center;justify-content:center;\\">
                                <div style=\\"width:84px;height:84px;background:#f5f5f5;border-radius:6px;display:flex;align-items:center;justify-content:center;color:#999;font-size:12px;\\">No image</div>
                            </div>
                            <div class=\\"meta\\" style=\\"flex:1;\\">
                                <div class=\\"title\\" style=\\"font-size:14px;margin-bottom:6px;\\">
                                    <a href=\\"/product/ao-moi-a\\" style=\\"color:#0b66c3;text-decoration:none;font-weight:600;\\">Áo Mới A</a>
                                </div>
                                <div class=\\"desc\\" style=\\"font-size:13px;color:#444;margin-bottom:8px;\\">
                                    <p>Thiết kế basic, phù hợp mặc hàng ngày.</p>
                                </div>
                                <div class=\\"price\\" style=\\"text-align: end;font-size:16px;font-weight:700;color:#111;\\">Price: null</div>
                            </div>
                        </div>
                    </div>"
                """);

        return examples;
    }

    String buildNL2SQLPrompt(String schemaJson, List<String> examples, String userMessage, List<ConversationHistory> conversationHistory) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert SQL generator for MySQL. Only output the SQL SELECT statement, nothing else.\n\n");
        sb.append("Database schema (JSON):\n").append(schemaJson).append("\n\n");
        sb.append("Examples:\n");
        for (String ex : examples) sb.append(ex).append("\n\n");
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            sb.append("Conversation history (oldest -> newest):\n");
            for (ConversationHistory h : conversationHistory) {
                if (h == null) continue;
                String roleName = (h.getRole() != null) ? h.getRole().toLowerCase() : "unknown";
                String msg = (h.getMessage() != null) ? escapeQuotes(h.getMessage()) : "";
                sb.append(roleName).append(": \"").append(msg).append("\"\n");
            }
            sb.append("\n");
        }
        sb.append("# Now user:\nUser: \"").append(escapeQuotes(userMessage)).append("\"\nSQL:");
        return sb.toString();
    }

    String generateNaturalAnswer(String userMessage, List<ProductVariantResponse> products, List<ConversationHistory> conversationHistory) throws Exception {
        StringBuilder jsonBuilder = new StringBuilder();
        if (products != null && !products.isEmpty()) {
            jsonBuilder.append("[");
            for (int i = 0; i < products.size(); i++) {
                ProductVariantResponse p = products.get(i);
                jsonBuilder.append("{")
                        .append("\"name\":\"").append(optional(p.getName())).append("\",")
                        .append("\"description\":\"").append(optional(p.getDescription())).append("\",")
                        .append("\"price\":\"").append(p.getPrice() != null ? formatPrice(p.getPrice()) : "null").append("\",")
                        .append("\"images\":\"").append(optional(p.getImages())).append("\",")
                        .append("\"slug\":\"").append(optional(p.getSlug())).append("\"")
                        .append("}");
                if (i < products.size() - 1) jsonBuilder.append(",");
            }
            jsonBuilder.append("]");
        } else {
            jsonBuilder.append("[]");
        }

        StringBuilder historyJson = new StringBuilder();
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            historyJson.append("[");
            for (int i = 0; i < conversationHistory.size(); i++) {
                ConversationHistory h = conversationHistory.get(i);
                String roleName = (h.getRole() != null) ? h.getRole().toLowerCase() : "unknown";
                String msg = (h.getMessage() != null) ? escapeQuotes(h.getMessage()) : "";
                historyJson.append("{")
                        .append("\"role\":\"").append(roleName).append("\",")
                        .append("\"message\":\"").append(msg).append("\"")
                        .append("}");
                if (i < conversationHistory.size() - 1) historyJson.append(",");
            }
            historyJson.append("]");
        } else {
            historyJson.append("[]");
        }

        // Build prompt with strict rules and few-shot examples
        String prompt = """
                You are a friendly fashion e-commerce assistant for the website "Fashion Store".
                Respond in the same language as the user's message.
                
                OUTPUT RULE (strict): Output ONLY a single HTML fragment (no explanation, no markdown, no additional text).
                The HTML will be inserted directly into the frontend via innerHTML.
                
                INSTRUCTIONS:
                - Use ONLY the product data from the Product JSON below. Do NOT invent or change any field.
                - If product list is non-empty, produce an HTML fragment containing one product card per product.
                - If product list is empty or the user is greeting/asking non-product things, produce a short friendly HTML paragraph (e.g., <p>...</p>).
                - Each product card must follow the required card structure (see template below).
                - Description: shorten to at most 2 bullet points (<=120 chars each). If description includes HTML or escaped HTML, extract plain text and summarize.
                - Price: if price == "null", render exactly: <div class="price">Price: null</div>
                - Images: if images string is empty, show a placeholder div with text 'No image' inside the thumb area (see examples).
                - Links: product link must be /product/<slug>
                - SECURITY: do NOT include <script> tags, inline event handlers, or external links.
                - Use conversation history to maintain context and tone.
                - Do NOT repeat old assistant messages unless relevant to the new reply.
                - Do NOT hallucinate or create new history lines.
                
                REQUIRED CARD TEMPLATE (follow structure and attributes; you may inline styles as in examples):
                <div class="product-card" style="font-family:Arial,Helvetica,sans-serif;color:#222">
                  <div class="card-row" style="border:1px solid #eee;border-radius:8px;padding:10px;margin-bottom:12px;background:#fff;box-shadow:0 1px 2px rgba(0,0,0,0.04);">
                    <div class="thumb" style="width:84px;height:84px;flex:0 0 84px;margin-right:12px;display:flex;align-items:center;justify-content:center;">
                      <!-- <img> when available, otherwise placeholder -->
                    </div>
                    <div class="meta" style="flex:1;">
                      <div class="title" style="font-size:14px;margin-bottom:6px;">
                        <a href="/product/{SLUG}" style="color:#0b66c3;text-decoration:none;font-weight:600;">{PRODUCT_NAME}</a>
                      </div>
                      <div class="desc" style="font-size:13px;color:#444;margin-bottom:8px;">
                        {DESCRIPTION_SHORT_HTML}
                      </div>
                      <div class="price" style="text-align: end;font-size:16px;font-weight:700;color:#111;">{PRICE}</div>
                    </div>
                  </div>
                </div>
                
                FEW-SHOT EXAMPLES (do not output the text 'EXAMPLES' in the final answer; they are for your understanding):
                """ + String.join("\n\n", loadFewShotExamplesNatural()) + "\n\n" +
                "Product JSON:\n" + jsonBuilder.toString() + "\n\n" +
                "Conversation History JSON:\n" + historyJson.toString() + "\n\n" +
                "User's message: \"" + userMessage + "\"\n\n" +
                "Now output ONLY the HTML fragment (no commentary):";

        return callGemini(prompt);
    }

    String optional(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    String formatPrice(BigDecimal price) {
        if (price == null) return "null";
        return String.format("%,.0fđ", price); // dấu chấm ngăn cách hàng nghìn
    }

    String escapeQuotes(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    String callGemini(String prompt) throws Exception {
        String url = geminiEndpoint + "?key=" + geminiApiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String requestBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException(
                    "Gemini API error: " + response.statusCode() + " - " + response.body()
            );
        }

        return response.body();
    }

    public String extractTextFromResponse(String jsonResponse) {
        if (jsonResponse == null) return "";
        JSONObject obj = new JSONObject(jsonResponse);
        JSONArray candidates = obj.optJSONArray("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.optJSONObject("content");
            if (content != null) {
                JSONArray parts = content.optJSONArray("parts");
                if (parts != null && !parts.isEmpty()) {
                    JSONObject firstPart = parts.getJSONObject(0);
                    return firstPart.optString("text", "");
                }
            }
        }
        return obj.optString("text", jsonResponse);
    }

    List<ProductVariantResponse> mapRowsToProducts(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<ProductVariantResponse> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            ProductVariantResponse p = new ProductVariantResponse();
            Object name = r.get("name");
            Object priceObj = r.get("price");
            Object images = r.get("images");
            Object description = r.get("description");
            Object slug = r.get("slug");
            Object category = r.get("category");
            Object brand = r.get("brand");
            p.setName(name != null ? String.valueOf(name) : null);
            p.setImages(images != null ? String.valueOf(images) : null);
            p.setDescription(description != null ? String.valueOf(description) : null);
            p.setSlug(slug != null ? String.valueOf(slug) : null);
            p.setCategory(category != null ? String.valueOf(category) : null);
            p.setBrand(brand != null ? String.valueOf(brand) : null);

            if (priceObj == null) {
                p.setPrice(null);
            } else if (priceObj instanceof BigDecimal) {
                p.setPrice((BigDecimal) priceObj);
            } else if (priceObj instanceof Number) {
                p.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            } else {
                try {
                    p.setPrice(new BigDecimal(String.valueOf(priceObj)));
                } catch (Exception ex) {
                    p.setPrice(null);
                }
            }

            out.add(p);
        }
        return out;
    }

    ProductVariantResponse selectClosestProduct(List<ProductVariantResponse> products, String userMessage) {
        if (products == null || products.isEmpty()) return null;

        ProductVariantResponse bestProduct = null;
        int bestScore = -1;
        String lowerMsg = userMessage.toLowerCase();

        for (ProductVariantResponse p : products) {
            String name = p.getName() != null ? p.getName().toLowerCase() : "";
            int score = 0;

            for (String w : lowerMsg.split("\\s+")) {
                if (!w.isBlank() && name.contains(w)) score++;
            }

            if (score > bestScore) {
                bestScore = score;
                bestProduct = p;
            }
        }

        return bestProduct;
    }
}
