package com.code.challenge.wcanlayser;

import com.code.challenge.wcanlayser.model.FileDetail;
import com.code.challenge.wcanlayser.repo.FileDetailsRepository;
import com.code.challenge.wcanlayser.utils.FileStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
		locations = "classpath:application-integration.properties"
)
class FileWordCountAnlayserApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	WebApplicationContext webApplicationContext;

	@Autowired
	FileDetailsRepository fileDetailsRepo;

	@Test
	public void testFileUploadVerifySuccess()
	throws Exception{
		MockMultipartFile file =  new MockMultipartFile("file", "MyTest1.txt", MediaType.TEXT_PLAIN_VALUE,
				"This is test file".getBytes());

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();
		mockMvc.perform(multipart("/v1/upload").file(file).param("userName","user")).andExpect(status().isOk());
	}

	@Test
	public void testFileUploadWithEmptyFile()
			throws Exception{
		MockMultipartFile file =  new MockMultipartFile("file", "Empty.txt", MediaType.TEXT_PLAIN_VALUE,
				"".getBytes());

		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();
		mockMvc.perform(multipart("/v1/upload").file(file)).andExpect(status().isBadRequest());
	}

	@Test
	public void testFileUploadWithUnsupportedFileExtension()
			throws Exception{
		MockMultipartFile file =  new MockMultipartFile("file", "Empty.rar", MediaType.TEXT_PLAIN_VALUE,
				"Not a Text file".getBytes());

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();
		mockMvc.perform(multipart("/v1/upload").file(file)).andExpect(status().isBadRequest());
	}

	@Test
	public void testGetStatusEmptyFileName() throws Exception {
		mockMvc.perform(get("/v1/getStatus").param("fileName", "")
				.param("userName", "su_user")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("File name can't be empty")));
	}
	@Test
	public void testGetStatusEmptyUserName() throws Exception {
		mockMvc.perform(get("/v1/getStatus").param("fileName", "FileStatus")
				.param("userName", "")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("User name can't be empty")));
	}

	@Test
	public void testGetStatusCompleted() throws Exception {
		FileDetail file = new FileDetail("su_user", "FileStatus.txt", FileStatus.COMPLETED, "");
		fileDetailsRepo.save(file);
		mockMvc.perform(get("/v1/getStatus").param("fileName", "FileStatus.txt")
				.param("userName", "su_user")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("COMPLETED")));
	}

	@Test
	public void testGetStatusInProgress() throws Exception {
		FileDetail file = new FileDetail("user","FileStatus.txt", FileStatus.IN_PROGRESS, "");
		fileDetailsRepo.save(file);
		mockMvc.perform(get("/v1/getStatus").param("fileName", "FileStatus.txt")
				.param("userName","user")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("IN_PROGRESS")));
	}

	@Test
	public void testGetStatusFailed() throws Exception {
		FileDetail file = new FileDetail("failedUser","FileStatus.txt", FileStatus.FAILED, "");
		fileDetailsRepo.save(file);
		mockMvc.perform(get("/v1/getStatus")
				.param("fileName", "FileStatus.txt")
				.param("userName","failedUser")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("FAILED")));
	}

	@Test
	public void testGetAnalysisResultSuccess() throws Exception {
		String fileName = "MyTestSuccess.txt";
		String user = "user1";
		MockMultipartFile file =  new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
				"This is test file".getBytes());
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();
		mockMvc.perform(multipart("/v1/upload").file(file).param("userName",user))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message",is("File upload success")));

		FileDetail processingFile = fileDetailsRepo.findByFileNameAndUserName(fileName,user);
		int retryCount = 1;
		int maxRetryCount = 5;
		while ((processingFile == null || FileStatus.IN_PROGRESS==processingFile.getStatus()) && retryCount<maxRetryCount) {
			//give sometime for analysis to complete
			Thread.sleep(100);
			processingFile = fileDetailsRepo.findByFileNameAndUserName(fileName,user);
			retryCount++;
		}
		mockMvc.perform(get("/v1/getAnalysis").param("fileName", fileName)
				.param("userName", user)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetAnalysisResultInProgress() throws Exception {
		String fileName = "MyTest.txt";
		String userName = "user";
		FileDetail file = new FileDetail(userName, fileName, FileStatus.IN_PROGRESS, "");
		fileDetailsRepo.save(file);

		FileDetail processingFile = fileDetailsRepo.findByFileNameAndUserName(fileName,userName);
		int retryCount = 1;
		int maxRetryCount = 5;
		while ((processingFile == null || FileStatus.IN_PROGRESS==processingFile.getStatus()) && retryCount<maxRetryCount) {
			//give sometime for analysis to complete
			Thread.sleep(100);
			processingFile = fileDetailsRepo.findByFileNameAndUserName(fileName,userName);
			retryCount++;
		}
		mockMvc.perform(get("/v1/getAnalysis").param("fileName", fileName)
				.param("userName","user")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is1xxInformational())
				.andExpect(jsonPath("$.message",is("File analysis still running try again after sometime!!")));
	}

	@Test
	public void testGetAnalysisResultFailed() throws Exception {
		String fileName = "MyTest.txt";
		String userName = "userFailed";
		FileDetail file = new FileDetail(userName, fileName, FileStatus.FAILED, "");
		fileDetailsRepo.save(file);

		mockMvc.perform(get("/v1/getAnalysis").param("fileName", fileName)
				.param("userName","userFailed")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError())
				.andExpect(jsonPath("$.message",is("File Analysis failed. Please upload the file again!!")));
	}
}
