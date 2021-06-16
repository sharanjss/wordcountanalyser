package com.code.challenge.wcanlayser.service;

import com.code.challenge.wcanlayser.model.FileDetail;
import com.code.challenge.wcanlayser.utils.FileStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.code.challenge.wcanlayser.repo.FileDetailsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * File Analyser helps to analyse the file word count and store analysis details to result folder
 * This is handled using Threadpool Executor.
 */
@Slf4j
@AllArgsConstructor
public class FileAnalyser implements Runnable {

    FileDetailsRepository fileDetailsRepo;
    private String fileName;
    private String userName;
    private String downloadLocation;
    private String resultLocation;

    public FileAnalyser(String userName, String fileName,String downloadLocation, String resultLocation, FileDetailsRepository fileDetailsRepo) {
        this.userName = userName;
        this.fileName = fileName;
        this.downloadLocation = downloadLocation;
        this.resultLocation = resultLocation;
        this.fileDetailsRepo = fileDetailsRepo;
    }

    @Override
    public void run() {
        try {
            log.info("Thread execution");
            //Initially file details loaded to DB as IN_PROGRESS
            FileDetail f = new FileDetail(userName,fileName, FileStatus.IN_PROGRESS, "");
            fileDetailsRepo.save(f);
            getFileWordCount(fileName, fileDetailsRepo);
        } catch (Exception e) {
            //fileDetailsRepo.save(new FileDetail(userName,fileName, FileStatus.FAILED, ""));
            fileDetailsRepo.updateStatus(userName,fileName, FileStatus.FAILED, "");
            log.error("Exception occurred during analysis {0}", e);
        }
    }

    /**
     * Read the file from downloaded folder and do the word count for each word
     * Save the file details in DB with Completed/Failure status and save the result file in result folder
     * @param fileName
     * @param fileDetailsRepo
     * @throws IOException
     */
    private void getFileWordCount(String fileName, FileDetailsRepository fileDetailsRepo) throws IOException {
        long startTime = System.currentTimeMillis();
        log.info("Starting Analysis at {}.", startTime);
        Path location = Paths.get(downloadLocation + fileName);
        Map<String, Long> wordMap = Files.lines(location)
                .flatMap(lines -> Arrays.stream(lines.trim().split("\\s"))) //Split word with space
                .map(w -> w.replaceAll("[.,]$+", "").toLowerCase()) //ignore the trailing dot(.) and comma(,)
                .filter(w -> w.length() > 0)
                .map(w -> new AbstractMap.SimpleEntry<>(w, 1))
                .collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey, Collectors.counting()));
        ObjectMapper mapper = new ObjectMapper();
        //Store the result into result folder
        String resultFileName = "result_"+fileName;
        mapper.writeValue(new File(resultLocation +File.separator+ resultFileName), wordMap);
        //fileDetailsRepo.save(new FileDetail(userName,fileName, FileStatus.COMPLETED, resultLocation + File.separator+ resultFileName));
        fileDetailsRepo.updateStatus(userName,fileName, FileStatus.COMPLETED, resultLocation + File.separator+ resultFileName);
        log.info("Analysis completed {}.", System.currentTimeMillis());
        log.debug("Total time taken for analysis: {}", (System.currentTimeMillis() - startTime) + "ms");
    }
}
