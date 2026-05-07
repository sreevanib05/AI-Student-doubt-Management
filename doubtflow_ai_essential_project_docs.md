# DoubtFlow AI — Essential Project Documents

## 1. PROJECT_OVERVIEW.md

```md
# DoubtFlow AI

## Project Description
DoubtFlow AI is a Java-based AI-assisted student doubt management system.

The platform allows:
- Students to post doubts
- Mentors to resolve doubts
- Faculty/Admins to monitor performance
- Automatic doubt categorization
- Duplicate doubt detection
- AI-like FAQ suggestions

This project is built as a dashboard-style web application.

---

# Core Features

## Student Features
- Register/Login
- Submit doubts
- View doubt status
- Get AI-like FAQ suggestions
- Track previous doubts

## Mentor Features
- View assigned doubts
- Respond to doubts
- Update resolution status

## Faculty/Admin Features
- View analytics
- Monitor mentors
- Filter doubts category-wise
- Assign mentors manually

---

# Technologies Used

| Layer | Technology |
|---|---|
| Frontend | React + Tailwind CSS |
| Backend | Spring Boot |
| Database | MySQL |
| Database Access | JDBC |
| Authentication | JWT |
| Build Tool | Maven |

---

# OOP Concepts Used

- Classes
- Objects
- Inheritance
- Interfaces
- Encapsulation
- Polymorphism
- Exception Handling
- Multithreading

---

# Doubt Categories

1. ConceptualDoubt
2. CodingDoubt
3. DebuggingDoubt

---

# Interfaces Used

## Assignable
Used for assigning mentors to doubts.

---

# Exceptions Used

## DuplicateDoubtException
Thrown when a similar doubt already exists.

## InvalidCategoryException
Thrown when category is invalid.

---

# AI-like FAQ Suggestion

The system compares keywords from new doubts with previously solved doubts.

If similarity is found:
- Existing answers are suggested instantly.

---

# Project Goal

To build a production-style academic doubt management dashboard using Java technologies.
```

---

# 2. PROJECT_STRUCTURE.md

```md
# Project Folder Structure

ai-doubt-management-system/
│
├── backend/
│   ├── src/main/java/com/doubtflow/
│   │
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── exception/
│   │   ├── interfaces/
│   │   ├── thread/
│   │   └── config/
│   │
│   └── resources/
│       └── application.properties
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── layouts/
│   │   ├── services/
│   │   └── assets/
│
├── database/
│   └── schema.sql
│
└── docs/
```

---

# 3. DATABASE_SCHEMA.md

```sql
CREATE DATABASE doubtflow_ai;

USE doubtflow_ai;

CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255)
);

CREATE TABLE mentors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    expertise VARCHAR(100)
);

CREATE TABLE doubts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255),
    description TEXT,
    category VARCHAR(50),
    status VARCHAR(50),
    student_id INT,
    mentor_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(student_id) REFERENCES students(id),
    FOREIGN KEY(mentor_id) REFERENCES mentors(id)
);

CREATE TABLE responses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    doubt_id INT,
    mentor_id INT,
    response_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(doubt_id) REFERENCES doubts(id),
    FOREIGN KEY(mentor_id) REFERENCES mentors(id)
);
```

---

# 4. BACKEND_CLASSES.md

```md
# Core Classes

## Student
Represents student users.

Fields:
- id
- name
- email
- password

---

## Mentor
Represents mentors.

Fields:
- id
- name
- expertise

---

## Doubt
Base class for all doubts.

Fields:
- id
- title
- description
- status

Methods:
- assignMentor()
- updateStatus()

---

## ConceptualDoubt
Extends Doubt

---

## CodingDoubt
Extends Doubt

---

## DebuggingDoubt
Extends Doubt

---

## Response
Stores mentor responses.

Fields:
- id
- responseText
- mentorId
- doubtId
```

---

# 5. INTERFACE_AND_EXCEPTIONS.md

```java
package com.doubtflow.interfaces;

import com.doubtflow.model.Mentor;

public interface Assignable {
    void assignMentor(Mentor mentor);
}
```

```java
package com.doubtflow.exception;

public class DuplicateDoubtException extends Exception {

    public DuplicateDoubtException(String message) {
        super(message);
    }
}
```

