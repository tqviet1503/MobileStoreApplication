# 📱 Cellphone Store Management Application

A mobile-based application developed in **Java with Android Studio** to manage phone brands, products, customers, orders, and sales reports. The system uses a relational database (SQLite) to ensure efficient data handling and integrity.

## 🔑 Features

- 🏪 Manage phone brands
- 📱 Add and update phone models
- 👥 Track customer info and order history
- 🧾 Create and manage invoices
- 📊 Generate revenue and sales reports

## 🗃️ Database Schema

**Main Tables:**
- `BRANDS`: Brand info
- `PHONES`: Phone details
- `CUSTOMERS`: Customer profiles
- `BILLS`: Order records
- `BILL_DETAILS`: Items per invoice
- `PROFIT`: Sales summary

**Relationships:**
- One brand ➝ many phones  
- One customer ➝ many bills  
- One bill ➝ many bill details  
- One phone ➝ many bill details

## ⚙️ Technologies

- **Language:** Java  
- **Platform:** Android Studio  
- **Database:** SQLite 
