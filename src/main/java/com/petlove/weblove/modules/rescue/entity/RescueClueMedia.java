package com.petlove.weblove.modules.rescue.entity;

import com.petlove.weblove.common.util.CreatedOnlyEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "rescue_clue_media")
public class RescueClueMedia extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clue_id", nullable = false)
    private Long clueId;

    @Column(name = "file_object_id", nullable = false)
    private Long fileObjectId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public Long getClueId() {
        return clueId;
    }

    public void setClueId(Long clueId) {
        this.clueId = clueId;
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
