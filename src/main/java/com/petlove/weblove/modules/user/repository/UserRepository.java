package com.petlove.weblove.modules.user.repository;

import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMobile(String mobile);

    Optional<User> findByEmail(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByMobileContainingIgnoreCaseOrEmailContainingIgnoreCase(String mobileKeyword,
                                                                            String emailKeyword,
                                                                            Pageable pageable);

    Page<User> findByStatusAndMobileContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCase(
        UserStatus mobileStatus,
        String mobileKeyword,
        UserStatus emailStatus,
        String emailKeyword,
        Pageable pageable
    );

    @Query("""
        select u.id from User u
        where lower(coalesce(u.mobile, '')) like lower(concat('%', :keyword, '%'))
           or lower(coalesce(u.email, '')) like lower(concat('%', :keyword, '%'))
        """)
    List<Long> searchIdsByMobileOrEmail(@Param("keyword") String keyword);
}
