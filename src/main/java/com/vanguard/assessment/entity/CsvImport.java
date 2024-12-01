package com.vanguard.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "vanguard", name = "csv_import")
@Getter @Setter
public class CsvImport extends BaseEntity {

    @Id
    private String id;
    @Column(name = "file_path")
    private String filepath;
    @Column(name = "status")
    private String status;

}
