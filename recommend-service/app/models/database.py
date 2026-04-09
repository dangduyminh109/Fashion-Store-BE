import os
from pymongo import MongoClient

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
DB_NAME = os.getenv("MONGO_DB_NAME", "fashion_store_recommendations")
COLLECTION_NAME = "recommend_rules"

client = MongoClient(MONGO_URI)
db = client[DB_NAME]
recommend_rules = db[COLLECTION_NAME]

recommend_rules.create_index([("antecedent", 1)])
