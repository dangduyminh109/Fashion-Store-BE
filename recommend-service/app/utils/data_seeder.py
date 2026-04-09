import pandas as pd
import uuid
import random
import os
import math
from datetime import datetime

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATA_DIR = os.path.join(BASE_DIR, 'dataset_raw')
ARTICLES_CSV = os.path.join(DATA_DIR, 'articles_full_v1.csv')
CUSTOMERS_CSV = os.path.join(DATA_DIR, 'customers_full_v1.csv')
TRANSACTIONS_CSV = os.path.join(DATA_DIR, 'transactions_1M_v1.csv')
SQL_OUTPUT = os.path.join(DATA_DIR, 'seed.sql')

def escape_sql(s):
    if pd.isna(s):
        return 'NULL'
    if isinstance(s, str):
        s = s.replace("'", "''")
        return f"'{s}'"
    return str(s)

def format_date(dt):
    return f"'{dt.strftime('%Y-%m-%d %H:%M:%S')}'"

def slugify(text):
    if pd.isna(text):
        return str(uuid.uuid4())[:8]
    return str(text).lower().replace("'", "").replace(" ", "-").replace("/", "-").replace("&", "and")

def chunked_inserts(table, columns, values, chunk_size=1000):
    lines = []
    col_str = ", ".join(columns)
    for i in range(0, len(values), chunk_size):
        chunk = values[i:i + chunk_size]
        vals_str = ", ".join([f"({', '.join(map(str, v))})" for v in chunk])
        lines.append(f"INSERT IGNORE INTO {table} ({col_str}) VALUES {vals_str};\n")
    return lines

