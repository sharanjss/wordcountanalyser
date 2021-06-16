package com.code.challenge.wcanlayser.model;

import com.code.challenge.wcanlayser.utils.FileStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@Table(name = "FILE_DETAIL", uniqueConstraints = {@UniqueConstraint(name = "UniqueKey", columnNames = {"userName", "fileName"})})
public class FileDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    Long id;

    @Column
    @JsonProperty
    String userName;
    @Column
    @JsonProperty
    String fileName;
    @Column
    @JsonProperty
    @Enumerated(EnumType.STRING)
    FileStatus status;
    @Column
    @JsonProperty
    String fileLocation;

    public FileDetail(String userName, String fileName, FileStatus status, String fileLocation) {
        this.userName = userName;
        this.fileName = fileName;
        this.status = status;
        this.fileLocation = fileLocation;
    }
}
