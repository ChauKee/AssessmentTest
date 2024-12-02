package com.vanguard.assessment.entity;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
public abstract class BaseEntity {

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;
    @Column(name = "created_by", nullable = false, length = 32)
    private String createdBy;
    @Column(name = "updated_date", nullable = false)
    private Timestamp updatedDate;
    @Column(name = "updated_by", nullable = false, length = 32)
    private String updatedBy;

}
