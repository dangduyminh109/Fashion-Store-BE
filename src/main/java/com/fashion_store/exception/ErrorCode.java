package com.fashion_store.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    // ===== COMMON (9000-9099) =====
    INTERNAL_EXCEPTION(9000, "Lỗi máy chủ nội bộ", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_EXIST(9001, "Đối tượng không tồn tại", HttpStatus.NOT_FOUND),
    EXISTED(9002, "Đối tượng đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_KEY(9003, "Khóa không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_NAME(9004, "Tên không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_VALUE(9005, "Giá trị không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_FILE(9006, "Tệp không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_SAVE_FAILED(9007, "Lưu tệp thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CODE(9008, "Mã không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_FORM_FORMAT(9009, "Định dạng không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_CONTENT(9010, "Nội dung không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_TITLE(9011, "Tiêu đề không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_LIST_IMPORT_ITEM(9012, "Danh sách mục nhập phải có ít nhất một mục", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_MEDIA_TYPE(9013, "Loại phương tiện không được hỗ trợ", HttpStatus.BAD_REQUEST),

    // ===== PRODUCT (9100-9199) =====
    PRODUCT_NOT_EXIST(9100, "Sản phẩm không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT_ID(9101, "ID sản phẩm không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_SKU(9102, "SKU phải có ít nhất 3 ký tự", HttpStatus.BAD_REQUEST),
    INVALID_VARIANT(9103, "Yêu cầu ít nhất một biến thể", HttpStatus.BAD_REQUEST),
    INVALID_VARIANT_ID(9104, "ID biến thể không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_VARIANT_SKU(9105, "Mã SKU của biến thể không hợp lệ", HttpStatus.BAD_REQUEST),
    VARIANT_ID_MISMATCH(9106, "ID biến thể không khớp với danh sách cần cập nhật", HttpStatus.BAD_REQUEST),
    INVALID_VARIANT_LIST(9107, "Dữ liệu biến thể không hợp lệ", HttpStatus.BAD_REQUEST),
    VARIANT_NOT_AVAILABLE(9108, "Sản phẩm không có sẵn", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(9109, "Giá không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ORIGINAL_PRICE(9110, "Giá gốc phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_SALE_PRICE(9111, "Giá bán phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_PROMOTIONAL_PRICE(9112, "Giá khuyến mãi phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_TIME(9113, "Thời gian khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_START_TIME(9114, "Thời gian bắt đầu khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_END_TIME(9115, "Thời gian kết thúc khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ATTRIBUTE_VALUE(9116, "Phải có ít nhất một giá trị thuộc tính trong biến thể", HttpStatus.BAD_REQUEST),
    INVALID_ATTRIBUTE_COUNT(9117, "Thuộc tính phải có ít nhất một giá trị", HttpStatus.BAD_REQUEST),
    INVALID_IMPORT_PRICE(9118, "Giá nhập không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_TYPE(9119, "Loại hiển thị không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_TYPE_DATA(9120, "Kiểu dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    SKU_EXISTED(9120, "Mã sản phẩm bị trùng hoặc đã tồn tại", HttpStatus.BAD_REQUEST),

    // ===== STOCK / QUANTITY (9130-9139) =====
    INVALID_QUANTITY(9130, "Số lượng phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY_UPDATE(9131, "Số lượng yêu cầu vượt quá số lượng hiện có", HttpStatus.BAD_REQUEST),

    // ===== ORDER (9200-9299) =====
    INVALID_ORDER_ID(9200, "ID đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS(9201, "Trạng thái đơn hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_PAID(9202, "Đơn hàng đã được thanh toán", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_ITEM_LIST(9203, "Đơn hàng phải có ít nhất 1 sản phẩm", HttpStatus.BAD_REQUEST),
    NOT_AUTHORIZED_ORDER_ACCESS(9204, "Bạn không được phép truy cập đơn hàng này", HttpStatus.FORBIDDEN),
    INVALID_ORDER_VALUE(9205, "Giá trị đơn hàng tối thiểu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_METHOD(9206, "Phương thức thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAID_ORDER(9207, "Đơn hàng đã được thanh toán", HttpStatus.BAD_REQUEST),

    // ===== VOUCHER / PROMOTION (9300-9399) =====
    INVALID_VOUCHER_TIME(9300, "Thời gian voucher không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_VOUCHER_DISCOUNT(9301, "Giảm giá voucher không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_VOUCHER_TYPE(9302, "Loại voucher không hợp lệ", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_STARTED(9303, "Voucher chưa được áp dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_USAGE_LIMIT_EXCEEDED(9304, "Voucher đã được sử dụng hết", HttpStatus.BAD_REQUEST),
    VOUCHER_EXPIRED(9305, "Voucher đã hết hạn", HttpStatus.BAD_REQUEST),

    // ===== AUTH / USER (9400-9499) =====
    UNAUTHENTICATED(9400, "Chưa được xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(9401, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(9402, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    CANNOT_CREATE_TOKEN(9403, "Không thể tạo token", HttpStatus.BAD_REQUEST),
    INVALID_AUTH_PROVIDER(9404, "Nhà cung cấp xác thực không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_LIST_PERMISSION_ID(9405, "Danh sách ID quyền không hợp lệ", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(9406, "Tên người dùng phải có ít nhất 3 ký tự", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(9407, "Yêu cầu nhập mật khẩu", HttpStatus.BAD_REQUEST),
    PASSWORD_LENGTH_INVALID(9408, "Mật khẩu phải từ 6 đến 32 ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(9409, "Mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST),
    PASSWORD_WEAK(9410, "Mật khẩu phải chứa ít nhất một chữ cái in hoa, một chữ cái thường, một số và một ký tự đặc biệt", HttpStatus.BAD_REQUEST),
    INVALID_OTP(9411, "OTP không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(9412, "Yêu cầu nhập email", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(9413, "Định dạng email không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(9414, "Gửi email thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    ACTION_FORBIDDEN(9415, "Hành động bị cấm", HttpStatus.FORBIDDEN),
    INVALID_SIGNATURE(9416, "Chữ ký không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT(9417, "Tài khoản không hợp lệ hoặc đã bị khóa", HttpStatus.FORBIDDEN),

    // ===== CUSTOMER / ADDRESS (9500-9599) =====
    INVALID_CUSTOMER_ID(9500, "ID khách hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_CUSTOMER_NAME(9501, "Tên khách hàng không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(9502, "Số điện thoại không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS(9503, "Địa chỉ không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS_PHONE(9504, "Số điện thoại trong địa chỉ không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_CITY(9505, "Thành phố không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DISTRICT(9506, "Quận/Huyện không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_WARD(9507, "Phường/Xã không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DOB(9508, "Ngày sinh phải từ 17 tuổi trở lên", HttpStatus.BAD_REQUEST),
    CUSTOMER_HAS_ORDERS(9509, "Không thể xóa vì đang tồn tại đơn hàng liên kết với khách hàng này", HttpStatus.BAD_REQUEST),

    // ===== ROLE / PERMISSION (9600-9699) =====
    CANNOT_BE_DELETE(9600, "Không thể xóa vai trò quản trị viên", HttpStatus.BAD_REQUEST),
    CANNOT_BE_UPDATE(9601, "Không thể cập nhật vai trò quản trị viên", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_ID(9602, "ID vai trò không hợp lệ", HttpStatus.BAD_REQUEST),
    ROLE_IN_USE(9603, "Không thể xóa vai trò vì hiện đang được gán cho một hoặc nhiều người dùng", HttpStatus.BAD_REQUEST),
    ROLE_UPDATE_NOT_ALLOWED(9604, "Chỉ Admin mới có quền cập nhật vai trò", HttpStatus.BAD_REQUEST),
    ROLE_DELETE_NOT_ALLOWED(9605, "Chỉ Admin mới có quền xóa vai trò", HttpStatus.BAD_REQUEST);

    int code;
    String message;
    HttpStatus httpStatusCode;
}