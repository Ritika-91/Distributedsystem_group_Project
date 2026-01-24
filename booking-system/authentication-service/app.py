import os
import time
import jwt
from datetime import datetime, timedelta, timezone 
from flask import Flask, request, jsonify
from flask_cors import CORS 
import psycopg2
import bcrypt



DB_HOST = os.environ.get('DB_HOST', 'postgres-db') 
DB_NAME = os.environ.get('DB_NAME', 'auth_db')
DB_USER = os.environ.get('DB_USER', 'user')
DB_PASSWORD = os.environ.get('DB_PASSWORD', 'password')
DB_PORT = os.environ.get('DB_PORT', '5432')

JWT_SECRET_KEY = os.environ.get('JWT_SECRET_KEY', 'default-insecure-key') 
TOKEN_EXPIRATION_HOURS = 1 

app = Flask(__name__)
CORS(app) 


def get_db_connection():
    conn = None
    for i in range(5):
        try:
            conn = psycopg2.connect(
                host=DB_HOST,
                database=DB_NAME,
                user=DB_USER,
                password=DB_PASSWORD,
                port=DB_PORT,
                connect_timeout=5
            )
            print("Database connection established successfully.")
            return conn
        except psycopg2.Error as e:
            wait_time = 2**i
            print(f"Attempt {i+1}/5: Database connection failed. Retrying in {wait_time} seconds... Error: {e}")
            time.sleep(wait_time)
    raise Exception("Could not connect to the database after multiple retries.")

def init_db():
    conn = None
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(80) UNIQUE NOT NULL,
                password_hash BYTEA NOT NULL,
                -- Example for adding a role column (optional)
                role VARCHAR(50) DEFAULT 'USER', 
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """)
        conn.commit()
        cur.close()
        print("Database initialized (users table ensured).")
    except psycopg2.Error as e:
        print(f"Error initializing database: {e}")
    finally:
        if conn:
            conn.close()

init_db()


def hash_password(password):
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

def check_password(password, hashed_password):
    return bcrypt.checkpw(password.encode('utf-8'), hashed_password)

def generate_auth_token(username, user_id, role="USER"):
    try:
        expiration_time = datetime.now(timezone.utc) + timedelta(hours=TOKEN_EXPIRATION_HOURS)
        
        payload = {
            'sub': username,
            'user_id': user_id, 
            'role': role,
            'exp': expiration_time,
            'iat': datetime.now(timezone.utc),
        }
        
        token = jwt.encode(
            payload,
            JWT_SECRET_KEY,
            algorithm='HS256'
        )
        return token
    except Exception as e:
        print(f"JWT token generation error: {e}")
        return None


@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    role = data.get('role', 'USER') 

    if not username or not password:
        return jsonify({"message": "Username and password are required"}), 400

    hashed_pw = hash_password(password)
    
    conn = None
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO users (username, password_hash, role) VALUES (%s, %s, %s)",
            (username, hashed_pw, role)
        )
        conn.commit()
        cur.close()
        return jsonify({"message": f"User '{username}' registered successfully"}), 201
    except psycopg2.IntegrityError:
        return jsonify({"message": "Registration failed: Username already exists"}), 409
    except Exception as e:
        print(f"Registration error: {e}")
        return jsonify({"message": "An internal error occurred during registration"}), 500
    finally:
        if conn:
            conn.close()

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"message": "Username and password are required"}), 400

    conn = None
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        
        cur.execute(
            "SELECT id, password_hash, role FROM users WHERE username = %s", 
            (username,)
        )
        user_record = cur.fetchone()
        cur.close()

        if user_record:
            user_id = user_record[0]
            stored_hash = user_record[1]
            user_role = user_record[2]
            
            if not isinstance(stored_hash, bytes):
                stored_hash = bytes(stored_hash) 

            if check_password(password, stored_hash):
                auth_token = generate_auth_token(username, user_id, user_role)
                if not auth_token:
                    return jsonify({"message": "Failed to generate token"}), 500
                    
                return jsonify({
                    "message": f"Login successful!", 
                    "token": auth_token, 
                    "status": "authenticated"
                }), 200
            else:
                return jsonify({"message": "Incorrect credentials"}), 401
        else:
            return jsonify({"message": "Incorrect credentials"}), 401
            
    except Exception as e:
        print(f"Login error: {e}")
        return jsonify({"message": "An internal error occurred during login"}), 500
    finally:
        if conn:
            conn.close()

@app.route('/health', methods=['GET'])
def health_check():
    conn = None
    db_status = "down"
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT 1")
        cur.fetchone()
        cur.close()
        db_status = "ok"
    except Exception:
        db_status = "down"
    finally:
        if conn:
            conn.close()
            
    if db_status == "ok":
        return jsonify({"status": "ok", "service": "Authentication Service", "database_status": db_status}), 200
    else:
        return jsonify({"status": "degraded", "service": "Authentication Service", "database_status": db_status}), 503


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)