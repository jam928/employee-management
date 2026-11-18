-- Hibernate 6+ no longer uses the shared hibernate_sequence table for GenerationType.AUTO,
-- so Employee.id now uses GenerationType.IDENTITY backed by MySQL's AUTO_INCREMENT.
ALTER TABLE employee MODIFY id BIGINT NOT NULL AUTO_INCREMENT;

DROP TABLE IF EXISTS hibernate_sequence;
