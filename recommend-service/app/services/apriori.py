import pandas as pd
from mlxtend.preprocessing import TransactionEncoder
from mlxtend.frequent_patterns import association_rules, fpgrowth
from datetime import datetime
from app.models.database import recommend_rules
import logging
from collections import Counter

logger = logging.getLogger(__name__)

def train_apriori(
    dataset,
    min_support=0.001,
    min_confidence=0.1,
    top_k_items=1000
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

    if frequent_itemsets.empty:
        logger.warning(f"No frequent itemsets found with min_support={min_support}")
        return False
        
    logger.info(f"Frequent itemsets found: {len(frequent_itemsets)}")

    rules = association_rules(
        frequent_itemsets,
        metric="confidence",
        min_threshold=min_confidence
    )

    if rules.empty:
        logger.warning(f"No rules generated with min_confidence={min_confidence}")
        return False
        
    logger.info(f"Rules generated: {len(rules)}")

    version_tag = f"v_{datetime.now().strftime('%Y%m%d%H%M%S')}"
    new_rules = []

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
                "createdAt": datetime.now(),
                "version": version_tag
            })

    if new_rules:
        recommend_rules.insert_many(new_rules)
        recommend_rules.delete_many({"version": {"$ne": version_tag}})
        logger.info(f"Saved {len(new_rules)} rules. Version: {version_tag}")
        return True

    logger.warning("No rules to save")
    return False