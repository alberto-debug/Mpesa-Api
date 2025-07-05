# 🛒 Supermarket Management System with M-Pesa Integration

A full-stack supermarket system designed for real-time product management and mobile payments via M-Pesa (Vodacom Mozambique). Built with **React** frontend, **Spring Boot** backend, and **MySQL** database.

---

## 📦 Features

### 👨‍💼 Staff Operations
- Secure staff login
- Customer checkout using STK Push (real-time M-Pesa prompt)
- Query transaction status from M-Pesa API

### 🛍️ Product & Stock Management
- Create, read, update, delete products and categories
- Track inventory levels
- Manage discounts and expiry dates

### 💸 M-Pesa Integration
- C2B (Customer to Business) transactions
- STK Push (sim prompt to customer to enter PIN)
- Callback handling to update transaction status
- Secure `.env` configuration

---

## 🛠 Tech Stack

| Layer       | Technology         |
|-------------|--------------------|
| Frontend    | React + TypeScript |
| Backend     | Spring Boot (Java) |
| Database    | MySQL              |
| API         | M-Pesa Business API (Vodacom Mozambique) |
| Auth        | JWT                |
| Deployment  | Docker / Render / Railway (optional) |
| Versioning  | Git + GitHub with Git Flow |

---

## 🧑‍💻 Getting Started

### 🔐 1. Prerequisites
- Node.js and npm
- Java 17+ and Maven
- MySQL Server running locally or remotely
- M-Pesa Business Short Code and API credentials

---

### 🚀 2. Clone the Repo
```bash
git clone git@github.com:alberto-debug/Mpesa-Api.git
cd Mpesa-Ap
