package com.secureuniversity.repo;

import com.secureuniversity.model.Grade;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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

    public List<Map<String,Object>> byStudentIdJoinCourse(long studentId) {
        String sql = "select c.name as course, g.grade_value as grade from grades g join courses c on c.id=g.course_id where g.student_id="+studentId;
        return jdbc.query(sql, (rs,i)-> Map.of("course", rs.getString("course"), "grade", rs.getString("grade")));
    }

    public void upsert(long studentId, long courseId, String grade) {
        int n = jdbc.update("update grades set grade_value=? where student_id=? and course_id=?",
                grade, studentId, courseId);
        if (n==0) {
            jdbc.update("insert into grades(student_id,course_id,grade_value) values(?,?,?)",
                    studentId, courseId, grade);
        }
    }
}