def main():
    print("Loading Datasets...")
    if not os.path.exists(ARTICLES_CSV) or not os.path.exists(CUSTOMERS_CSV) or not os.path.exists(TRANSACTIONS_CSV):
        print("CSV files missing in dataset_raw!")
        return

    articles_df = pd.read_csv(ARTICLES_CSV)
    customers_df = pd.read_csv(CUSTOMERS_CSV)
    transactions_df = pd.read_csv(TRANSACTIONS_CSV)

    now = datetime.now()
    now_str = format_date(now)

    f = open(SQL_OUTPUT, 'w', encoding='utf-8')
    f.write("SET FOREIGN_KEY_CHECKS=0;\n\n")

    f.write("TRUNCATE TABLE order_items;\n")
    f.write("TRUNCATE TABLE orders;\n")
    f.write("TRUNCATE TABLE customers;\n")
    f.write("TRUNCATE TABLE variant_attribute_value;\n")
    f.write("TRUNCATE TABLE variants;\n")
    f.write("TRUNCATE TABLE products;\n")
    f.write("TRUNCATE TABLE attribute_values;\n")
    f.write("TRUNCATE TABLE attributes;\n")
    f.write("TRUNCATE TABLE categories;\n")
    f.write("TRUNCATE TABLE brands;\n\n")

    print("Generating Brands...")
    brands = [("1", now_str, "0", "NULL", "'H&M'", "'h-m'", "1")]
    f.writelines(chunked_inserts('brands', ['id', 'created_at', 'is_deleted', 'image', 'name', 'slug', 'status'], brands))

    print("Generating Categories...")
    category_map = {}
    cat_inserts = []
    cat_id_counter = 1

    # Unique index groups (Level 1)
    for ig in articles_df['index_group_name'].dropna().unique():
        cat_inserts.append((cat_id_counter, now_str, "0", escape_sql(ig), escape_sql(slugify(ig)), "1", "NULL"))
        category_map[ig] = cat_id_counter
        cat_id_counter += 1

    for row in articles_df[['index_group_name', 'product_group_name']].drop_duplicates().dropna().itertuples():
        parent_id = category_map[row.index_group_name]
        cat_inserts.append((cat_id_counter, now_str, "0", escape_sql(row.product_group_name), escape_sql(slugify(row.product_group_name)+f"-{cat_id_counter}"), "1", parent_id))
        category_map[row.product_group_name] = cat_id_counter
        cat_id_counter += 1

    f.writelines(chunked_inserts('categories', ['id', 'created_at', 'is_deleted', 'name', 'slug', 'status', 'parent_id'], cat_inserts))

    print("Generating Attributes...")
    attr_inserts = [(1, now_str, "0", 1, "'Color'", "1"), (2, now_str, "0", 0, "'Appearance'", "1")]
    f.writelines(chunked_inserts('attributes', ['id', 'created_at', 'is_deleted', 'display_type', 'name', 'status'], attr_inserts))

    print("Generating Attribute Values...")
    attr_val_inserts = []
    color_map = {}
    appear_map = {}
    attr_val_id = 1
    for color in articles_df['colour_group_name'].dropna().unique():
        color_map[color] = attr_val_id
        attr_val_inserts.append((attr_val_id, now_str, "0",escape_sql(color), 1))
        attr_val_id += 1
    for appear in articles_df['graphical_appearance_name'].dropna().unique():
        appear_map[appear] = attr_val_id
        attr_val_inserts.append((attr_val_id, now_str, "0", escape_sql(appear), 2))
        attr_val_id += 1

    f.writelines(chunked_inserts('attribute_values', ['id', 'created_at', 'is_deleted', 'value', 'attribute_id'], attr_val_inserts))

    print("Calculating average price for variants...")
    price_df = transactions_df.groupby('article_id')['price'].mean().reset_index()
    price_df['price_vnd'] = (price_df['price'] * 1000000).round()
    price_map = price_df.set_index('article_id')['price_vnd'].to_dict()

    print("Generating Products and Variants...")
    product_inserts = []
    variant_inserts = []
    variant_attr_inserts = []
    product_map = {} 
    prod_id = 1
    var_id = 1

    grouped = articles_df.groupby('product_code')

    for pcode, group in grouped:
        first = group.iloc[0]
        cat_parent = first.get('product_group_name')
        if pd.isna(cat_parent) or cat_parent not in category_map:
            cat_id = 1
        else:
            cat_id = category_map[cat_parent]

        name = escape_sql(first['prod_name'])
        desc = escape_sql(str(first.get('detail_desc', '')))
        slug = escape_sql(f"{slugify(first['prod_name'])}-{pcode}")

        product_inserts.append((prod_id, now_str, "0", desc, "0", name, "0", slug, "1", 1, cat_id))
        product_map[pcode] = prod_id

        # Variants for this product
        for idx, row in group.iterrows():
            sku = escape_sql(str(row['article_id']))
            inventory = random.randint(10, 200)
            avg_price = price_map.get(row['article_id'], 200000) # Default 200k if not in transactions
            
            variant_inserts.append((var_id, now_str, "0", inventory, avg_price, avg_price, sku, "1", prod_id))
            
            color = row.get('colour_group_name')
            if pd.notna(color) and color in color_map:
                variant_attr_inserts.append((var_id, color_map[color]))
                
            appear = row.get('graphical_appearance_name')
            if pd.notna(appear) and appear in appear_map:
                variant_attr_inserts.append((var_id, appear_map[appear]))

            var_id += 1
        
        prod_id += 1

    f.writelines(chunked_inserts('products', ['id', 'created_at', 'is_deleted', 'description', 'is_featured', 'name', 'position', 'slug', 'status', 'brand_id', 'category_id'], product_inserts))
    f.writelines(chunked_inserts('variants', ['id', 'created_at', 'is_deleted', 'inventory', 'original_price', 'sale_price', 'sku', 'status', 'product_id'], variant_inserts))
    f.writelines(chunked_inserts('variant_attribute_value', ['variant_id', 'attribute_value_id'], variant_attr_inserts))

    print("Generating Customers...")
    customer_inserts = []
    # Only keep customers who bought something to reduce size, or limit to top N
    active_customers = set(transactions_df['customer_id'].unique())
    customer_map = {}
    
    count = 1
    import hashlib
    for idx, row in customers_df.iterrows():
        cid = str(row['customer_id'])
        if cid in active_customers:
            my_uuid = str(uuid.uuid5(uuid.NAMESPACE_OID, cid))
            customer_map[cid] = my_uuid
            name = escape_sql(f"Customer {count}")
            email = escape_sql(f"customer{count}@hm.com")
            customer_inserts.append((escape_sql(my_uuid), now_str, "0", 0, email, name, "0", "'123456'", escape_sql(f"09{random.randint(10000000, 99999999)}"), "1"))
            count += 1
            
    f.writelines(chunked_inserts('customers', ['id', 'created_at', 'is_deleted', 'auth_provider', 'email', 'full_name', 'is_guest', 'password', 'phone', 'status'], customer_inserts))

    print("Generating Orders and Order Items...")
    order_inserts = []
    orderitem_inserts = []
    oi_id = 1

    sku_to_var = {}
    sku_to_prodname = {}
    v_id_counter = 1
    for pcode, group in grouped:
        prod_name = escape_sql(group.iloc[0]['prod_name'])
        for _, row in group.iterrows():
            sku_to_var[row['article_id']] = v_id_counter
            sku_to_prodname[row['article_id']] = prod_name
            v_id_counter += 1

    t_groups = transactions_df.groupby(['customer_id', 't_dat'])
    for (cid, date_str), group in t_groups:
        if cid not in customer_map:
            continue
            
        order_uuid = escape_sql(str(uuid.uuid4()))
        db_cid = escape_sql(customer_map[cid])
        date_formatted = f"'{date_str} 10:00:00'"
        
        group['price_vnd'] = (group['price'] * 1000000).round()
        total_amount = group['price_vnd'].sum()
        dummy_addr = "'123 HCM'"
        dummy_name = "'Khách Hàng H&M'"
        dummy_phone = "'0901234567'"
        
        order_inserts.append((order_uuid, date_formatted, "0", dummy_addr, "'HCM'", 1, "'Q1'", 1, dummy_name, "1", 3, 1, dummy_phone, total_amount, total_amount, 0, "'P1'", 1, db_cid))
        
        for _, row in group.iterrows():
            sku = row['article_id']
            if sku in sku_to_var:
                my_var_id = sku_to_var[sku]
                my_pname = sku_to_prodname[sku]
                my_price = row['price_vnd']
                orderitem_inserts.append((oi_id, my_price, my_pname, 1, order_uuid, my_var_id))
                oi_id += 1

    f.writelines(chunked_inserts('orders', ['id', 'created_at', 'is_deleted', 'address', 'city', 'city_id', 'district', 'district_id', 'customer_name', 'is_paid', 'order_status', 'payment_method', 'phone', 'final_amount', 'total_amount', 'total_discount', 'ward', 'ward_id', 'customer_id'], order_inserts))
    f.writelines(chunked_inserts('order_items', ['id', 'price', 'product_name', 'quantity', 'order_id', 'variant_id'], orderitem_inserts))

    f.write("\nSET FOREIGN_KEY_CHECKS=1;\n")
    f.close()
    
    print(f"Data Generation Complete! Seed file saved to {SQL_OUTPUT}")

if __name__ == "__main__":
    main()
