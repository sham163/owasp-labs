package com.secureuniversity.repo;

import com.secureuniversity.model.Course;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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

    public Course findById(long id) {
        return jdbc.query("select * from courses where id="+id, rs -> rs.next()? map.mapRow(rs,1):null);
    }

    /** Create a course and return its new ID */
    public long create(String name, String desc) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into courses(name, description) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, desc);
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().longValue() : -1L;
    }
}
