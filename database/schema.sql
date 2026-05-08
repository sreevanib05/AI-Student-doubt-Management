CREATE DATABASE IF NOT EXISTS doubtflow_ai
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE doubtflow_ai;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mentors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    expertise VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doubts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    subject VARCHAR(80) NOT NULL DEFAULT 'General',
    context_notes TEXT NULL,
    prompt_template TEXT NULL,
    pdf_file_name VARCHAR(255) NULL,
    pdf_content_type VARCHAR(120) NULL,
    pdf_data MEDIUMTEXT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    student_id BIGINT NOT NULL,
    mentor_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,

    CONSTRAINT fk_doubts_student
        FOREIGN KEY (student_id) REFERENCES students(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_doubts_mentor
        FOREIGN KEY (mentor_id) REFERENCES mentors(id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS responses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doubt_id BIGINT NOT NULL,
    mentor_id BIGINT NOT NULL,
    response_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_responses_doubt
        FOREIGN KEY (doubt_id) REFERENCES doubts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_responses_mentor
        FOREIGN KEY (mentor_id) REFERENCES mentors(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_doubts_student ON doubts(student_id);
CREATE INDEX idx_doubts_mentor ON doubts(mentor_id);
CREATE INDEX idx_doubts_category ON doubts(category);
CREATE INDEX idx_doubts_status ON doubts(status);
