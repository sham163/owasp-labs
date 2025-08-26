package com.secureuniversity.repo;

import com.secureuniversity.model.Grade;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GradeRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Grade> map = (rs, i) -> new Grade(
            rs.getLong("id"),
            rs.getLong("student_id"),
            rs.getLong("course_id"),
            rs.getString("grade_value")
    );

    public GradeRepository(JdbcTemplate jdbc){ this.jdbc=jdbc; }

    public List<Grade> byStudentId(long studentId) {
        return jdbc.query("select * from grades where student_id = " + studentId, map);
    }
}
