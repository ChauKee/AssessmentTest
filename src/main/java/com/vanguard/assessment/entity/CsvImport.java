package com.vanguard.assessment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "vanguard", name = "csv_import")
@Getter @Setter
public class CsvImport extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    @Column(name = "file_path", nullable = false, length = 100)
    private String filepath;
    @Column(name = "status", nullable = false)
    private Integer status;
    @Column(name = "error", length = 500)
    private String error;
}
