
DELETE FROM radnom;


ALTER TABLE radnom ALTER COLUMN product_id RESTART WITH 1;

INSERT INTO radnom
(product_name, price, product_date, description, category, image_url, stock, weight, dimensions, brand, rating, review_count)
VALUES
('Laptop Gamingowy', 3999, '2024-01-15', 'Wydajny laptop do gier z procesorem Intel Core i7 i kartą RTX 4060', 'Electronics', '/images/laptop.jpg', 10, 2.4, '35x25x2 cm', 'Lenovo', 4.8, 120);

INSERT INTO radnom
(product_name, price, product_date, description, category, image_url, stock, weight, dimensions, brand, rating, review_count)
VALUES
('Myszka bezprzewodowa', 129, '2024-02-20', 'Cicha i precyzyjna myszka ergonomiczna', 'Electronics', '/images/mouse.jpg', 50, 0.08, '10x6x4 cm', 'Logitech', 4.6, 89);

INSERT INTO radnom
(product_name, price, product_date, description, category, image_url, stock, weight, dimensions, brand, rating, review_count)
VALUES
('Klawiatura mechaniczna', 349, '2024-03-10', 'Klawiatura mechaniczna z przełącznikami blue', 'Electronics', '/images/keyboard.jpg', 25, 1.2, '44x13x4 cm', 'Razer', 4.7, 200);

INSERT INTO radnom
(product_name, price, product_date, description, category, image_url, stock, weight, dimensions, brand, rating, review_count)
VALUES
('Monitor 27 cali', 1299, '2024-01-05', 'Monitor 4K IPS 144Hz', 'Electronics', '/images/monitor.jpg', 8, 5.5, '61x36x5 cm', 'Dell', 4.9, 75);