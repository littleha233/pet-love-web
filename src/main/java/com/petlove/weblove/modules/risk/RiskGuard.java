package com.petlove.weblove.modules.risk;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.ops.entity.RiskBlacklistEntry;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import com.petlove.weblove.modules.ops.service.CityFeatureSwitchService;
import com.petlove.weblove.modules.ops.service.RiskBlacklistService;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskGuard {

    private final RiskBlacklistService riskBlacklistService;
    private final CityFeatureSwitchService cityFeatureSwitchService;
    private final UserRepository userRepository;

    public RiskGuard(RiskBlacklistService riskBlacklistService,
                     CityFeatureSwitchService cityFeatureSwitchService,
                     UserRepository userRepository) {
        this.riskBlacklistService = riskBlacklistService;
        this.cityFeatureSwitchService = cityFeatureSwitchService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public RiskCheckResult checkUserAction(Long userId,
                                           String mobile,
                                           String cityCode,
                                           String actionKey,
                                           CityFeatureKey featureKey) {
        Optional<RiskBlacklistEntry> blockingEntry = riskBlacklistService.findFirstBlockingEntry(userId, mobile, actionKey);
        if (blockingEntry.isPresent()) {
            RiskBlacklistEntry entry = blockingEntry.get();
            String message = entry.getReasonNote() == null || entry.getReasonNote().isBlank()
                ? "当前账号存在风控限制，暂不可执行该操作"
                : entry.getReasonNote();
            return RiskCheckResult.deny(ErrorCode.RISK_BLACKLIST_BLOCKED, message);
        }

        CityFeatureSwitchService.CityFeatureCheckResult cityResult = cityFeatureSwitchService.checkWriteAllowed(cityCode, featureKey);
        if (!cityResult.allowed()) {
            return RiskCheckResult.deny(cityResult.errorCode(), cityResult.noticeText());
        }

        return RiskCheckResult.allow();
    }

    @Transactional(readOnly = true)
    public void ensureUserActionAllowed(Long userId,
                                        String cityCode,
                                        String actionKey,
                                        CityFeatureKey featureKey) {
        String mobile = resolveMobile(userId);
        RiskCheckResult result = checkUserAction(userId, mobile, cityCode, actionKey, featureKey);
        if (!result.allowed()) {
            throw new BizException(result.errorCode(), result.noticeText());
        }
    }

    @Transactional(readOnly = true)
    public void ensureCityFeatureReadable(String cityCode, CityFeatureKey featureKey) {
        CityFeatureSwitchService.CityFeatureCheckResult result = cityFeatureSwitchService.checkReadAllowed(cityCode, featureKey);
        if (!result.allowed()) {
            throw new BizException(result.errorCode(), result.noticeText());
        }
    }

    @Transactional(readOnly = true)
    public void ensureCityFeatureWritable(String cityCode, CityFeatureKey featureKey) {
        CityFeatureSwitchService.CityFeatureCheckResult result = cityFeatureSwitchService.checkWriteAllowed(cityCode, featureKey);
        if (!result.allowed()) {
            throw new BizException(result.errorCode(), result.noticeText());
        }
    }

    @Transactional(readOnly = true)
    public Set<String> resolveReadBlockedCityCodes(CityFeatureKey featureKey) {
        return cityFeatureSwitchService.findReadBlockedCityCodes(featureKey);
    }

    private String resolveMobile(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId).orElse(null);
        return user == null ? null : user.getMobile();
    }
}
