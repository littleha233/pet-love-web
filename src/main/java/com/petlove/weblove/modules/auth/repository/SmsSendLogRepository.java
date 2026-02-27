package com.petlove.weblove.modules.auth.repository;

import com.petlove.weblove.modules.auth.entity.SmsSendLog;
import com.petlove.weblove.modules.auth.enums.SmsBizType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsSendLogRepository extends JpaRepository<SmsSendLog, Long> {

    Optional<SmsSendLog> findTopByMobileAndBizTypeOrderByIdDesc(String mobile, SmsBizType bizType);

    long countByMobileAndBizTypeAndCreatedAtGreaterThanEqual(String mobile,
                                                              SmsBizType bizType,
                                                              LocalDateTime createdAt);

    long countByClientIpAndCreatedAtGreaterThanEqual(String clientIp, LocalDateTime createdAt);
}
