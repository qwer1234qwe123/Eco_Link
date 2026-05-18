-- alter table collection_route drop FOREIGN key fk_collection_route_worker;

-- drop table worker;

-- create table users (
--     id bigint not null AUTO_INCREMENT,
--     username VARCHAR(50) not null,
--     password VARCHAR(255) not null,
--     grade int not null DEFAULT 5,
--     vehicle_number VARCHAR(20) null,
--     PRIMARY key (id),
--     UNIQUE key uq_user_username (username)
-- );

-- alter table collection_route drop FOREIGN key fk_collection_route_worker;

-- alter table collection_route CHANGE COLUMN worker_id user_id bigint not null;

-- alter table collection_route add constraint fk_collection_route_user
--     FOREIGN key (user_id) REFERENCES users (id)
--     on delete RESTRICT on update cascade;

-- show create table collection_route;

-- alter table collection_route drop index idx_collection_route_worker_id;

-- alter table collection_route CHANGE COLUMN worker_id user_id BIGINT not null;

-- alter table collection_route add index idx_collection_route_user_id (user_id);

-- alter table collection_route add constraint fk_collection_route_user
--     Foreign Key (user_id) REFERENCES users (id)
--     on delete RESTRICT on update cascade;

-- show create table collection_route;

-- insert into users (username, password, grade, vehicle_number) VALUES('admin','admin1234',1,null);
-- insert into users (username, password, vehicle_number) VALUES('worker1','worker1234','12가3456');

select * from users;

update users set username='admin' where username='관리자';
update users set username='worker1' where username='작업자1';
