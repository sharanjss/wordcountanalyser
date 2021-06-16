package com.code.challenge.wcanlayser.service;

import com.code.challenge.wcanlayser.model.FileDetail;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface FileService {

    public void uploadFile(MultipartFile file, String userName);
    public Map<String, Integer> getResultOrderedByFrequency(FileDetail fileDetails) throws IOException;
}
