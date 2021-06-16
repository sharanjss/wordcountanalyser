package com.code.challenge.wcanlayser.controller;

import com.code.challenge.wcanlayser.model.FileDetail;
import com.code.challenge.wcanlayser.model.ResponseMessage;
import com.code.challenge.wcanlayser.repo.FileDetailsRepository;
import com.code.challenge.wcanlayser.service.FileService;
import com.code.challenge.wcanlayser.utils.FileStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * File word count controller deals with File upload, File status and File word count analysis
 */
@RestController
@RequestMapping("/v1")
@Slf4j
public class FileWordCountController {
    public static final String TXT = ".txt";
    @Autowired
    FileService fileService;

    @Autowired
    FileDetailsRepository fileDetailsRepo;

    /**
     * File Upload - only support txt format
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST)
    public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile file,
                                        @RequestParam("userName") String userName) {
        ResponseEntity<?> errResponse = validateUploadRequest(file, userName);
        if (errResponse != null) return errResponse;
        log.info("Starting file upload");
        fileService.uploadFile(file, userName);
        log.info("File upload Completed");
        return ResponseEntity.ok(new ResponseMessage("File upload success"));
    }

    /**
     * Valid file is empty and it is of txt format
     *
     * @param file
     * @return ErrorResponse object with error code
     */
    private ResponseEntity<?> validateUploadRequest(MultipartFile file, String userName) {
        //Check file is empty and it is .txt format
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("File is empty. Please upload correct file"));
        }
        if (!file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1).equalsIgnoreCase("txt")) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Please upload text file only"));
        }
        if(StringUtils.isEmpty(userName)){
            return ResponseEntity.badRequest().body(new ResponseMessage("User name can't be empty"));
        }
        return null;
    }

    /**
     * Get the file status for given file
     *
     * @param fileName
     * @return file status
     */
    @RequestMapping(value = "/getStatus", method = RequestMethod.GET)
    public ResponseEntity<?> getFileStatus(@RequestParam("fileName") String fileName,
                                           @RequestParam("userName") String userName) {
        ResponseEntity<?> responseEntity = validate(fileName, userName);
        if(responseEntity!=null) return responseEntity;
        String fileNameWithExtension = fileName.contains(TXT) ? fileName : fileName + TXT;
        log.info("Getting file status");
        FileDetail existingFile = fileDetailsRepo.findByFileNameAndUserName(fileNameWithExtension, userName);
        if (existingFile != null) {
            return ResponseEntity.ok(new ResponseMessage(existingFile.getStatus().name()));
        } else {
            return ResponseEntity.badRequest().body(new ResponseMessage("Please check file name and user name is correct"));
        }
    }

    private ResponseEntity<?> validate(String fileName, String userName) {

        if(StringUtils.isEmpty(fileName) && StringUtils.isEmpty(userName)){
            return ResponseEntity.badRequest().body(new ResponseMessage("File name and User name can't be empty."));
        }
        if(StringUtils.isEmpty(fileName)){
            return ResponseEntity.badRequest().body(new ResponseMessage("File name can't be empty"));
        }
        if (StringUtils.isEmpty(userName)){
            return ResponseEntity.badRequest().body(new ResponseMessage("User name can't be empty"));
        }
        return null;

    }

    /**
     * File analyser
     *
     * @param fileName
     * @return Map of word count in order
     */
    @RequestMapping(value = "/getAnalysis", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<?> getAnalysis(@RequestParam("fileName") String fileName,
                                         @RequestParam("userName") String userName) {
        ResponseEntity<?> responseEntity = validate(fileName, userName);
        if(responseEntity!=null) return responseEntity;

        String fileNameWithExtension = fileName.contains(TXT) ? fileName : fileName + TXT;
        log.info("File Analysis started");
        FileDetail existingFile = fileDetailsRepo.findByFileNameAndUserName(fileNameWithExtension, userName);
        if (existingFile != null) {
            return getAnalysisResponseEntity(existingFile);
        } else {
            log.warn("File not found... {} ", fileName);
            return ResponseEntity.badRequest().body(new ResponseMessage("Please provide the correct file name"));
        }
    }

    private ResponseEntity<?> getAnalysisResponseEntity(FileDetail existingFile) {
        if (FileStatus.COMPLETED == existingFile.getStatus()) {
            Map<String, Integer> sortedFileResult = null;
            try {
                sortedFileResult = fileService.getResultOrderedByFrequency(existingFile);
            } catch (IOException e) {
                log.info("File Analysis failed... {}", e);
                return ResponseEntity.internalServerError().body(new ResponseMessage("File Analysis failed. Please upload the file again!!"));
            }
            log.info("File Analysis completed");
            return ResponseEntity.ok(sortedFileResult);
        } else if (FileStatus.IN_PROGRESS == existingFile.getStatus()) {
            log.info("File Analysis In-Progress...");
            return ResponseEntity.status(HttpStatus.PROCESSING).body(new ResponseMessage("File analysis still running try again after sometime!!"));
        } else {
            log.info("File Analysis failed...");
            return ResponseEntity.internalServerError().body(new ResponseMessage("File Analysis failed. Please upload the file again!!"));
        }
    }
}
