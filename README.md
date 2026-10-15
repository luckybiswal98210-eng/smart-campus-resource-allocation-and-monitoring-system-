# 🏫 Smart Campus Resource Allocation and Monitoring System

**Java Microproject (Semester 5)**  
**Repository:** [https://github.com/luckybiswal98210-eng/smart-campus-resource-allocation-and-monitoring-system-](https://github.com/luckybiswal98210-eng/smart-campus-resource-allocation-and-monitoring-system-)

---

## 📖 1. Project Overview
The **Smart Campus Resource Allocation and Monitoring System** is a robust Java SE application designed to centralize and automate the reservation, conflict prevention, and real-time operational monitoring of academic assets across a modern university or college campus.

### Key Campus Assets Managed:
- **Smart Classrooms & Lecture Theatres** (Interactive Digital Whiteboards, Lecture capture)
- **Specialized Research Laboratories** (AI & Robotics Lab, IoT Lab, Cloud Computing Lab)
- **Auditoriums & Seminar Halls** (APJ Abdul Kalam Auditorium with central HVAC & AV)
- **High-Demand Equipment & Utilities** (4K Mobile Projectors, EV Campus Shuttle Charging Stations)

---

## 💡 2. Core Features & Capabilities

1. **Role-Based Access Control (RBAC):**
   - **Admin / Facility Manager:** Add, edit, or decommission campus facilities; view cross-campus analytics; oversee maintenance alerts.
   - **Faculty:** Search available labs/classrooms and book time slots for lectures, exams, and workshops.
   - **Student Coordinator:** Request spaces for club activities, hackathons, and technical symposiums.

2. **Automated Conflict Resolution Engine:**
   - Detects scheduling overlaps mathematically using SQL and Java logic:
     $$\text{Conflict} \iff (\text{Existing Start} < \text{Requested End}) \land (\text{Existing End} > \text{Requested Start})$$
   - Prevents double bookings and displays informative notices to users.

3. **Multithreaded IoT Telemetry & Energy Monitoring:**
   - Employs a background daemon thread (`SensorSimulator.java`) that simulates real-time IoT campus sensor telemetry (occupancy counters, power draw in kW, room temperature).
   - **Eco-Audit Policy:** Automated monitoring flagging energy wastage (e.g. AC running in empty classrooms).
   - Auto-updates the UI smoothly via `SwingUtilities.invokeLater()`.

4. **Dual-Mode JDBC Database Architecture:**
   - **Primary:** **MySQL Server** (`smart_campus_db` with full relational schema, foreign key cascades, and indexing).
   - **Resilient Fallback:** Automatically switches to an offline local SQLite database if MySQL server is stopped, ensuring **zero presentation crashes** during college viva!
   - Dynamic in-app **Database Settings Dialog** to test credentials and reconnect anytime.

5. **Analytics & Automated Audit Reporting:**
   - Utilization breakdown by category.
   - Total real-time campus load (kW) and live student count.
   - Single-click export of **Campus Audit Reports** (`.txt` formatted report).

---

## 🏗️ 3. Project Architecture

The system strictly follows the **Model-View-Controller (MVC) + DAO** pattern:

```text
com.smartcampus/
├── Main.java                        # Entry point & startup initialization
├── config/
│   └── DatabaseConnection.java      # Singleton JDBC connection manager (MySQL + Fallback)
├── model/
│   ├── User.java                    # User model with role permissions
│   ├── Resource.java                # Campus asset model
│   ├── Allocation.java              # Reservation & booking record
│   ├── Telemetry.java               # Real-time IoT sensor telemetry
│   └── MaintenanceAlert.java        # Hardware & equipment ticket
├── dao/
│   ├── UserDAO.java                 # User authentication & credentials
│   ├── ResourceDAO.java             # CRUD queries for campus assets
│   ├── AllocationDAO.java           # Booking queries & conflict checking
│   ├── TelemetryDAO.java            # Real-time sensor log queries
│   └── MaintenanceAlertDAO.java     # Facility ticket logging
├── service/
│   ├── AllocationService.java       # Scheduling business logic & conflict verification
│   └── SensorSimulator.java         # Multithreaded background IoT engine
└── ui/
    ├── UITheme.java                 # Modern Flat Look-and-Feel & styling tokens
    ├── DatabaseConfigDialog.java    # MySQL connection configuration modal
    ├── LoginFrame.java              # Sleek login with quick demo credentials
    ├── MainDashboardFrame.java      # Main navigation shell with live digital clock
    ├── ResourcePanel.java           # Inventory table, search, filters & CRUD
    ├── AllocationPanel.java         # Reservation booking modal & release actions
    ├── LiveMonitoringPanel.java     # Real-time IoT dashboard & eco-audit
    ├── MaintenanceAlertPanel.java   # Ticket management
    └── AnalyticsPanel.java          # KPIs, progress indicators & report export
```

---

## 🗄️ 4. MySQL Database Setup

### Step 1: Start MySQL Server
Ensure MySQL server is running (e.g., via Terminal, XAMPP, or MySQL Workbench):
```bash
# On macOS (Homebrew):
brew services start mysql

# Or via XAMPP Control Panel
```

### Step 2: Import Schema
Execute the SQL script located at `database/schema.sql`:
```bash
mysql -u root -p < database/schema.sql
```
*(Note: If you run the app directly, the system will also automatically create the database and tables on startup if they don't exist!)*

### Step 3: Database Configuration (`database/db.properties`)
```properties
db.type=mysql
db.host=localhost
db.port=3306
db.name=smart_campus_db
db.user=root
db.password=your_password_here
```

---

## 🔑 5. Pre-Configured Demo Credentials

| Role | Username | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **Campus Admin** | `admin` | `admin123` | Full administrative control (CRUD resources, all bookings, alerts) |
| **Faculty Member** | `sharma` | `sharma123` | Academic booking, release facilities, view status |
| **Student Coordinator** | `lucky` | `lucky123` | Event allocation requests, view my bookings |

*(Use the **Quick Demo Login** buttons on the login screen for instant 1-click access!)*

---

## 🚀 6. How to Compile and Run

### Option A: Using Provided Scripts (Recommended)
```bash
# 1. Compile the Java files
./compile.sh

# 2. Launch the Application
./run.sh
```

### Option B: Manual Command Line
```bash
# Compile
javac -cp "lib/*:." -d bin src/com/smartcampus/*.java src/com/smartcampus/*/*.java

# Run
java -cp "bin:lib/*:." com.smartcampus.Main
```

---

## 🎓 7. Academic Viva & Evaluation Notes

### Q1: Which Java concepts are implemented in this microproject?
- **OOP Concepts:** Encapsulation (POJO models), Inheritance (Swing containers, Thread execution), Abstraction (DAO pattern), Polymorphism.
- **JDBC API:** `DriverManager`, `Connection`, `PreparedStatement` (SQL injection prevention), `ResultSet`, Transaction isolation.
- **Multithreading:** `SensorSimulator` implements `Runnable` to run continuous background IoT telemetry without freezing the Swing Event Dispatch Thread (EDT).
- **Collections Framework:** `List<Resource>`, `Map<String, Integer>`, and sorting comparators.
- **GUI & Event-Driven Architecture:** Modern Swing components, `ActionListener`, `ChangeListener`, and modal dialogs.

### Q2: How does the system handle booking conflicts?
`AllocationService` checks for time overlaps in the database:
```sql
SELECT COUNT(*) FROM allocations 
WHERE resource_id = ? 
  AND booking_date = ? 
  AND status IN ('APPROVED', 'ACTIVE') 
  AND (start_time < ? AND end_time > ?)
```
If the count $> 0$, the request is rejected with a clear explanation.

### Q3: What happens if MySQL is not running on the examiner's computer?
`DatabaseConnection` includes an automatic fallback to local SQLite storage (`smart_campus_local.db`). The app runs smoothly without crashing, and a yellow indicator informs the user of local mode.
