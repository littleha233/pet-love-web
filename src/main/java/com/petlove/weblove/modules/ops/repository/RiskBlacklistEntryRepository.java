package com.petlove.weblove.modules.ops.repository;

import com.petlove.weblove.modules.ops.entity.RiskBlacklistEntry;
import com.petlove.weblove.modules.ops.enums.BlacklistStatus;
import com.petlove.weblove.modules.ops.enums.BlacklistSubjectType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RiskBlacklistEntryRepository extends JpaRepository<RiskBlacklistEntry, Long>, JpaSpecificationExecutor<RiskBlacklistEntry> {

    List<RiskBlacklistEntry> findBySubjectTypeAndSubjectValueAndStatus(BlacklistSubjectType subjectType,
                                                                       String subjectValue,
                                                                       BlacklistStatus status);

    List<RiskBlacklistEntry> findByStatusIn(Collection<BlacklistStatus> statuses);
}
