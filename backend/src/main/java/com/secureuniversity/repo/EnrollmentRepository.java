package com.secureuniversity.repo;

import com.secureuniversity.model.Enrollment;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EnrollmentRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Enrollment> map = (rs,i)-> new Enrollment(
            rs.getLong("id"), rs.getLong("student_id"), rs.getLong("course_id"),
            rs.getTimestamp("created_at").toLocalDateTime());

    public EnrollmentRepository(JdbcTemplate jdbc){ this.jdbc=jdbc; }

    public void enroll(long studentId, long courseId) {
        jdbc.update("insert into enrollments(student_id,course_id,created_at) values(?,?,?)",
                studentId, courseId, Timestamp.valueOf(LocalDateTime.now()));
    }

    public List<Enrollment> byStudent(long studentId) {
        return jdbc.query("select * from enrollments where student_id="+studentId+" order by created_at desc", map);
    }
}
