-- Sample products for Dermanet e-commerce

-- Jackets
INSERT INTO products (name, description, price, image, category, is_featured, created_at, updated_at) VALUES
('Leather Jacket', 'Premium leather jacket with zipper', 129.99, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500', 'jackets', true, NOW(), NOW()),
('Denim Jacket', 'Classic blue denim jacket', 79.99, 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500', 'jackets', false, NOW(), NOW()),
('Bomber Jacket', 'Stylish bomber jacket for all seasons', 99.99, 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=500', 'jackets', true, NOW(), NOW());

-- T-shirts
INSERT INTO products (name, description, price, image, category, is_featured, created_at, updated_at) VALUES
('Cotton T-Shirt', 'Comfortable cotton t-shirt', 24.99, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500', 't-shirts', true, NOW(), NOW()),
('Graphic Tee', 'Cool graphic design t-shirt', 29.99, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500', 't-shirts', false, NOW(), NOW()),
('V-Neck Shirt', 'Classic v-neck t-shirt', 19.99, 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500', 't-shirts', false, NOW(), NOW());

-- Jeans
INSERT INTO products (name, description, price, image, category, is_featured, created_at, updated_at) VALUES
('Slim Fit Jeans', 'Modern slim fit denim jeans', 69.99, 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=500', 'jeans', true, NOW(), NOW()),
('Straight Leg Jeans', 'Classic straight leg jeans', 59.99, 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500', 'jeans', false, NOW(), NOW()),
('Ripped Jeans', 'Trendy ripped denim jeans', 74.99, 'https://images.unsplash.com/photo-1475178626620-a4d074967452?w=500', 'jeans', false, NOW(), NOW());

-- Shoes
INSERT INTO products (name, description, price, image, category, is_featured, created_at, updated_at) VALUES
('Running Shoes', 'Comfortable running shoes', 89.99, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500', 'shoes', true, NOW(), NOW()),
('Casual Sneakers', 'Stylish casual sneakers', 79.99, 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500', 'shoes', false, NOW(), NOW()),
('Leather Boots', 'Premium leather boots', 149.99, 'https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=500', 'shoes', false, NOW(), NOW());

-- Bags
INSERT INTO products (name, description, price, image, category, is_featured, created_at, updated_at) VALUES
('Backpack', 'Spacious travel backpack', 49.99, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500', 'bags', false, NOW(), NOW()),
('Leather Bag', 'Elegant leather handbag', 119.99, 'https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=500', 'bags', true, NOW(), NOW()),
('Messenger Bag', 'Professional messenger bag', 69.99, 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=500', 'bags', false, NOW(), NOW());

-- Glasses
INSERT INTO products (name, description, price, image, category, is_featured, created_at, updated_at) VALUES
('Sunglasses', 'UV protection sunglasses', 39.99, 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500', 'glasses', false, NOW(), NOW()),
('Reading Glasses', 'Stylish reading glasses', 29.99, 'https://images.unsplash.com/photo-1574258495973-f010dfbb5371?w=500', 'glasses', false, NOW(), NOW()),
('Sports Glasses', 'Durable sports eyewear', 49.99, 'https://images.unsplash.com/photo-1577803645773-f96470509666?w=500', 'glasses', false, NOW(), NOW());

-- Suits
INSERT INTO products (name, description, price, image, category, is_featured, created_at, updated_at) VALUES
('Business Suit', 'Professional business suit', 299.99, 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=500', 'suits', true, NOW(), NOW()),
('Tuxedo', 'Elegant black tuxedo', 399.99, 'https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=500', 'suits', false, NOW(), NOW());
