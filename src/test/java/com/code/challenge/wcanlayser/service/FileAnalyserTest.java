package com.code.challenge.wcanlayser.service;

import com.code.challenge.wcanlayser.utils.FileStatus;
import com.code.challenge.wcanlayser.utils.FileUtils;
import com.code.challenge.wcanlayser.model.FileDetail;
import com.code.challenge.wcanlayser.repo.FileDetailsRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FileAnalyserTest {

    @Mock
    FileDetailsRepository fileDetailsRepo;

    private static String downloadLocation="test"+File.separator+"download"+File.separator;

    private static String resultLocation="test"+File.separator+"result"+File.separator;

    private static String fileName = "myTest.txt";
    private static String userName = "user";

    @InjectMocks
    FileAnalyser fileAnalyser = new FileAnalyser(userName, fileName, downloadLocation, resultLocation, fileDetailsRepo);

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        FileUtils.createDirectory(FileUtils.CURRENT_DIR+downloadLocation);
        FileUtils.createDirectory(FileUtils.CURRENT_DIR+resultLocation);
    }


    @AfterEach
    public void teadDown(){
        File dir = new File(FileUtils.CURRENT_DIR+"test");
        if (dir.exists()) {
            FileSystemUtils.deleteRecursively(dir);
        }
    }

    @Test
    public void testRun() throws IOException {
        FileDetail fileAnalysisInProgress = new FileDetail(userName, fileName, FileStatus.IN_PROGRESS, "");
        FileDetail fileAnalysisComplete = new FileDetail(userName, fileName, FileStatus.COMPLETED, "");
        when(fileDetailsRepo.save(fileAnalysisInProgress)).thenReturn(fileAnalysisInProgress);
        doNothing().when(fileDetailsRepo).updateStatus(userName, fileName, FileStatus.COMPLETED,resultLocation);


        MockMultipartFile file =  new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
                "This is test file".getBytes());
        Path location = Paths.get(FileUtils.CURRENT_DIR+ downloadLocation, File.separator, StringUtils.cleanPath(file.getOriginalFilename()));
        Files.copy(file.getInputStream(), location, StandardCopyOption.REPLACE_EXISTING);
        fileAnalyser.run();
        verify(fileDetailsRepo, Mockito.times(1)).save(any(FileDetail.class));
        verify(fileDetailsRepo, Mockito.times(1)).updateStatus(anyString(),anyString(),any(),anyString());
    }
}
