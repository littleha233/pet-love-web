INSERT INTO cities(city_code, city_name, is_enabled)
SELECT '310100', '上海', TRUE
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE city_code = '310100');

INSERT INTO cities(city_code, city_name, is_enabled)
SELECT '330100', '杭州', TRUE
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE city_code = '330100');

INSERT INTO cities(city_code, city_name, is_enabled)
SELECT '320100', '南京', TRUE
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE city_code = '320100');

INSERT INTO cities(city_code, city_name, is_enabled)
SELECT '440300', '深圳', TRUE
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE city_code = '440300');
