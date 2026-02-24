package com.petlove.weblove.modules.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.modules.admin.dto.AdminAuditLogDTO;
import com.petlove.weblove.modules.admin.entity.AdminAuditLog;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.repository.AdminAuditLogRepository;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    public AdminAuditService(AdminAuditLogRepository adminAuditLogRepository,
                             AdminUserRepository adminUserRepository,
                             ObjectMapper objectMapper) {
        this.adminAuditLogRepository = adminAuditLogRepository;
        this.adminUserRepository = adminUserRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void writeAuditLog(long adminUserId,
                              String action,
                              String targetType,
                              String targetId,
                              Object before,
                              Object after,
                              String remark) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUserId(adminUserId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeSnapshot(toJson(before));
        log.setAfterSnapshot(toJson(after));
        log.setRemark(remark);
        log.setRequestId(MDC.get("requestId"));
        adminAuditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLogDTO> list(Long adminUserId,
                                       String action,
                                       String targetType,
                                       Pageable pageable) {
        Specification<AdminAuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (adminUserId != null) {
                predicates.add(cb.equal(root.get("adminUserId"), adminUserId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<AdminAuditLog> page = adminAuditLogRepository.findAll(spec, pageable);
        Map<Long, String> adminNameMap = adminUserRepository.findAllById(
                page.getContent().stream().map(AdminAuditLog::getAdminUserId).distinct().toList())
            .stream()
            .collect(Collectors.toMap(AdminUser::getId, AdminUser::getDisplayName));

        return page.map(log -> new AdminAuditLogDTO(
            log.getId(),
            log.getAdminUserId(),
            adminNameMap.getOrDefault(log.getAdminUserId(), "未知管理员"),
            log.getAction(),
            log.getTargetType(),
            log.getTargetId(),
            log.getRemark(),
            log.getRequestId(),
            log.getCreatedAt()
        ));
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
