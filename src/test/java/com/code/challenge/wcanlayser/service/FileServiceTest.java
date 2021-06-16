package com.code.challenge.wcanlayser.service;

import com.code.challenge.wcanlayser.exceptionhandler.DuplicateFileException;
import com.code.challenge.wcanlayser.model.FileDetail;
import com.code.challenge.wcanlayser.repo.FileDetailsRepository;
import com.code.challenge.wcanlayser.utils.FileStatus;
import com.code.challenge.wcanlayser.utils.FileUtils;
import org.aspectj.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.*;

public class FileServiceTest {

    @Mock
    ThreadPoolExecutor threadPoolExecutor;

    @Mock
    FileDetailsRepository fileDetailsRepo;

    String downloadLocation = FileUtils.CURRENT_DIR+"test"+File.separator;
    String resultLocation = FileUtils.CURRENT_DIR+"test"+File.separator;

    @InjectMocks
    FileService fileService = new FileServiceImpl();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(fileService, "downloadLocation", downloadLocation);
        ReflectionTestUtils.setField(fileService, "resultLocation", resultLocation);

        FileUtils.createDirectory(downloadLocation);
        FileUtils.createDirectory(resultLocation);
        try {
            String fileName = "testResult.txt";
            MockMultipartFile file =  new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
                    "{\"big\":1,\"data\":3,\"mass\":2,\"info\":1,\"12C\":2}".getBytes());
            Path location = Paths.get(resultLocation+ fileName);
            Files.copy(file.getInputStream(), location, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void teadDown(){
        File dir = new File(FileUtils.CURRENT_DIR+"test");
        if (dir.exists()) {
            FileSystemUtils.deleteRecursively(dir);
        }
    }

    @Test
    public void testUploadFile(){
        String fileName = "myTest.txt";
        String userName = "userName";
        MockMultipartFile file =  new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
                "This is test file".getBytes());
        when(fileDetailsRepo.findByFileNameAndUserName(fileName,userName)).thenReturn(null);
        doNothing().when(threadPoolExecutor).execute(Mockito.any(FileAnalyser.class));
        fileService.uploadFile(file, userName);
        verify(threadPoolExecutor).execute(Mockito.any(FileAnalyser.class));
    }

    @Test
    public void testUploadDuplicateFile(){
        String fileName = "myTest.txt";
        String userName = "userName";
        MockMultipartFile file =  new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
                "This is test file".getBytes());
        when(fileDetailsRepo.findByFileNameAndUserName(fileName,userName)).thenReturn(new FileDetail(userName, fileName,FileStatus.COMPLETED,""));
        Assertions.assertThrows(DuplicateFileException.class, ()->fileService.uploadFile(file, userName));
    }

    @Test
    public void testGetResultOrderedByFrequency() throws IOException {
        String fileName="testResult.txt";
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("big",1);
        expected.put("info",1);
        expected.put("mass",2);
        expected.put("12C",2);
        expected.put("data", 3);
        FileDetail fileDetails = new FileDetail("user",fileName, FileStatus.COMPLETED,
                resultLocation+ fileName);
        Map<String, Integer> result = fileService.getResultOrderedByFrequency(fileDetails);
        Assertions.assertEquals(expected, result);

    }
}
