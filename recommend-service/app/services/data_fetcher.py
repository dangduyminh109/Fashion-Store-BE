import mysql.connector
import os
import logging
from collections import defaultdict

logger = logging.getLogger(__name__)

MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_PORT = os.getenv("MYSQL_PORT", "3306")
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "root")
MYSQL_DB = os.getenv("MYSQL_DB", "fashion-store")

def fetch_orders():
    connection = None
    try:
        connection = mysql.connector.connect(
            host=MYSQL_HOST,
            port=MYSQL_PORT,
            user=MYSQL_USER,
            password=MYSQL_PASSWORD,
            database=MYSQL_DB
        )
        
        cursor = connection.cursor(dictionary=True)
        
        query = """
            SELECT oi.order_id, v.product_id
            FROM order_items oi
            JOIN variants v ON oi.variant_id = v.id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.is_deleted = 0
        """
        
        cursor.execute(query)
        rows = cursor.fetchall()
        
        transactions = defaultdict(set)
        for row in rows:
            transactions[row['order_id']].add(row['product_id'])
            
        result = [list(items) for items in transactions.values() if items]
        logger.info(f"Successfully fetched {len(result)} transactions directly from MySQL.")
        return result

    except Exception as e:
        logger.error(f"Error fetching data directly from MySQL: {e}")
        return []
        
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()
