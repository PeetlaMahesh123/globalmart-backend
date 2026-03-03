# 🛒 GlobalMart E-Commerce Backend

This is the **Spring Boot backend** for the GlobalMart E-Commerce application.
It provides REST APIs for user authentication, product management, cart operations, and order processing.

---
## 🚀 Tech Stack

* **Java 21**
* **Spring Boot**
* **Spring Data JPA**
* **Spring Security**
* **MySQL Database**
* **Maven**
* **REST APIs**
* **JWT Authentication (if implemented)**

---

## 📂 Project Structure

```
globalmart-backend/
│
├── src/main/java/com/globalmart/
│   ├── controller/      # REST Controllers
│   ├── service/         # Business logic
│   ├── repository/      # Database access layer
│   ├── model/           # Entity classes
│   └── GlobalmartApplication.java
│
├── src/main/resources/
│   ├── application.properties
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## ⚙️ Features

✅ User Registration
✅ User Login Authentication
✅ Product Management (Add, View, Update, Delete)
✅ Cart Management
✅ Order Management
✅ RESTful API architecture
✅ MySQL database integration

---

## 🗄️ Database Configuration

Create MySQL database:

```
CREATE DATABASE globalmart;
```

Update your `application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/globalmart
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

---

## ▶️ How to Run the Project

### Step 1: Clone repository

```
git clone https://github.com/PeetlaMahesh123/globalmart-backend.git
```

### Step 2: Go to project folder

```
cd globalmart-backend
```

### Step 3: Run application

Using Maven:

```
mvn spring-boot:run
```

OR

```
./mvnw spring-boot:run
```

---

## 🌐 Server Runs On

```
http://localhost:8080
```

---

## 📡 Example API Endpoints

### User APIs

```
POST /api/users/register
POST /api/users/login
GET  /api/users/{id}
```

### Product APIs

```
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

### Cart APIs

```
POST   /api/cart/add
GET    /api/cart/{userId}
DELETE /api/cart/remove/{cartId}
```

### Order APIs

```
POST /api/orders
GET  /api/orders/{userId}
```

---

## 🧪 Testing APIs

Use tools like:

* Postman
* Thunder Client
* Frontend React application

---

## 🔒 Security Notes

Do NOT commit:

```
application.properties
.env
target/
```

Use `.gitignore` to protect sensitive data.

---

## 👨‍💻 Author

Mahesh Peetla
Java Full Stack Developer

---

## 📄 License

This project is for educational and learning purposes.
