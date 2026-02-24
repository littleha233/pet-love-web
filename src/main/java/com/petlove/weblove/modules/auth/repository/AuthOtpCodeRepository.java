package com.petlove.weblove.modules.auth.repository;

import com.petlove.weblove.modules.auth.entity.AuthOtpCode;
import com.petlove.weblove.modules.auth.enums.OtpChannel;
import com.petlove.weblove.modules.auth.enums.OtpPurpose;
import com.petlove.weblove.modules.auth.enums.OtpStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthOtpCodeRepository extends JpaRepository<AuthOtpCode, Long> {

    Optional<AuthOtpCode> findTopByChannelAndTargetHashAndPurposeOrderByIdDesc(OtpChannel channel,
                                                                                 String targetHash,
                                                                                 OtpPurpose purpose);

    Optional<AuthOtpCode> findTopByChannelAndTargetHashAndPurposeAndStatusOrderByIdDesc(OtpChannel channel,
                                                                                          String targetHash,
                                                                                          OtpPurpose purpose,
                                                                                          OtpStatus status);
}
