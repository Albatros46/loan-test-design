CREATE TABLE customer (
    id             INT PRIMARY KEY,
    name           VARCHAR(100)  NOT NULL,
    age            INT           NOT NULL,
    credit_score   INT           NOT NULL,
    monthly_income DECIMAL(10,2) NOT NULL,
    has_default    BOOLEAN       NOT NULL
);

CREATE TABLE loan_application (
    id          INT PRIMARY KEY,
    customer_id INT           NOT NULL REFERENCES customer(id),
    amount      DECIMAL(10,2) NOT NULL,
    status      VARCHAR(20)   NOT NULL
);