drop table if exists users cascade;
drop table if exists courses cascade;
drop table if exists grades cascade;
drop table if exists forum_posts cascade;

create table users(
  id bigserial primary key,
  username varchar(100) unique not null,
  password_md5 varchar(128) not null,
  role varchar(20) not null,
  email varchar(255) not null
);

create table courses(
  id bigserial primary key,
  name varchar(255) not null,
  description text
);

create table grades(
  id bigserial primary key,
  student_id bigint not null references users(id) on delete cascade,
  course_id bigint not null references courses(id) on delete cascade,
  grade_value varchar(4) not null
);

create table forum_posts(
  id bigserial primary key,
  author_id bigint not null references users(id) on delete cascade,
  content text not null,
  created_at timestamp not null default now()
);
