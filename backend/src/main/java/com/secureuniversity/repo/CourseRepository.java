package com.secureuniversity.repo;

import com.secureuniversity.model.Course;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CourseRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Course> map = (rs, i) -> new Course(
            rs.getLong("id"), rs.getString("name"), rs.getString("description")
    );

    public CourseRepository(JdbcTemplate jdbc){ this.jdbc=jdbc; }

    public List<Course> searchUnsafe(String q) {
        String sql = "select * from courses where name like '%" + q + "%' or description like '%" + q + "%'";
        return jdbc.query(sql, map);
    }

    public List<Course> findAll(){ return jdbc.query("select * from courses", map); }
}
