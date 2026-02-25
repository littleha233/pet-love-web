package com.petlove.weblove.modules.ops.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.ops.enums.ComplaintPriority;
import com.petlove.weblove.modules.ops.enums.ComplaintTargetType;
import com.petlove.weblove.modules.ops.enums.ComplaintTicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_tickets")
public class ComplaintTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", nullable = false, unique = true)
    private String ticketNo;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ComplaintTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "evidence_file_ids", columnDefinition = "JSON")
    private String evidenceFileIds;

    @Column(name = "contact_mobile")
    private String contactMobile;

    @Column(name = "contact_mobile_masked")
    private String contactMobileMasked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintTicketStatus status;

    @Column(name = "assigned_admin_id")
    private Long assignedAdminId;

    @Column(name = "triage_note")
    private String triageNote;

    @Column(name = "resolution_note")
    private String resolutionNote;

    @Column(name = "last_reply_at")
    private LocalDateTime lastReplyAt;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    public Long getId() {
        return id;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public Long getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(Long reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public ComplaintTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(ComplaintTargetType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEvidenceFileIds() {
        return evidenceFileIds;
    }

    public void setEvidenceFileIds(String evidenceFileIds) {
        this.evidenceFileIds = evidenceFileIds;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public String getContactMobileMasked() {
        return contactMobileMasked;
    }

    public void setContactMobileMasked(String contactMobileMasked) {
        this.contactMobileMasked = contactMobileMasked;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public ComplaintTicketStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintTicketStatus status) {
        this.status = status;
    }

    public Long getAssignedAdminId() {
        return assignedAdminId;
    }

    public void setAssignedAdminId(Long assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    public String getTriageNote() {
        return triageNote;
    }

    public void setTriageNote(String triageNote) {
        this.triageNote = triageNote;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public LocalDateTime getLastReplyAt() {
        return lastReplyAt;
    }

    public void setLastReplyAt(LocalDateTime lastReplyAt) {
        this.lastReplyAt = lastReplyAt;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }
}
