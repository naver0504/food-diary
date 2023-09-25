package com.fooddiary.api.service;

import com.fooddiary.api.entity.tag.Tag;
import com.fooddiary.api.repository.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final JdbcTemplate jdbcTemplate;


    @Transactional
    public void deleteAllById(List<Integer> ids) {
        tagRepository.deleteAll(ids);
    }

    @Transactional
    public void saveAll(List<Tag> tags) {

        tagRepository.saveAll(tags);

        // tag가 두 개씩 저장된다...
//        String sql = "INSERT INTO TAG (TAG_NAME, IMAGE_ID, USER_ID) VALUES (?, ?, ?)";
//
//        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                Tag tag = tags.get(i);
//                ps.setString(1, tag.getTagName());
//                ps.setInt(2, tag.getImage().getId());
//                ps.setInt(3, tag.getUser().getId());
//            }
//
//            @Override
//            public int getBatchSize() {
//                return tags.size();
//            }
//        });
    }
}
