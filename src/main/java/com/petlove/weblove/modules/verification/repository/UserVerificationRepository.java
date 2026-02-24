package com.petlove.weblove.modules.verification.repository;

import com.petlove.weblove.modules.verification.entity.UserVerification;
import com.petlove.weblove.modules.verification.enums.VerificationType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long>, JpaSpecificationExecutor<UserVerification> {

    Optional<UserVerification> findByUserIdAndVerificationType(Long userId, VerificationType verificationType);

    List<UserVerification> findByUserId(Long userId);

    List<UserVerification> findByUserIdIn(Collection<Long> userIds);
}
