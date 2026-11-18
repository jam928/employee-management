CREATE TABLE IF NOT EXISTS employee (
    id            BIGINT       NOT NULL,
    email         VARCHAR(255) DEFAULT NULL,
    employee_code VARCHAR(255) NOT NULL,
    image_url     VARCHAR(255) DEFAULT NULL,
    job_title     VARCHAR(255) DEFAULT NULL,
    name          VARCHAR(255) DEFAULT NULL,
    phone         VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- Employee.id uses GenerationType.AUTO, which Hibernate implements on MySQL with this table
CREATE TABLE IF NOT EXISTS hibernate_sequence (
    next_val BIGINT
) ENGINE = InnoDB;

INSERT INTO hibernate_sequence (next_val) VALUES (1);
