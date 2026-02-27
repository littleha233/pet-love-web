package com.petlove.weblove.modules.auth.repository;

import com.petlove.weblove.modules.auth.entity.SmsCodeRecord;
import com.petlove.weblove.modules.auth.enums.SmsBizType;
import com.petlove.weblove.modules.auth.enums.SmsCodeStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsCodeRecordRepository extends JpaRepository<SmsCodeRecord, Long> {

    Optional<SmsCodeRecord> findTopByMobileAndBizTypeOrderByIdDesc(String mobile, SmsBizType bizType);

    Optional<SmsCodeRecord> findTopByMobileAndBizTypeAndStatusOrderByIdDesc(String mobile,
                                                                             SmsBizType bizType,
                                                                             SmsCodeStatus status);

    long countByMobileAndBizTypeAndCreatedAtGreaterThanEqual(String mobile,
                                                              SmsBizType bizType,
                                                              LocalDateTime createdAt);

    long countByClientIpAndCreatedAtGreaterThanEqual(String clientIp, LocalDateTime createdAt);
}
