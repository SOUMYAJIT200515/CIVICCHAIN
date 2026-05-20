CREATE DATABASE civicpulse;
USE civicpulse;
CREATE TABLE candidate (
  id BIGINT AUTO_INCREMENT,
  name VARCHAR(255),
  party VARCHAR(255),
  vote_count INT DEFAULT 0,
  PRIMARY KEY (id)
);