```java
package com.doubtflow.exception;

public class InvalidCategoryException extends Exception {

    public InvalidCategoryException(String message) {
        super(message);
    }
}
```

---

# 6. BASIC_JAVA_MODELS.md

```java
package com.doubtflow.model;

public class Student {

    private int id;
    private String name;
    private String email;
    private String password;

    public Student() {
    }

    public Student(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

```java
package com.doubtflow.model;

public class Mentor {

    private int id;
    private String name;
    private String expertise;

    public Mentor() {
    }

    public Mentor(int id, String name, String expertise) {
        this.id = id;
        this.name = name;
        this.expertise = expertise;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }
}
```

```java
package com.doubtflow.model;

import com.doubtflow.interfaces.Assignable;

public class Doubt implements Assignable {

    protected int id;
    protected String title;
    protected String description;
    protected String status;
    protected Mentor mentor;

    @Override
    public void assignMentor(Mentor mentor) {
        this.mentor = mentor;
    }
}
```

```java
package com.doubtflow.model;

public class CodingDoubt extends Doubt {

    private String programmingLanguage;

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }
}
```

---

# 7. JDBC_CONNECTION.md

```java
package com.doubtflow.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/doubtflow_ai";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
```

---

# 8. MULTITHREADING.md

```java
package com.doubtflow.thread;

public class MentorThread extends Thread {

    private String mentorName;

    public MentorThread(String mentorName) {
        this.mentorName = mentorName;
    }

    @Override
    public void run() {

        System.out.println(mentorName + " is resolving doubts...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(mentorName + " resolved the doubt.");
    }
}
```

```java
MentorThread mentor1 = new MentorThread("Rahul");
MentorThread mentor2 = new MentorThread("Anjali");

mentor1.start();
mentor2.start();
```

---

# 9. FAQ_ENGINE.md

```java
package com.doubtflow.service;

public class FAQSuggestionService {

    public boolean isSimilar(String oldDoubt, String newDoubt) {

        oldDoubt = oldDoubt.toLowerCase();
        newDoubt = newDoubt.toLowerCase();

        return oldDoubt.contains(newDoubt)
                || newDoubt.contains(oldDoubt);
    }
}
```

---

# 10. FRONTEND_UI_GUIDE.md

```md
# UI Theme

## Primary Colors
- Blue: #2563EB
- White: #FFFFFF
- Light Gray: #F1F5F9

---

# Layout Style

## Sidebar
- Dark blue background
- White text

## Dashboard Cards
- White cards
- Rounded corners
- Soft shadow

## Buttons
- Blue buttons
- White text

---

# Main Pages

## Student Dashboard
- Submit Doubt
- My Doubts
- Suggested FAQs

## Mentor Dashboard
- Assigned Doubts
- Resolve Doubts

## Admin Dashboard
- Analytics
- Mentor Monitoring
- Category Filtering
```

---

# 11. SIMPLE_API_PLAN.md

```md
# Backend APIs

## Student APIs

POST /api/students/register
POST /api/students/login

---

## Doubt APIs

POST /api/doubts/create
GET /api/doubts/all
GET /api/doubts/category/{category}

---

## Mentor APIs

GET /api/mentors/all
POST /api/mentors/respond

---

## Admin APIs

GET /api/admin/analytics
```

---

# 12. BEGINNER_DEVELOPMENT_PLAN.md

```md
# Step-by-Step Beginner Plan

## STEP 1
Create Spring Boot backend.

## STEP 2
Connect MySQL database.

## STEP 3
Create model classes.

## STEP 4
Create JDBC repository classes.

## STEP 5
Create REST APIs.

## STEP 6
Test APIs using Postman.

## STEP 7
Create React frontend.

## STEP 8
Connect frontend with backend.

## STEP 9
Add AI FAQ suggestions.

## STEP 10
Add multithreading.

## STEP 11
Create admin analytics dashboard.
```

---

# FINAL NOTE

These documents are enough to:
- Start the project
- Use Codex agents
- Generate code module-by-module
- Build the backend
- Build the frontend
- Implement JDBC
- Implement multithreading
- Implement AI-like FAQ suggestions

This structure is intentionally beginner-friendly.

