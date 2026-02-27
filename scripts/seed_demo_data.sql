USE pet_platform;
SET NAMES utf8mb4;

SET @admin_id := (SELECT id FROM admin_users ORDER BY id LIMIT 1);

-- 1) 清理旧的 DEMO 数据（按标记删除，避免影响非 DEMO 数据）
DELETE r
FROM complaint_ticket_replies r
JOIN complaint_tickets t ON t.id = r.ticket_id
WHERE t.ticket_no LIKE 'DEMOCT%';

DELETE FROM complaint_tickets WHERE ticket_no LIKE 'DEMOCT%';

DELETE m
FROM rescue_clue_media m
JOIN rescue_clues c ON c.id = m.clue_id
WHERE c.clue_no LIKE 'DEMORC%';

DELETE FROM rescue_clues WHERE clue_no LIKE 'DEMORC%';

DELETE FROM rescue_resources WHERE name LIKE '[DEMO] %';
DELETE FROM rescue_guides WHERE title LIKE '[DEMO] %';

DELETE vm
FROM feeding_visit_media vm
JOIN feeding_order_visits v ON v.id = vm.visit_id
JOIN feeding_orders o ON o.id = v.order_id
WHERE o.order_no LIKE 'DEMOFO%';

DELETE v
FROM feeding_order_visits v
JOIN feeding_orders o ON o.id = v.order_id
WHERE o.order_no LIKE 'DEMOFO%';

DELETE op
FROM feeding_order_pets op
JOIN feeding_orders o ON o.id = op.order_id
WHERE o.order_no LIKE 'DEMOFO%';

DELETE rv
FROM feeding_order_reviews rv
JOIN feeding_orders o ON o.id = rv.order_id
WHERE o.order_no LIKE 'DEMOFO%';

DELETE FROM feeding_orders WHERE order_no LIKE 'DEMOFO%';

DELETE aa
FROM adoption_applications aa
JOIN adoption_posts ap ON ap.id = aa.post_id
WHERE ap.title LIKE '[DEMO] %';

DELETE pm
FROM pet_media pm
JOIN pets p ON p.id = pm.pet_id
WHERE p.name LIKE 'Demo-%';

DELETE FROM adoption_posts WHERE title LIKE '[DEMO] %';
DELETE FROM pets WHERE name LIKE 'Demo-%';

DELETE FROM feeding_provider_profiles
WHERE provider_user_id IN (
  SELECT u.id FROM users u WHERE u.email IN (
    'demo.provider1@petlove.local',
    'demo.provider2@petlove.local',
    'demo.provider3@petlove.local'
  )
);

DELETE uv
FROM user_verifications uv
JOIN users u ON u.id = uv.user_id
WHERE u.email LIKE 'demo.%@petlove.local';

DELETE FROM file_objects WHERE object_key LIKE 'demo/%';

