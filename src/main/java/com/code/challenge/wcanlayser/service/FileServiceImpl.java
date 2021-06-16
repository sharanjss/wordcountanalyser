package com.code.challenge.wcanlayser.service;

import com.code.challenge.wcanlayser.exceptionhandler.DuplicateFileException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.code.challenge.wcanlayser.utils.FileUtils;
import com.code.challenge.wcanlayser.model.FileDetail;
import com.code.challenge.wcanlayser.repo.FileDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * File service class for upload and initializing file word count analysis by
 * adding to Threadpool executor
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService{

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    FileDetailsRepository fileDetailsRepo;

    @Value("${download.location}")
    String downloadLocation;

    @Value("${result.location}")
    String resultLocation;

    /**
     * create required dirs like download and result dirs
     */
    @PostConstruct
    private void init() {
        if(downloadLocation.isEmpty()){
            downloadLocation= FileUtils.CURRENT_DIR + "download\\";
        }
        if(resultLocation.isEmpty()){
            resultLocation= FileUtils.CURRENT_DIR + "result\\";
        }
        FileUtils.createDirectory(downloadLocation);
        FileUtils.createDirectory(resultLocation);
    }

    /**
     * Mulitpart text file will be downloaded to download dir and added to Threadpool executor
     * for Word Count analysis
     * @param file
     */
    @Override
    public void uploadFile(MultipartFile file, String userName) {
        try {
            log.info("UploadFile Method");
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            log.debug("File name : " + fileName);
            //Check file is already loaded into DB
            FileDetail existingFile = fileDetailsRepo.findByFileNameAndUserName(fileName, userName);
            if (existingFile == null) {
                log.info("File upload started at Location:  {}, File name: {}", downloadLocation, fileName);
                Path location = Paths.get(downloadLocation + File.separator + fileName);
                //Copy file to download dir
                Files.copy(file.getInputStream(), location, StandardCopyOption.REPLACE_EXISTING);
                //Adding to Threadpool executor
                threadPoolExecutor.execute(new FileAnalyser( userName,fileName, downloadLocation, resultLocation,  fileDetailsRepo));
            } else {
                log.warn("File alreday exist {}, please upload different file", existingFile);
                throw new DuplicateFileException(String.format("%s file alreday exist, please upload different file",existingFile.getFileName()));
            }

        } catch (IOException e) {
            log.error("Error!! {}", e);
        }
    }

    /**
     * Sort the File<word, count> result by value
     * @param fileDetails
     * @return Map of word count
     * @throws IOException
     */
    @Override
    public Map<String, Integer> getResultOrderedByFrequency(FileDetail fileDetails) throws IOException {
        log.info("File word count Analysis started...");
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream input = new FileInputStream(fileDetails.getFileLocation());
        Map<String, Integer> fileAnalysis = objectMapper.readValue(input, Map.class);
        //Sort by frequency
        Map<String, Integer> sortedfileAnalysis = fileAnalysis.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (w1, w2) -> w1, LinkedHashMap::new));
        log.info("File word count Analysis completed, returning results.");
        return sortedfileAnalysis;
    }

    /**
     * Shutdown the Threadpool executor before the bean destory
     */
    @PreDestroy
    public void preDestory(){
        threadPoolExecutor.shutdown();
    }
}
