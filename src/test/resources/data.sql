INSERT INTO customer VALUES (1, 'Ayşe Yılmaz',   34, 720, 3500.00, FALSE);
INSERT INTO customer VALUES (2, 'Mehmet Demir',  45, 610, 1800.00, FALSE);
INSERT INTO customer VALUES (3, 'Elif Kaya',     29, 550, 2800.00, FALSE);
INSERT INTO customer VALUES (4, 'Can Öztürk',    62, 790, 5200.00, TRUE);
INSERT INTO customer VALUES (5, 'Zeynep Arslan', 23, 680, 2100.00, FALSE);

INSERT INTO loan_application VALUES (101, 1, 15000.00, 'APPROVED');
INSERT INTO loan_application VALUES (102, 1,  5000.00, 'PAID_OUT');
INSERT INTO loan_application VALUES (103, 2,  8000.00, 'UNDER_REVIEW');
-- HATALI KAYIT: Elif'in notu 550 ama başvurusu onaylanmış (R4 ihlali)
INSERT INTO loan_application VALUES (104, 3, 12000.00, 'APPROVED');
INSERT INTO loan_application VALUES (105, 4, 20000.00, 'REJECTED');
INSERT INTO loan_application VALUES (106, 5,  3000.00, 'SUBMITTED');
-- HATALI KAYIT: tutar 50.000 sınırının üstünde (R2 ihlali)
INSERT INTO loan_application VALUES (107, 5, 60000.00, 'DRAFT');
INSERT INTO loan_application VALUES (108, 1,  2500.00, 'CANCELLED');
INSERT INTO loan_application VALUES (109, 2, 45000.00, 'SUBMITTED');