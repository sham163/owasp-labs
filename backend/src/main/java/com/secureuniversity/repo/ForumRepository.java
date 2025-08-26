package com.secureuniversity.repo;

import com.secureuniversity.model.ForumPost;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ForumRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<ForumPost> map = (rs, i) -> new ForumPost(
            rs.getLong("id"),
            rs.getLong("author_id"),
            rs.getString("content"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    public ForumRepository(JdbcTemplate jdbc){ this.jdbc=jdbc; }

    public void addPost(long authorId, String content) {
        jdbc.update("insert into forum_posts(author_id, content, created_at) values(?,?,?)",
                authorId, content, Timestamp.valueOf(LocalDateTime.now()));
    }

    public List<ForumPost> list() {
        return jdbc.query("select * from forum_posts order by created_at desc", map);
    }
}
