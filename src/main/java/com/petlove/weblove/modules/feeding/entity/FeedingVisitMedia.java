package com.petlove.weblove.modules.feeding.entity;

import com.petlove.weblove.common.util.CreatedOnlyEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "feeding_visit_media")
public class FeedingVisitMedia extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Column(name = "file_object_id", nullable = false)
    private Long fileObjectId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public Long getFileObjectId() {
        return fileObjectId;
    }

    public void setFileObjectId(Long fileObjectId) {
        this.fileObjectId = fileObjectId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
