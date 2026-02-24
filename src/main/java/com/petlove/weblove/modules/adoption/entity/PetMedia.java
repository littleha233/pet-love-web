package com.petlove.weblove.modules.adoption.entity;

import com.petlove.weblove.common.util.CreatedOnlyEntity;
import com.petlove.weblove.modules.adoption.enums.PetMediaType;
import jakarta.persistence.*;

@Entity
@Table(name = "pet_media")
public class PetMedia extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "file_object_id", nullable = false)
    private Long fileObjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private PetMediaType mediaType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getFileObjectId() {
        return fileObjectId;
    }

    public void setFileObjectId(Long fileObjectId) {
        this.fileObjectId = fileObjectId;
    }

    public PetMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(PetMediaType mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
