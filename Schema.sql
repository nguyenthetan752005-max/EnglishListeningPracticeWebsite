DROP DATABASE IF EXISTS english_learning_db;
CREATE DATABASE english_learning_db;
USE english_learning_db;

SET FOREIGN_KEY_CHECKS=0;

-- ========================================
-- USERS
-- ========================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','USER') NOT NULL,
  is_active BIT(1) NOT NULL,
  is_deleted BIT(1) NOT NULL,
  avatar_url VARCHAR(255),
  avatar_public_id VARCHAR(255),
  provider VARCHAR(255),
  provider_id VARCHAR(255),
  total_active_time INT,
  active_time_7d INT,
  active_time_30d INT,
  created_at DATETIME(6)
);

-- ========================================
-- APP SETTINGS
-- ========================================
DROP TABLE IF EXISTS app_settings;
CREATE TABLE app_settings (
  id BIGINT PRIMARY KEY,
  site_name VARCHAR(150) NOT NULL,
  seo_meta_description VARCHAR(500) NOT NULL,
  max_recent_users_on_dashboard INT NOT NULL,
  speaking_pass_threshold INT NOT NULL,
  allow_user_registration BIT(1) NOT NULL
);

-- ========================================
-- CATEGORIES
-- ========================================
DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(255),
  image_url VARCHAR(255),
  cloud_image_id VARCHAR(255),
  level_range VARCHAR(255),
  type ENUM('AUDIO','VIDEO'),
  practice_type ENUM('LISTENING','SPEAKING'),
  total_lessons INT DEFAULT 0,
  order_index INT,
  status ENUM('ARCHIVED','DRAFT','PUBLISHED') NOT NULL,
  is_deleted BIT(1) NOT NULL
);

-- ========================================
-- SECTIONS
-- ========================================
DROP TABLE IF EXISTS sections;
CREATE TABLE sections (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(255),
  category_id BIGINT,
  order_index INT,
  status ENUM('ARCHIVED','DRAFT','PUBLISHED') NOT NULL,
  is_deleted BIT(1) NOT NULL,
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- ========================================
-- LESSONS
-- ========================================
DROP TABLE IF EXISTS lessons;
CREATE TABLE lessons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255),
  level VARCHAR(255),
  youtube_video_id VARCHAR(255),
  total_sentences INT,
  section_id BIGINT,
  order_index INT,
  status ENUM('ARCHIVED','DRAFT','PUBLISHED') NOT NULL,
  is_deleted BIT(1) NOT NULL,
  FOREIGN KEY (section_id) REFERENCES sections(id)
);

-- ========================================
-- SENTENCES
-- ========================================
DROP TABLE IF EXISTS sentences;
CREATE TABLE sentences (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content TEXT,
  audio_url VARCHAR(255),
  cloud_audio_id VARCHAR(255),
  start_time DOUBLE,
  end_time DOUBLE,
  order_index INT,
  lesson_id BIGINT,
  status ENUM('ARCHIVED','DRAFT','PUBLISHED') NOT NULL,
  is_deleted BIT(1) NOT NULL,
  FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- ========================================
-- COMMENTS
-- ========================================
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content TEXT NOT NULL,
  created_at DATETIME(6),
  parent_id BIGINT,
  sentence_id BIGINT,
  user_id BIGINT,
  is_deleted BIT(1) NOT NULL,
  is_hidden BIT(1) NOT NULL,
  FOREIGN KEY (parent_id) REFERENCES comments(id),
  FOREIGN KEY (sentence_id) REFERENCES sentences(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ========================================
-- COMMENT VOTES
-- ========================================
DROP TABLE IF EXISTS comment_votes;
CREATE TABLE comment_votes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  comment_id BIGINT,
  user_id BIGINT,
  is_like BIT(1) NOT NULL,
  created_at DATETIME(6),
  UNIQUE (comment_id, user_id),
  FOREIGN KEY (comment_id) REFERENCES comments(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ========================================
-- USER PROGRESS
-- ========================================
DROP TABLE IF EXISTS user_progress;
CREATE TABLE user_progress (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  sentence_id BIGINT,
  status ENUM('COMPLETED','IN_PROGRESS','SKIPPED'),
  last_accessed DATETIME(6),
  UNIQUE (user_id, sentence_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (sentence_id) REFERENCES sentences(id)
);

-- ========================================
-- DAILY STUDY STATISTICS
-- ========================================
DROP TABLE IF EXISTS daily_study_statistics;
CREATE TABLE daily_study_statistics (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  study_date DATE NOT NULL,
  active_time_seconds INT NOT NULL,
  UNIQUE (user_id, study_date),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ========================================
-- SPEAKING RESULTS
-- ========================================
DROP TABLE IF EXISTS speaking_results;
CREATE TABLE speaking_results (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  sentence_id BIGINT,
  score INT,
  accuracy DOUBLE,
  recognized_text VARCHAR(255),
  feedback TEXT,
  result_type ENUM('BEST','CURRENT'),
  user_audio_url VARCHAR(255),
  user_audio_public_id VARCHAR(255),
  created_at DATETIME(6),
  updated_at DATETIME(6),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (sentence_id) REFERENCES sentences(id)
);

-- ========================================
-- PASSWORD RESET TOKEN
-- ========================================
DROP TABLE IF EXISTS password_reset_token;
CREATE TABLE password_reset_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expiry_date DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ========================================
-- SLIDESHOWS
-- ========================================
DROP TABLE IF EXISTS slideshows;
CREATE TABLE slideshows (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255),
  image_url VARCHAR(255) NOT NULL,
  cloud_image_id VARCHAR(255),
  link_url VARCHAR(255),
  display_order INT DEFAULT 0,
  position ENUM('HOME') DEFAULT 'HOME',
  is_active TINYINT(1) DEFAULT 1,
  is_deleted BIT(1) NOT NULL,
  created_at DATETIME,
  updated_at DATETIME
);

SET FOREIGN_KEY_CHECKS=1;