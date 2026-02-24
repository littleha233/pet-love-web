package com.petlove.weblove.modules.user.repository;

import com.petlove.weblove.modules.user.entity.UserProfile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    List<UserProfile> findByUserIdIn(Collection<Long> userIds);

    @Query("""
        select up.userId from UserProfile up
        where lower(up.nickname) like lower(concat('%', :keyword, '%'))
        """)
    List<Long> searchUserIdsByNickname(@Param("keyword") String keyword);
}
