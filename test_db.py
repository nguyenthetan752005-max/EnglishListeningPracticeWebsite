import pymysql
import json

DB_CONFIG = {
    "host": "127.0.0.1",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "english_learning_db",
    "charset": "utf8mb4"
}

try:
    conn = pymysql.connect(**DB_CONFIG)
    cur = conn.cursor(pymysql.cursors.DictCursor)
    cur.execute("SELECT * FROM categories LIMIT 2;")
    print("=== CATEGORIES ===")
    for row in cur.fetchall():
        for k, v in row.items():
            row[k] = str(v)
        print(json.dumps(row, indent=2))
        
    cur.execute("SELECT * FROM lessons LIMIT 2;")
    print("=== LESSONS ===")
    for row in cur.fetchall():
        for k, v in row.items():
            row[k] = str(v)
        print(json.dumps(row, indent=2))

    conn.close()
except Exception as e:
    print(e)
