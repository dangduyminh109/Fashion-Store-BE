import pandas as pd
from mlxtend.preprocessing import TransactionEncoder
from mlxtend.frequent_patterns import association_rules, fpgrowth
from datetime import datetime
from app.models.database import recommend_rules
import logging
import os
from collections import Counter, defaultdict

logger = logging.getLogger(__name__)

def get_int_env(name, default):
    try:
        return int(os.getenv(name, str(default)))
    except ValueError:
        logger.warning("Invalid %s value. Using default: %s", name, default)
        return default

DEFAULT_TOP_K_ITEMS = get_int_env("RECOMMEND_TOP_K_ITEMS", 3000)
MIN_PAIR_COUNT = get_int_env("RECOMMEND_MIN_PAIR_COUNT", 3)

def train_apriori(
    dataset,
    min_support=0.001,
    min_confidence=0.1,
    top_k_items=DEFAULT_TOP_K_ITEMS
):
    logger.info("===== START TRAIN FP-GROWTH =====")

    if not dataset or len(dataset) < 2:
        logger.warning("Not enough data to train")
        return False

    logger.info(f"Total transactions: {len(dataset)}")

    valid_dataset = [t for t in dataset if len(t) >= 2]
    if len(valid_dataset) < 2:
        logger.warning("Not enough valid transactions after length filter")
        return False

    lengths = [len(t) for t in valid_dataset]
    logger.info(f"Min items: {min(lengths)} | Max items: {max(lengths)} | Avg items: {sum(lengths)/len(lengths):.2f}")
    logger.info(f"After filter >=2 items: {len(valid_dataset)}")

    item_counter = Counter()
    for t in valid_dataset:
        item_counter.update(t)

    logger.info(f"Top 10 items: {item_counter.most_common(10)}")

    top_items = set([item for item, _ in item_counter.most_common(top_k_items)])
    
    filtered_dataset = [
        [item for item in t if item in top_items]
        for t in valid_dataset
    ]

    final_dataset = [t for t in filtered_dataset if len(t) >= 2]

    logger.info(f"After top-{top_k_items} filter: {len(final_dataset)}")
    
    if len(final_dataset) < 2:
        logger.warning("Dataset too small after applying top_k filtering")
        return False

    # ===== PHASE 1: FP-Growth Association Rules =====
    te = TransactionEncoder()
    te_ary = te.fit(final_dataset).transform(final_dataset, sparse=True)
    
    df = pd.DataFrame.sparse.from_spmatrix(te_ary, columns=te.columns_)
    df.columns = [str(i) for i in df.columns]
    
    logger.info(f"Encoded shape: {df.shape}")

    frequent_itemsets = fpgrowth(
        df,
        min_support=min_support,
        use_colnames=True
    )

    fpgrowth_rules_count = 0
    version_tag = f"v_{datetime.now().strftime('%Y%m%d%H%M%S')}"
    new_rules = []

    if not frequent_itemsets.empty:
        logger.info(f"Frequent itemsets found: {len(frequent_itemsets)}")

        rules = association_rules(
            frequent_itemsets,
            metric="confidence",
            min_threshold=min_confidence
        )

        if not rules.empty:
            logger.info(f"FP-Growth rules generated: {len(rules)}")
            for _, row in rules.iterrows():
                antecedents = [int(x) for x in row['antecedents']]
                consequents = [int(x) for x in row['consequents']]
                for consequent in consequents:
                    new_rules.append({
                        "antecedent": antecedents,
                        "consequent": consequent,
                        "support": float(row['support']),
                        "confidence": float(row['confidence']),
                        "lift": float(row['lift']),
                        "source": "fpgrowth",
                        "createdAt": datetime.now(),
                        "version": version_tag
                    })
            fpgrowth_rules_count = len(new_rules)
    else:
        logger.warning(f"No frequent itemsets found with min_support={min_support}")

    # ===== PHASE 2: Co-occurrence Rules (Pairwise) =====
    # Use ALL valid transactions (not filtered by top_k) for broader coverage
    logger.info("===== START CO-OCCURRENCE MINING =====")
    logger.info(f"Co-occurrence input: {len(valid_dataset)} transactions (ALL products)")
    
    pair_count = defaultdict(int)
    item_count = defaultdict(int)
    
    for transaction in valid_dataset:
        unique_items = list(set(transaction))
        for item in unique_items:
            item_count[item] += 1
        # Count all pairs
        for i in range(len(unique_items)):
            for j in range(i + 1, len(unique_items)):
                a, b = unique_items[i], unique_items[j]
                pair_key = (min(a, b), max(a, b))
                pair_count[pair_key] += 1

    total_transactions = len(valid_dataset)
    min_pair_count = MIN_PAIR_COUNT  # At least 3 co-occurrences for broader coverage
    
    co_occurrence_rules = []
    existing_pairs = set()
    
    # Track existing FP-Growth rules to avoid duplicates
    for rule in new_rules:
        if len(rule["antecedent"]) == 1:
            existing_pairs.add((rule["antecedent"][0], rule["consequent"]))
    
    for (item_a, item_b), count in pair_count.items():
        if count < min_pair_count:
            continue
            
        support = count / total_transactions
        
        # Rule: A -> B
        if item_count[item_a] > 0 and (item_a, item_b) not in existing_pairs:
            confidence_ab = count / item_count[item_a]
            expected_b = item_count[item_b] / total_transactions
            lift_ab = confidence_ab / expected_b if expected_b > 0 else 0
            
            co_occurrence_rules.append({
                "antecedent": [int(item_a)],
                "consequent": int(item_b),
                "support": round(support, 6),
                "confidence": round(confidence_ab, 4),
                "lift": round(lift_ab, 4),
                "source": "co_occurrence",
                "createdAt": datetime.now(),
                "version": version_tag
            })
        
        # Rule: B -> A
        if item_count[item_b] > 0 and (item_b, item_a) not in existing_pairs:
            confidence_ba = count / item_count[item_b]
            expected_a = item_count[item_a] / total_transactions
            lift_ba = confidence_ba / expected_a if expected_a > 0 else 0
            
            co_occurrence_rules.append({
                "antecedent": [int(item_b)],
                "consequent": int(item_a),
                "support": round(support, 6),
                "confidence": round(confidence_ba, 4),
                "lift": round(lift_ba, 4),
                "source": "co_occurrence",
                "createdAt": datetime.now(),
                "version": version_tag
            })

    logger.info(f"Co-occurrence rules generated: {len(co_occurrence_rules)}")
    new_rules.extend(co_occurrence_rules)

    # ===== SAVE ALL RULES =====
    if new_rules:
        recommend_rules.insert_many(new_rules)
        recommend_rules.delete_many({"version": {"$ne": version_tag}})
        logger.info(f"Total saved: {len(new_rules)} rules "
                    f"(FP-Growth: {fpgrowth_rules_count}, Co-occurrence: {len(co_occurrence_rules)}). "
                    f"Version: {version_tag}")
        return True

    logger.warning("No rules to save")
    return False
