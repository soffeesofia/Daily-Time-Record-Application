# RMI Daily Time Record Application

A client-server attendance tracking application built with Java RMI (Remote Method Invocation), allowing employees to log their daily time records and enabling admins to monitor employee status and generate attendance reports.

Developed as a Midterm Group Project for PProgramming 3 (CS 222) at Saint Louis University, Baguio City.

---

## Team Members

- Arellano, Mark Gian
- Balagtey, Gregg Andres
- Bosaing, Ryeth Ezryhee
- Chegyem, Roger
- Marquez, John
- Surro, Jaymee Sofia

**Instructor:** Roderick Makil  

---

## Overview

The application follows a client-server architecture over Java RMI. Employees use the client application to time in and out throughout the day, while the admin panel manages registration approvals, monitors employee status, and generates reports over a specified date range. All data is persisted in JSON files on the server.

---

## Features

### Client (Employee)

- **Registration and Login** — Employees register with a username and password, then log in to access the application
- **Time In / Time Out** — Employees can record their time in and time out; the relevant button is disabled based on current status (working or on break) to prevent invalid entries
- **Daily Summary** — Employees can view their own time records for the day, showing Date, Time In, and Time Out per entry

### Server (Admin)

- **Admin Login** — The admin panel is protected by a login page
- **Registration Approval** — The admin can accept or reject registration requests from employees; duplicate usernames against confirmed users are not permitted
- **Employee Status Monitoring** — The admin can view the current status of all employees in real time
- **Report Generation** — The admin can generate a report of accumulated hours for all employees within a specified date range; each employee's full breakdown of time logs is shown before their total hours rendered

---

## Data Storage

All data is stored in JSON files managed by the server:

| Data | Storage |
|---|---|
| Employee credentials | JSON file |
| Pending registration requests | JSON file |
| Time records / attendance logs | JSON file |

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| GUI | Java Swing / AWT |
| Networking | Java RMI |
| Data Storage | JSON files |
| Architecture | Client-Server (RMI) |

---

## License

This project was created for academic purposes at Saint Louis University. All rights reserved by the respective authors.