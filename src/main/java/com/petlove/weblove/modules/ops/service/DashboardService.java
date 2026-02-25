package com.petlove.weblove.modules.ops.service;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import com.petlove.weblove.modules.feeding.enums.FeedingProviderProfileStatus;
import com.petlove.weblove.modules.ops.dto.admin.DashboardSummaryDTO;
import com.petlove.weblove.modules.ops.dto.admin.DashboardTodoItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.DashboardTrendPointDTO;
import com.petlove.weblove.modules.ops.dto.admin.DashboardTrendQuery;
import com.petlove.weblove.modules.ops.enums.ComplaintTicketStatus;
import com.petlove.weblove.modules.rescue.enums.RescueClueStatus;
import com.petlove.weblove.modules.rescue.enums.RescueResourceStatus;
import com.petlove.weblove.modules.verification.enums.VerificationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final jakarta.persistence.EntityManager entityManager;

    public DashboardService(jakarta.persistence.EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional(readOnly = true)
    public DashboardSummaryDTO summary() {
        LocalDate utcToday = LocalDate.now(ZoneOffset.UTC);
        LocalDateTime start = utcToday.atStartOfDay();
        LocalDateTime end = utcToday.plusDays(1).atStartOfDay();

        long todayNewUsers = countByCreatedAt("select count(u) from User u where u.createdAt >= :start and u.createdAt < :end", start, end);
        long todayNewAdoptionPosts = countByCreatedAt("select count(p) from AdoptionPost p where p.createdAt >= :start and p.createdAt < :end", start, end);
        long todayNewFeedingOrders = countByCreatedAt("select count(o) from FeedingOrder o where o.createdAt >= :start and o.createdAt < :end", start, end);
        long todayNewRescueClues = countByCreatedAt("select count(c) from RescueClue c where c.createdAt >= :start and c.createdAt < :end", start, end);
        long todayNewComplaints = countByCreatedAt("select count(t) from ComplaintTicket t where t.createdAt >= :start and t.createdAt < :end", start, end);

        long pendingVerificationCount = countByStatus("select count(v) from UserVerification v where v.status = :status", VerificationStatus.PENDING);
        long pendingAdoptionReviewCount = countByStatus("select count(p) from AdoptionPost p where p.status = :status", AdoptionPostStatus.PENDING_REVIEW);
        long pendingComplaintCount = countByComplaintPendingStatus();
        long pendingRescueClueCount = countByRescuePendingStatus();

        long activeFeedingProviders = countByStatus("select count(p) from FeedingProviderProfile p where p.status = :status", FeedingProviderProfileStatus.ACTIVE);
        long publishedAdoptionPosts = countByStatus("select count(p) from AdoptionPost p where p.status = :status", AdoptionPostStatus.PUBLISHED);
        long activeRescueResources = countByStatus("select count(r) from RescueResource r where r.status = :status", RescueResourceStatus.ACTIVE);

        return new DashboardSummaryDTO(
            todayNewUsers,
            todayNewAdoptionPosts,
            todayNewFeedingOrders,
            todayNewRescueClues,
            todayNewComplaints,
            pendingVerificationCount,
            pendingAdoptionReviewCount,
            pendingComplaintCount,
            pendingRescueClueCount,
            activeFeedingProviders,
            publishedAdoptionPosts,
            activeRescueResources,
            LocalDateTime.now(ZoneOffset.UTC)
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional(readOnly = true)
    public List<DashboardTodoItemDTO> todos() {
        DashboardSummaryDTO summary = summary();
        List<DashboardTodoItemDTO> result = new ArrayList<>();

        if (summary.pendingVerificationCount() > 0) {
            result.add(new DashboardTodoItemDTO(
                "VERIFICATION_REVIEW",
                "待审核实名认证",
                summary.pendingVerificationCount(),
                "HIGH",
                "/admin/verifications"
            ));
        }
        if (summary.pendingAdoptionReviewCount() > 0) {
            result.add(new DashboardTodoItemDTO(
                "ADOPTION_REVIEW",
                "待审核送养帖",
                summary.pendingAdoptionReviewCount(),
                "HIGH",
                "/admin/adoptions/posts"
            ));
        }
        if (summary.pendingComplaintCount() > 0) {
            result.add(new DashboardTodoItemDTO(
                "COMPLAINT_PENDING",
                "待处理投诉工单",
                summary.pendingComplaintCount(),
                "HIGH",
                "/admin/ops/complaints"
            ));
        }
        if (summary.pendingRescueClueCount() > 0) {
            result.add(new DashboardTodoItemDTO(
                "RESCUE_TRIAGE",
                "待分诊救助线索",
                summary.pendingRescueClueCount(),
                "MEDIUM",
                "/admin/rescue/clues"
            ));
        }

        if (result.isEmpty()) {
            result.add(new DashboardTodoItemDTO("NO_TODO", "当前暂无待办", 0L, "LOW", "/admin/ops/dashboard"));
        }

        return result;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional(readOnly = true)
    public List<DashboardTrendPointDTO> trends(DashboardTrendQuery query) {
        String metric = normalizeRequiredText(query.getMetric(), "metric").toUpperCase(Locale.ROOT);
        int days = query.getDays() == null ? 7 : query.getDays();
        int normalizedDays = Math.min(Math.max(days, 1), 30);

        TrendMetric trendMetric = parseMetric(metric);

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = todayUtc.minusDays(normalizedDays - 1L);

        List<DashboardTrendPointDTO> points = new ArrayList<>();
        for (int i = 0; i < normalizedDays; i++) {
            LocalDate date = startDate.plusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            long value = countTrendMetric(trendMetric, dayStart, dayEnd);
            points.add(new DashboardTrendPointDTO(date, value));
        }
        return points;
    }

    private TrendMetric parseMetric(String metric) {
        try {
            return TrendMetric.valueOf(metric);
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "metric is invalid");
        }
    }

    private long countTrendMetric(TrendMetric metric, LocalDateTime start, LocalDateTime end) {
        return switch (metric) {
            case NEW_USERS -> countByCreatedAt("select count(u) from User u where u.createdAt >= :start and u.createdAt < :end", start, end);
            case ADOPTION_POSTS -> countByCreatedAt("select count(p) from AdoptionPost p where p.createdAt >= :start and p.createdAt < :end", start, end);
            case FEEDING_ORDERS -> countByCreatedAt("select count(o) from FeedingOrder o where o.createdAt >= :start and o.createdAt < :end", start, end);
            case RESCUE_CLUES -> countByCreatedAt("select count(c) from RescueClue c where c.createdAt >= :start and c.createdAt < :end", start, end);
            case COMPLAINTS -> countByCreatedAt("select count(t) from ComplaintTicket t where t.createdAt >= :start and t.createdAt < :end", start, end);
        };
    }

    private long countByComplaintPendingStatus() {
        return entityManager.createQuery(
                "select count(t) from ComplaintTicket t where t.status in (:s1,:s2,:s3)",
                Long.class
            )
            .setParameter("s1", ComplaintTicketStatus.SUBMITTED)
            .setParameter("s2", ComplaintTicketStatus.IN_REVIEW)
            .setParameter("s3", ComplaintTicketStatus.WAITING_USER)
            .getSingleResult();
    }

    private long countByRescuePendingStatus() {
        return entityManager.createQuery(
                "select count(c) from RescueClue c where c.status in (:s1,:s2,:s3)",
                Long.class
            )
            .setParameter("s1", RescueClueStatus.SUBMITTED)
            .setParameter("s2", RescueClueStatus.TRIAGED)
            .setParameter("s3", RescueClueStatus.IN_PROGRESS)
            .getSingleResult();
    }

    private long countByStatus(String jpql, Enum<?> status) {
        return entityManager.createQuery(jpql, Long.class)
            .setParameter("status", status)
            .getSingleResult();
    }

    private long countByCreatedAt(String jpql, LocalDateTime start, LocalDateTime end) {
        return entityManager.createQuery(jpql, Long.class)
            .setParameter("start", start)
            .setParameter("end", end)
            .getSingleResult();
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private enum TrendMetric {
        NEW_USERS,
        ADOPTION_POSTS,
        FEEDING_ORDERS,
        RESCUE_CLUES,
        COMPLAINTS
    }
}
