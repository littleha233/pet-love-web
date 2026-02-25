package com.petlove.weblove.modules.ops.entity;

import com.petlove.weblove.common.util.CreatedOnlyEntity;
import com.petlove.weblove.modules.ops.enums.ComplaintReplyAuthorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "complaint_ticket_replies")
public class ComplaintTicketReply extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false)
    private ComplaintReplyAuthorType authorType;

    @Column(name = "author_user_id")
    private Long authorUserId;

    @Column(name = "author_admin_id")
    private Long authorAdminId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_internal_note", nullable = false)
    private boolean internalNote;

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public ComplaintReplyAuthorType getAuthorType() {
        return authorType;
    }

    public void setAuthorType(ComplaintReplyAuthorType authorType) {
        this.authorType = authorType;
    }

    public Long getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(Long authorUserId) {
        this.authorUserId = authorUserId;
    }

    public Long getAuthorAdminId() {
        return authorAdminId;
    }

    public void setAuthorAdminId(Long authorAdminId) {
        this.authorAdminId = authorAdminId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isInternalNote() {
        return internalNote;
    }

    public void setInternalNote(boolean internalNote) {
        this.internalNote = internalNote;
    }
}
