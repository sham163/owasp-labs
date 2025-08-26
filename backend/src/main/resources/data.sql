-- MD5("password")
insert into users(username,password_md5,role,email) values
('admin','5f4dcc3b5aa765d61d8327deb882cf99','ADMIN','admin@secureu.local'),
('teacher1','5f4dcc3b5aa765d61d8327deb882cf99','TEACHER','t1@secureu.local'),
('student1','5f4dcc3b5aa765d61d8327deb882cf99','STUDENT','s1@secureu.local'),
('student2','5f4dcc3b5aa765d61d8327deb882cf99','STUDENT','s2@secureu.local');

insert into courses(name, description) values
('Intro to Security', 'Covers OWASP Top 10'),
('Computer Networks', 'ICMP, TCP/IP basics'),
('DB Systems', 'SQL basics; beware injection!');

insert into grades(student_id, course_id, grade_value) values
((select id from users where username='student1'), (select id from courses where name='Intro to Security'), 'A'),
((select id from users where username='student1'), (select id from courses where name='DB Systems'), 'B'),
((select id from users where username='student2'), (select id from courses where name='Intro to Security'), 'C');

insert into forum_posts(author_id, content) values
((select id from users where username='teacher1'), 'Welcome to <b>Secure University</b>!'),
((select id from users where username='student1'), '<script>alert("stored xss!")</script>');

-- enrollments
insert into enrollments(student_id, course_id) values
((select id from users where username='student1'), (select id from courses where name='Intro to Security')),
((select id from users where username='student1'), (select id from courses where name='DB Systems'));
