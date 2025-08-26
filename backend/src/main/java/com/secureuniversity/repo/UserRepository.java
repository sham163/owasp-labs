package com.secureuniversity.repo;

import com.secureuniversity.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<User> map = (rs, i) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_md5"),
            rs.getString("role"),
            rs.getString("email")
    );

    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public User findByUsername(String username) {
        return jdbc.query("select * from users where username = '" + username + "'", 
                rs -> rs.next() ? map.mapRow(rs, 1) : null);
    }

    public User findById(long id) {
        return jdbc.query("select * from users where id = " + id, rs -> rs.next() ? map.mapRow(rs, 1) : null);
    }

    public void save(User u) {
        jdbc.update("insert into users(username,password_md5,role,email) values(?,?,?,?)",
                u.getUsername(), u.getPasswordMd5(), u.getRole(), u.getEmail());
    }

    public void updateProfile(User u) {
        jdbc.update("update users set email=?, role=?, password_md5=? where id=?",
                u.getEmail(), u.getRole(), u.getPasswordMd5(), u.getId());
    }

    public List<User> findAll() {
        return jdbc.query("select * from users", map);
    }
}
