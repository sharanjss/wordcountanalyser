package com.code.challenge.wcanlayser.repo;

import com.code.challenge.wcanlayser.model.FileDetail;
import com.code.challenge.wcanlayser.utils.FileStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

@Transactional
public interface FileDetailsRepository extends CrudRepository<FileDetail, Long> {
    FileDetail findByFileNameAndUserName(String fileName, String userName);

    @Modifying
    @Query("UPDATE FileDetail f SET f.status = :status, f.fileLocation = :fileLocation WHERE f.userName = :userName AND f.fileName = :fileName")
    void updateStatus(@Param("userName") String userName, @Param("fileName") String fileName,
                      @Param("status") FileStatus status, @Param("fileLocation") String fileLocation);
}
