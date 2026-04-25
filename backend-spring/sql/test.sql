#쓰레기통6개, 직원1명 테스트 데이터 삽입 sql 
INSERT INTO trash_can (loc_name, loc_lat, loc_lng, max_capa) VALUES
('1번 쓰레기통', 10, 20, 100),
('2번 쓰레기통', 10, 20, 100),
('3번 쓰레기통', 10, 20, 100),
('4번 쓰레기통', 10, 20, 100),
('5번 쓰레기통', 10, 20, 100),
('6번 쓰레기통', 10, 20, 100);

INSERT INTO worker (username, password, vehicle_number) VALUES
('worker1', '1234', '12가3456');