-- 2) 用户与资料
INSERT INTO users (mobile, mobile_verified_at, register_channel, email, login_type, status, last_login_at)
VALUES
('13800138001', NOW(3), 'MOBILE_SMS', 'demo.owner1@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3)),
('13800138002', NOW(3), 'MOBILE_SMS', 'demo.owner2@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3)),
('13800138003', NOW(3), 'MOBILE_SMS', 'demo.owner3@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3)),
('13800138004', NOW(3), 'MOBILE_SMS', 'demo.provider1@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3)),
('13800138005', NOW(3), 'MOBILE_SMS', 'demo.provider2@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3)),
('13800138006', NOW(3), 'MOBILE_SMS', 'demo.provider3@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3)),
('13800138007', NOW(3), 'MOBILE_SMS', 'demo.reporter1@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3)),
('13800138008', NOW(3), 'MOBILE_SMS', 'demo.reporter2@petlove.local', 'MOBILE_OTP', 'ACTIVE', NOW(3))
ON DUPLICATE KEY UPDATE
  mobile_verified_at = VALUES(mobile_verified_at),
  register_channel = VALUES(register_channel),
  login_type = VALUES(login_type),
  status = VALUES(status),
  last_login_at = VALUES(last_login_at),
  updated_at = NOW(3);

SET @u_owner1 := (SELECT id FROM users WHERE email='demo.owner1@petlove.local');
SET @u_owner2 := (SELECT id FROM users WHERE email='demo.owner2@petlove.local');
SET @u_owner3 := (SELECT id FROM users WHERE email='demo.owner3@petlove.local');
SET @u_provider1 := (SELECT id FROM users WHERE email='demo.provider1@petlove.local');
SET @u_provider2 := (SELECT id FROM users WHERE email='demo.provider2@petlove.local');
SET @u_provider3 := (SELECT id FROM users WHERE email='demo.provider3@petlove.local');
SET @u_reporter1 := (SELECT id FROM users WHERE email='demo.reporter1@petlove.local');
SET @u_reporter2 := (SELECT id FROM users WHERE email='demo.reporter2@petlove.local');

INSERT INTO user_profiles (user_id, nickname, avatar_url, city_code, city_name, bio, is_real_name_verified, is_provider_verified)
VALUES
(@u_owner1, '林小鱼', 'https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&w=300', '310100', '上海', '家有两只猫，愿意长期回访。', 1, 0),
(@u_owner2, '周木木', 'https://images.pexels.com/photos/733872/pexels-photo-733872.jpeg?auto=compress&cs=tinysrgb&w=300', '330100', '杭州', '偏好领养成年犬，重视科学喂养。', 1, 0),
(@u_owner3, '顾晨', 'https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=300', '320100', '南京', '有多年救助经验，支持同城领养。', 1, 0),
(@u_provider1, '晴晴上门照护', 'https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=300', '310100', '上海', '猫咪照护 120+ 单，照片反馈详细。', 1, 1),
(@u_provider2, '阿泽宠护', 'https://images.pexels.com/photos/91227/pexels-photo-91227.jpeg?auto=compress&cs=tinysrgb&w=300', '330100', '杭州', '擅长胆小狗狗的慢节奏陪护。', 1, 1),
(@u_provider3, 'Luna 宠护', 'https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&w=300', '440300', '深圳', '多宠家庭上门经验丰富。', 1, 1),
(@u_reporter1, '陆然', 'https://images.pexels.com/photos/614810/pexels-photo-614810.jpeg?auto=compress&cs=tinysrgb&w=300', '310100', '上海', '日常关注流浪动物救助。', 1, 0),
(@u_reporter2, '陈一', 'https://images.pexels.com/photos/2379005/pexels-photo-2379005.jpeg?auto=compress&cs=tinysrgb&w=300', '440300', '深圳', '社区志愿者。', 1, 0)
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  avatar_url = VALUES(avatar_url),
  city_code = VALUES(city_code),
  city_name = VALUES(city_name),
  bio = VALUES(bio),
  is_real_name_verified = VALUES(is_real_name_verified),
  is_provider_verified = VALUES(is_provider_verified),
  updated_at = NOW(3);

INSERT INTO user_verifications (user_id, verification_type, status, submit_version, real_name, reviewed_by_admin_id, reviewed_at)
VALUES
(@u_owner1, 'REAL_NAME', 'APPROVED', 1, '林小鱼', @admin_id, NOW(3)),
(@u_owner2, 'REAL_NAME', 'APPROVED', 1, '周木木', @admin_id, NOW(3)),
(@u_owner3, 'REAL_NAME', 'APPROVED', 1, '顾晨', @admin_id, NOW(3)),
(@u_provider1, 'REAL_NAME', 'APPROVED', 1, '陈晴', @admin_id, NOW(3)),
(@u_provider1, 'PROVIDER', 'APPROVED', 1, NULL, @admin_id, NOW(3)),
(@u_provider2, 'REAL_NAME', 'APPROVED', 1, '李泽', @admin_id, NOW(3)),
(@u_provider2, 'PROVIDER', 'APPROVED', 1, NULL, @admin_id, NOW(3)),
(@u_provider3, 'REAL_NAME', 'APPROVED', 1, '刘璐', @admin_id, NOW(3)),
(@u_provider3, 'PROVIDER', 'APPROVED', 1, NULL, @admin_id, NOW(3))
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  reviewed_by_admin_id = VALUES(reviewed_by_admin_id),
  reviewed_at = VALUES(reviewed_at),
  updated_at = NOW(3);

-- 3) 文件与领养数据
INSERT INTO file_objects (owner_user_id, bucket, object_key, biz_type, file_name, mime_type, file_size, status, public_url)
VALUES
(@u_owner1, 'petlove-public', 'demo/pets/mili-1.jpg', 'PET_MEDIA', 'mili-1.jpg', 'image/jpeg', 235000, 'READY', 'https://images.pexels.com/photos/1056251/pexels-photo-1056251.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_owner1, 'petlove-public', 'demo/pets/mili-2.jpg', 'PET_MEDIA', 'mili-2.jpg', 'image/jpeg', 210000, 'READY', 'https://images.pexels.com/photos/1741235/pexels-photo-1741235.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_owner2, 'petlove-public', 'demo/pets/doubao-1.jpg', 'PET_MEDIA', 'doubao-1.jpg', 'image/jpeg', 240000, 'READY', 'https://images.pexels.com/photos/551628/pexels-photo-551628.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_owner2, 'petlove-public', 'demo/pets/doubao-2.jpg', 'PET_MEDIA', 'doubao-2.jpg', 'image/jpeg', 260000, 'READY', 'https://images.pexels.com/photos/733416/pexels-photo-733416.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_owner3, 'petlove-public', 'demo/pets/xiaoyun-1.jpg', 'PET_MEDIA', 'xiaoyun-1.jpg', 'image/jpeg', 220000, 'READY', 'https://images.pexels.com/photos/127028/pexels-photo-127028.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_owner3, 'petlove-public', 'demo/pets/naitang-1.jpg', 'PET_MEDIA', 'naitang-1.jpg', 'image/jpeg', 210000, 'READY', 'https://images.pexels.com/photos/730896/pexels-photo-730896.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_owner1, 'petlove-public', 'demo/pets/aqi-1.jpg', 'PET_MEDIA', 'aqi-1.jpg', 'image/jpeg', 215000, 'READY', 'https://images.pexels.com/photos/733416/pexels-photo-733416.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_owner2, 'petlove-public', 'demo/pets/tuanzi-1.jpg', 'PET_MEDIA', 'tuanzi-1.jpg', 'image/jpeg', 225000, 'READY', 'https://images.pexels.com/photos/730896/pexels-photo-730896.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_reporter1, 'petlove-public', 'demo/rescue/clue-1.jpg', 'RESCUE_CLUE', 'clue-1.jpg', 'image/jpeg', 198000, 'READY', 'https://images.pexels.com/photos/4587994/pexels-photo-4587994.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_reporter2, 'petlove-public', 'demo/rescue/clue-2.jpg', 'RESCUE_CLUE', 'clue-2.jpg', 'image/jpeg', 205000, 'READY', 'https://images.pexels.com/photos/5732473/pexels-photo-5732473.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_provider1, 'petlove-public', 'demo/feeding/visit-1.jpg', 'FEEDING_LOG', 'visit-1.jpg', 'image/jpeg', 180000, 'READY', 'https://images.pexels.com/photos/4587999/pexels-photo-4587999.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_provider2, 'petlove-public', 'demo/feeding/visit-2.jpg', 'FEEDING_LOG', 'visit-2.jpg', 'image/jpeg', 188000, 'READY', 'https://images.pexels.com/photos/843467/pexels-photo-843467.jpeg?auto=compress&cs=tinysrgb&w=1200'),
(@u_reporter1, 'petlove-public', 'demo/complaint/evidence-1.jpg', 'COMPLAINT_EVIDENCE', 'evidence-1.jpg', 'image/jpeg', 176000, 'READY', 'https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=1200')
ON DUPLICATE KEY UPDATE
  owner_user_id = VALUES(owner_user_id),
  public_url = VALUES(public_url),
  status = VALUES(status),
  updated_at = NOW(3);

INSERT INTO pets (owner_user_id, pet_type, name, gender, age_months, breed, weight_kg, neutered_status, vaccinated_status, health_note, temperament_tags, special_care_note)
VALUES
(@u_owner1, 'CAT', 'Demo-米粒', 'FEMALE', 24, '英短', 4.10, 'YES', 'YES', '体检正常，食欲稳定。', JSON_ARRAY('亲人','安静','同城优先'), '建议继续湿粮+冻干混喂。'),
(@u_owner2, 'DOG', 'Demo-豆包', 'MALE', 18, '中华田园犬', 12.50, 'YES', 'YES', '有轻微分离焦虑。', JSON_ARRAY('活泼','可外出','亲人'), '需每日两次遛狗。'),
(@u_owner3, 'CAT', 'Demo-小云', 'FEMALE', 36, '橘猫', 3.90, 'YES', 'YES', '慢性鼻炎已稳定。', JSON_ARRAY('温和','熟悉猫砂盆'), '换季注意保暖。'),
(@u_owner3, 'CAT', 'Demo-奶糖', 'FEMALE', 8, '布偶', 2.80, 'NO', 'PARTIAL', '幼猫阶段，需补充营养。', JSON_ARRAY('幼猫','亲人','活泼'), '需固定喂养频次。'),
(@u_owner1, 'DOG', 'Demo-阿七', 'MALE', 48, '柯基', 13.20, 'YES', 'YES', '运动需求较高。', JSON_ARRAY('稳定','亲人','需运动'), '建议早晚外出散步。'),
(@u_owner2, 'CAT', 'Demo-团子', 'UNKNOWN', 20, '奶牛猫', 4.00, 'UNKNOWN', 'YES', '怕生，适应慢。', JSON_ARRAY('谨慎','需耐心'), '建议先隔离再合笼。')
;

SET @pet_mili := (SELECT id FROM pets WHERE name='Demo-米粒' ORDER BY id DESC LIMIT 1);
SET @pet_doubao := (SELECT id FROM pets WHERE name='Demo-豆包' ORDER BY id DESC LIMIT 1);
SET @pet_xiaoyun := (SELECT id FROM pets WHERE name='Demo-小云' ORDER BY id DESC LIMIT 1);
SET @pet_naitang := (SELECT id FROM pets WHERE name='Demo-奶糖' ORDER BY id DESC LIMIT 1);
SET @pet_aqi := (SELECT id FROM pets WHERE name='Demo-阿七' ORDER BY id DESC LIMIT 1);
SET @pet_tuanzi := (SELECT id FROM pets WHERE name='Demo-团子' ORDER BY id DESC LIMIT 1);

INSERT INTO adoption_posts (publisher_user_id, pet_id, title, content, city_code, city_name, district_name, status, submit_version, reviewed_by_admin_id, reviewed_at, published_at, view_count)
VALUES
(@u_owner1, @pet_mili, '[DEMO] 米粒找新家（上海）', '米粒亲人安静，已绝育，适合有稳定作息的家庭。希望领养人可接受定期回访。', '310100', '上海', '徐汇区', 'PUBLISHED', 1, @admin_id, NOW(3) - INTERVAL 10 DAY, NOW(3) - INTERVAL 9 DAY, 126),
(@u_owner2, @pet_doubao, '[DEMO] 豆包等待领养（杭州）', '豆包活泼但不拆家，熟悉牵引。希望有遛狗条件、可长期陪伴。', '330100', '杭州', '滨江区', 'PUBLISHED', 1, @admin_id, NOW(3) - INTERVAL 8 DAY, NOW(3) - INTERVAL 7 DAY, 98),
(@u_owner3, @pet_xiaoyun, '[DEMO] 小云寻稳定家庭（南京）', '小云温和，适应期稍慢。适合有养猫经验的家庭。', '320100', '南京', '鼓楼区', 'PUBLISHED', 1, @admin_id, NOW(3) - INTERVAL 6 DAY, NOW(3) - INTERVAL 5 DAY, 76),
(@u_owner3, @pet_naitang, '[DEMO] 奶糖幼猫送养（深圳）', '奶糖 8 个月，已做基础驱虫，期待认真负责的领养人。', '440300', '深圳', '南山区', 'PUBLISHED', 1, @admin_id, NOW(3) - INTERVAL 4 DAY, NOW(3) - INTERVAL 3 DAY, 54),
(@u_owner1, @pet_aqi, '[DEMO] 阿七送养（上海）', '阿七运动需求高，建议有遛狗习惯家庭。', '310100', '上海', '浦东新区', 'PUBLISHED', 1, @admin_id, NOW(3) - INTERVAL 2 DAY, NOW(3) - INTERVAL 2 DAY, 43),
(@u_owner2, @pet_tuanzi, '[DEMO] 团子待领养（杭州）', '团子怕生，需要耐心适应新环境。', '330100', '杭州', '余杭区', 'PUBLISHED', 1, @admin_id, NOW(3) - INTERVAL 1 DAY, NOW(3) - INTERVAL 1 DAY, 35)
;

SET @post_mili := (SELECT id FROM adoption_posts WHERE title='[DEMO] 米粒找新家（上海）' ORDER BY id DESC LIMIT 1);
SET @post_doubao := (SELECT id FROM adoption_posts WHERE title='[DEMO] 豆包等待领养（杭州）' ORDER BY id DESC LIMIT 1);
SET @post_xiaoyun := (SELECT id FROM adoption_posts WHERE title='[DEMO] 小云寻稳定家庭（南京）' ORDER BY id DESC LIMIT 1);

INSERT INTO pet_media (pet_id, file_object_id, media_type, sort_order)
VALUES
(@pet_mili, (SELECT id FROM file_objects WHERE object_key='demo/pets/mili-1.jpg'), 'IMAGE', 0),
(@pet_mili, (SELECT id FROM file_objects WHERE object_key='demo/pets/mili-2.jpg'), 'IMAGE', 1),
(@pet_doubao, (SELECT id FROM file_objects WHERE object_key='demo/pets/doubao-1.jpg'), 'IMAGE', 0),
(@pet_doubao, (SELECT id FROM file_objects WHERE object_key='demo/pets/doubao-2.jpg'), 'IMAGE', 1),
(@pet_xiaoyun, (SELECT id FROM file_objects WHERE object_key='demo/pets/xiaoyun-1.jpg'), 'IMAGE', 0),
(@pet_naitang, (SELECT id FROM file_objects WHERE object_key='demo/pets/naitang-1.jpg'), 'IMAGE', 0),
(@pet_aqi, (SELECT id FROM file_objects WHERE object_key='demo/pets/aqi-1.jpg'), 'IMAGE', 0),
(@pet_tuanzi, (SELECT id FROM file_objects WHERE object_key='demo/pets/tuanzi-1.jpg'), 'IMAGE', 0)
;

INSERT INTO adoption_applications (post_id, applicant_user_id, message, living_env_note, pet_experience_note, status, handled_by_user_id, handled_at, decision_note)
VALUES
(@post_mili, @u_owner2, '家里已有一只温顺母猫，想给米粒一个稳定的家。', '两室一厅全屋防护窗网', '有 5 年养猫经验', 'SUBMITTED', NULL, NULL, NULL),
(@post_mili, @u_owner3, '工作稳定，可接受长期回访。', '有独立宠物房', '有流浪猫救助经验', 'REJECTED', @u_owner1, NOW(3) - INTERVAL 2 DAY, '已与另一位申请人优先沟通'),
(@post_doubao, @u_owner1, '家里有院子，适合豆包活动。', '独立庭院', '长期养犬', 'ACCEPTED', @u_owner2, NOW(3) - INTERVAL 1 DAY, '线下见面通过，已确认')
;

-- 4) 喂养服务者与订单
INSERT INTO feeding_provider_profiles (
  provider_user_id, status, display_name, headline, intro,
  service_city_code, service_city_name, service_districts,
  service_pet_types, service_item_tags, base_price_per_visit,
  experience_years, max_orders_per_day, accept_notes,
  rating_avg, rating_count, completed_order_count
)
VALUES
(@u_provider1, 'ACTIVE', '晴晴上门照护', '猫咪照护 120+ 单', '擅长猫咪日常喂养与慢速安抚，留痕细致。', '310100', '上海', JSON_ARRAY('徐汇区','长宁区','静安区'), JSON_ARRAY('CAT'), JSON_ARRAY('FEED','WATER','LITTER','PHOTO_REPORT','HEALTH_OBSERVATION'), 88.00, 4, 4, '可提前一天沟通宠物习性。', 4.90, 86, 132),
(@u_provider2, 'ACTIVE', '阿泽宠护', '狗狗照护与遛犬经验丰富', '支持节假日服务，擅长胆小犬陪护。', '330100', '杭州', JSON_ARRAY('滨江区','西湖区'), JSON_ARRAY('DOG','CAT'), JSON_ARRAY('FEED','WATER','PLAY','PHOTO_REPORT'), 96.00, 5, 3, '建议提前共享门禁和注意事项。', 4.80, 64, 98),
(@u_provider3, 'ACTIVE', 'Luna 宠护', '多宠家庭照护', '可处理多宠家庭分宠喂养与留痕。', '440300', '深圳', JSON_ARRAY('南山区','福田区'), JSON_ARRAY('CAT','DOG'), JSON_ARRAY('FEED','WATER','LITTER','PLAY','PHOTO_REPORT'), 108.00, 3, 3, '支持固定时间段上门。', 4.70, 42, 67)
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  display_name = VALUES(display_name),
  headline = VALUES(headline),
  intro = VALUES(intro),
  service_city_code = VALUES(service_city_code),
  service_city_name = VALUES(service_city_name),
  service_districts = VALUES(service_districts),
  service_pet_types = VALUES(service_pet_types),
  service_item_tags = VALUES(service_item_tags),
  base_price_per_visit = VALUES(base_price_per_visit),
  experience_years = VALUES(experience_years),
  max_orders_per_day = VALUES(max_orders_per_day),
  accept_notes = VALUES(accept_notes),
  rating_avg = VALUES(rating_avg),
  rating_count = VALUES(rating_count),
  completed_order_count = VALUES(completed_order_count),
  updated_at = NOW(3);

SET @provider_profile1 := (SELECT id FROM feeding_provider_profiles WHERE provider_user_id=@u_provider1);
SET @provider_profile2 := (SELECT id FROM feeding_provider_profiles WHERE provider_user_id=@u_provider2);
SET @provider_profile3 := (SELECT id FROM feeding_provider_profiles WHERE provider_user_id=@u_provider3);

INSERT INTO feeding_orders (
  order_no, owner_user_id, provider_user_id, provider_profile_id, status,
  service_city_code, service_city_name, service_district_name,
  service_address_detail, service_address_note,
  contact_name, contact_mobile, contact_mobile_masked,
  service_item_tags, visit_count, owner_note,
  requested_total_amount, quoted_total_amount, currency,
  provider_response_note, owner_confirmed_at, created_at, updated_at
)
VALUES
('DEMOFO202602250001', @u_owner1, @u_provider1, @provider_profile1, 'CONFIRMED', '310100', '上海', '徐汇区', '漕溪北路 88 弄 3 号', '门禁卡放在物业', '林小鱼', '13800138001', '138****8001', JSON_ARRAY('FEED','WATER','LITTER','PHOTO_REPORT'), 2, '主粮在厨房左侧抽屉。', 176.00, 176.00, 'CNY', '已确认可按时上门', NOW(3) - INTERVAL 2 DAY, NOW(3) - INTERVAL 3 DAY, NOW(3) - INTERVAL 2 DAY),
('DEMOFO202602250002', @u_owner2, @u_provider2, @provider_profile2, 'IN_SERVICE', '330100', '杭州', '滨江区', '江南大道 1200 号', '需提前电话确认', '周木木', '13800138002', '138****8002', JSON_ARRAY('FEED','WATER','PLAY','PHOTO_REPORT'), 3, '狗狗怕生，先远距离互动。', 288.00, 288.00, 'CNY', '第一天先短时接触', NOW(3) - INTERVAL 1 DAY, NOW(3) - INTERVAL 2 DAY, NOW(3) - INTERVAL 10 HOUR),
('DEMOFO202602250003', @u_owner3, @u_provider3, @provider_profile3, 'WAITING_OWNER_CONFIRM', '440300', '深圳', '南山区', '科技园南区 5 栋', NULL, '顾晨', '13800138003', '138****8003', JSON_ARRAY('FEED','WATER','LITTER','PHOTO_REPORT'), 2, '第二次上门请补充猫砂。', 216.00, 216.00, 'CNY', '两次服务均完成', NOW(3) - INTERVAL 8 HOUR, NOW(3) - INTERVAL 2 DAY, NOW(3) - INTERVAL 2 HOUR),
('DEMOFO202602250004', @u_owner1, @u_provider2, @provider_profile2, 'COMPLETED', '310100', '上海', '长宁区', '天山路 66 号', '可从西门进入', '林小鱼', '13800138001', '138****8001', JSON_ARRAY('FEED','WATER','PLAY','PHOTO_REPORT'), 1, '单次临时托付。', 96.00, 96.00, 'CNY', '已完成并上传照片', NOW(3) - INTERVAL 4 DAY, NOW(3) - INTERVAL 5 DAY, NOW(3) - INTERVAL 4 DAY)
;

SET @order1 := (SELECT id FROM feeding_orders WHERE order_no='DEMOFO202602250001');
SET @order2 := (SELECT id FROM feeding_orders WHERE order_no='DEMOFO202602250002');
SET @order3 := (SELECT id FROM feeding_orders WHERE order_no='DEMOFO202602250003');
SET @order4 := (SELECT id FROM feeding_orders WHERE order_no='DEMOFO202602250004');

INSERT INTO feeding_order_pets (
  order_id, pet_id, pet_name_snapshot, pet_type_snapshot,
  pet_gender_snapshot, age_months_snapshot, breed_snapshot, special_care_note_snapshot
)
VALUES
(@order1, @pet_mili, 'Demo-米粒', 'CAT', 'FEMALE', 24, '英短', '注意分次喂食'),
(@order2, @pet_doubao, 'Demo-豆包', 'DOG', 'MALE', 18, '中华田园犬', '先建立信任再牵引'),
(@order3, @pet_naitang, 'Demo-奶糖', 'CAT', 'FEMALE', 8, '布偶', '幼猫注意保暖'),
(@order4, @pet_aqi, 'Demo-阿七', 'DOG', 'MALE', 48, '柯基', '出门前检查饮水')
;

INSERT INTO feeding_order_visits (
  order_id, visit_index, planned_start_at, planned_end_at, status,
  actual_start_at, actual_end_at, food_done, water_done, litter_done, play_done,
  health_observation, visit_note
)
VALUES
(@order1, 1, NOW(3) - INTERVAL 1 DAY, NOW(3) - INTERVAL 1 DAY + INTERVAL 40 MINUTE, 'DONE', NOW(3) - INTERVAL 1 DAY + INTERVAL 5 MINUTE, NOW(3) - INTERVAL 1 DAY + INTERVAL 35 MINUTE, 1, 1, 1, 1, '精神状态稳定', '完成喂食和猫砂清理，已上传图片'),
(@order1, 2, NOW(3) + INTERVAL 1 DAY, NOW(3) + INTERVAL 1 DAY + INTERVAL 40 MINUTE, 'PENDING', NULL, NULL, 0, 0, 0, 0, NULL, NULL),
(@order2, 1, NOW(3) - INTERVAL 12 HOUR, NOW(3) - INTERVAL 11 HOUR + INTERVAL 30 MINUTE, 'DONE', NOW(3) - INTERVAL 12 HOUR, NOW(3) - INTERVAL 11 HOUR + INTERVAL 25 MINUTE, 1, 1, 0, 1, '食欲良好', '完成第一次到访'),
(@order2, 2, NOW(3) + INTERVAL 6 HOUR, NOW(3) + INTERVAL 6 HOUR + INTERVAL 30 MINUTE, 'STARTED', NOW(3) + INTERVAL 6 HOUR, NULL, 1, 1, 0, 0, '正在服务中', '已到达，准备喂食'),
(@order2, 3, NOW(3) + INTERVAL 1 DAY, NOW(3) + INTERVAL 1 DAY + INTERVAL 30 MINUTE, 'PENDING', NULL, NULL, 0, 0, 0, 0, NULL, NULL),
(@order3, 1, NOW(3) - INTERVAL 7 HOUR, NOW(3) - INTERVAL 6 HOUR + INTERVAL 30 MINUTE, 'DONE', NOW(3) - INTERVAL 7 HOUR, NOW(3) - INTERVAL 6 HOUR + INTERVAL 20 MINUTE, 1, 1, 1, 1, '轻微应激但可进食', '第一天服务完成'),
(@order3, 2, NOW(3) - INTERVAL 2 HOUR, NOW(3) - INTERVAL 1 HOUR + INTERVAL 30 MINUTE, 'DONE', NOW(3) - INTERVAL 2 HOUR, NOW(3) - INTERVAL 1 HOUR + INTERVAL 18 MINUTE, 1, 1, 1, 1, '状态正常', '第二次服务完成，等待主人确认'),
(@order4, 1, NOW(3) - INTERVAL 4 DAY, NOW(3) - INTERVAL 4 DAY + INTERVAL 30 MINUTE, 'DONE', NOW(3) - INTERVAL 4 DAY + INTERVAL 3 MINUTE, NOW(3) - INTERVAL 4 DAY + INTERVAL 25 MINUTE, 1, 1, 0, 1, '状态活跃', '单次托付已完成')
;

INSERT INTO feeding_visit_media (visit_id, file_object_id, sort_order)
VALUES
((SELECT id FROM feeding_order_visits WHERE order_id=@order1 AND visit_index=1), (SELECT id FROM file_objects WHERE object_key='demo/feeding/visit-1.jpg'), 0),
((SELECT id FROM feeding_order_visits WHERE order_id=@order2 AND visit_index=1), (SELECT id FROM file_objects WHERE object_key='demo/feeding/visit-2.jpg'), 0)
;

INSERT INTO feeding_order_reviews (
  order_id, owner_user_id, provider_user_id,
  rating_overall, rating_timeliness, rating_cleanliness, rating_attitude, content
)
VALUES
(@order4, @u_owner1, @u_provider2, 5, 5, 5, 5, '沟通顺畅，照片反馈及时，整体体验很好。')
;

-- 5) 救助内容与线索
INSERT INTO rescue_guides (
  scenario_code, title, summary, content_md, city_code, tags,
  sort_order, status, version, published_at, created_by_admin_id, updated_by_admin_id
)
VALUES
('FOUND_STRAY_CAT', '[DEMO] 发现流浪猫：先观察再接触', '先判断环境安全，再决定是否临时安置。', '## 处理步骤\n1. 先观察 15-30 分钟\n2. 准备干净水和少量食物\n3. 若需带离，优先使用透气笼\n4. 及时联系同城资源', NULL, JSON_ARRAY('入门','流浪猫'), 10, 'PUBLISHED', 1, NOW(3) - INTERVAL 5 DAY, @admin_id, @admin_id),
('FOUND_STRAY_DOG', '[DEMO] 发现流浪狗：避免追赶和刺激', '狗狗应激风险较高，先建立安全距离。', '## 处理步骤\n1. 保持安全距离\n2. 避免直视和追赶\n3. 用温和语气引导\n4. 联络志愿者或收容点', NULL, JSON_ARRAY('流浪狗','安全'), 20, 'PUBLISHED', 1, NOW(3) - INTERVAL 4 DAY, @admin_id, @admin_id),
('INJURED_CAT', '[DEMO] 受伤猫应急处理', '先止血保暖，再尽快转运。', '## 应急\n- 佩戴手套\n- 轻压止血\n- 保暖\n- 尽快就医', '310100', JSON_ARRAY('急救','猫'), 30, 'PUBLISHED', 1, NOW(3) - INTERVAL 3 DAY, @admin_id, @admin_id),
('INJURED_DOG', '[DEMO] 受伤狗转运指南', '控制风险，减少二次伤害。', '## 转运建议\n- 使用毯子或硬板\n- 固定四肢\n- 途中保持安静', '440300', JSON_ARRAY('急救','狗'), 40, 'PUBLISHED', 1, NOW(3) - INTERVAL 2 DAY, @admin_id, @admin_id),
('ABANDONED_KITTENS', '[DEMO] 幼猫救助判断', '先确认是否母猫暂离。', '## 判断\n1. 观察母猫是否回巢\n2. 注意保暖\n3. 必要时联系奶猫志愿者', NULL, JSON_ARRAY('幼猫','判断'), 50, 'PUBLISHED', 1, NOW(3) - INTERVAL 1 DAY, @admin_id, @admin_id)
;

INSERT INTO rescue_resources (
  resource_type, name, city_code, city_name, district_name, address,
  contact_phone, contact_wechat, contact_other, service_hours, service_scope,
  accept_pet_types, capability_tags, description, source_url,
  verified_at, status, sort_order, created_by_admin_id, updated_by_admin_id
)
VALUES
('ANIMAL_HOSPITAL', '[DEMO] 徐汇宠物急救医院', '310100', '上海', '徐汇区', '上海市徐汇区龙华中路 188 号', '13800138011', 'xuhui-pet-er', NULL, '24 小时', '外伤急救/夜间接诊', JSON_ARRAY('CAT','DOG'), JSON_ARRAY('急诊','手术'), '支持夜间急诊，建议先电话分诊。', 'https://example.com/demo-hospital-1', NOW(3) - INTERVAL 3 DAY, 'ACTIVE', 10, @admin_id, @admin_id),
('VOLUNTEER_GROUP', '[DEMO] 杭州流浪猫志愿组', '330100', '杭州', '滨江区', '线上组织', '13800138012', 'hz-cat-help', 'QQ群 123456', '09:00-21:00', '流浪猫临时安置/送医协助', JSON_ARRAY('CAT'), JSON_ARRAY('上门协助','转运'), '可协助抓捕和送医。', 'https://example.com/demo-volunteer-1', NOW(3) - INTERVAL 2 DAY, 'ACTIVE', 20, @admin_id, @admin_id),
('SHELTER', '[DEMO] 南京同伴动物之家', '320100', '南京', '鼓楼区', '南京市鼓楼区示例路 8 号', '13800138013', NULL, NULL, '10:00-18:00', '临时收容/领养对接', JSON_ARRAY('CAT','DOG'), JSON_ARRAY('收容','领养评估'), '支持短期收容，需提前预约。', 'https://example.com/demo-shelter-1', NOW(3) - INTERVAL 1 DAY, 'ACTIVE', 30, @admin_id, @admin_id),
('OFFICIAL_CHANNEL', '[DEMO] 深圳官方救助热线', '440300', '深圳', '南山区', NULL, '13800138014', NULL, '12345 转宠物救助', '工作日 09:00-18:00', '政策咨询/官方转介', JSON_ARRAY('CAT','DOG','UNKNOWN'), JSON_ARRAY('官方'), '可提供属地政策与转介信息。', 'https://example.com/demo-official-1', NOW(3) - INTERVAL 1 DAY, 'ACTIVE', 40, @admin_id, @admin_id)
;

INSERT INTO rescue_clues (
  clue_no, reporter_user_id, city_code, city_name, district_name,
  location_text, geo_lat, geo_lng, pet_type, estimated_count,
  urgency_level, condition_tags, description,
  contact_name, contact_mobile, contact_mobile_masked,
  status, triage_note, resolution_note, handled_by_admin_id, handled_at, suggested_resource_ids
)
VALUES
('DEMORC20260225001', @u_reporter1, '310100', '上海', '徐汇区', '龙华中路地铁口旁绿化带', 31.1842211, 121.4491122, 'CAT', 2, 'MEDIUM', JSON_ARRAY('STARVING'), '两只流浪猫连续三天出现，体态偏瘦。', '陆然', '13800138007', '138****8007', 'SUBMITTED', NULL, NULL, NULL, NULL, JSON_ARRAY()),
('DEMORC20260225002', @u_reporter2, '440300', '深圳', '南山区', '科技园公交站后方', 22.5412301, 113.9503102, 'DOG', 1, 'HIGH', JSON_ARRAY('INJURED','BLEEDING'), '疑似前腿受伤，走路跛行。', '陈一', '13800138008', '138****8008', 'TRIAGED', '已联系就近志愿者先行观察', NULL, @admin_id, NOW(3) - INTERVAL 6 HOUR, JSON_ARRAY((SELECT id FROM rescue_resources WHERE name='[DEMO] 深圳官方救助热线' LIMIT 1))),
('DEMORC20260225003', @u_reporter1, '330100', '杭州', '滨江区', '江南大道天桥下', 30.2088102, 120.2057123, 'CAT', 3, 'LOW', JSON_ARRAY('ABANDONED'), '三只幼猫已被临时安置，等待资源接力。', '陆然', '13800138007', '138****8007', 'RESOLVED', '已转交志愿组', '幼猫已被接收', @admin_id, NOW(3) - INTERVAL 1 DAY, JSON_ARRAY((SELECT id FROM rescue_resources WHERE name='[DEMO] 杭州流浪猫志愿组' LIMIT 1)))
;

INSERT INTO rescue_clue_media (clue_id, file_object_id, sort_order)
VALUES
((SELECT id FROM rescue_clues WHERE clue_no='DEMORC20260225001'), (SELECT id FROM file_objects WHERE object_key='demo/rescue/clue-1.jpg'), 0),
((SELECT id FROM rescue_clues WHERE clue_no='DEMORC20260225002'), (SELECT id FROM file_objects WHERE object_key='demo/rescue/clue-2.jpg'), 0)
;

-- 6) 投诉工单
INSERT INTO complaint_tickets (
  ticket_no, reporter_user_id, target_type, target_id,
  title, content, evidence_file_ids,
  contact_mobile, contact_mobile_masked,
  priority, status, assigned_admin_id,
  triage_note, resolution_note, last_reply_at, handled_at
)
VALUES
('DEMOCT20260225001', @u_owner1, 'FEEDING_ORDER', @order2, '服务过程中沟通延迟', '第二次上门前沟通响应较慢，希望改进提醒机制。', JSON_ARRAY((SELECT id FROM file_objects WHERE object_key='demo/complaint/evidence-1.jpg')), '13800138001', '138****8001', 'MEDIUM', 'IN_REVIEW', @admin_id, '已通知服务者优化响应时效', NULL, NOW(3) - INTERVAL 3 HOUR, NULL),
('DEMOCT20260225002', @u_owner2, 'ADOPTION_POST', @post_xiaoyun, '帖子信息更新不及时', '帖子中描述的联系方式与实际沟通不一致，建议加强审核。', JSON_ARRAY(), '13800138002', '138****8002', 'LOW', 'RESOLVED', @admin_id, '已核对并提醒发布者更新', '已完成内容修正并回访确认', NOW(3) - INTERVAL 1 DAY, NOW(3) - INTERVAL 12 HOUR)
;

INSERT INTO complaint_ticket_replies (
  ticket_id, author_type, author_user_id, author_admin_id, content, is_internal_note
)
VALUES
((SELECT id FROM complaint_tickets WHERE ticket_no='DEMOCT20260225001'), 'USER', @u_owner1, NULL, '希望后续上门前能提前 30 分钟确认。', 0),
((SELECT id FROM complaint_tickets WHERE ticket_no='DEMOCT20260225001'), 'ADMIN', NULL, @admin_id, '已受理，会在服务者端增加提醒。', 0),
((SELECT id FROM complaint_tickets WHERE ticket_no='DEMOCT20260225002'), 'ADMIN', NULL, @admin_id, '已核验并完成修正，工单关闭。', 0)
;
