**File Word Count Analayser:**
 
 Api to calculate the word frequency for a given text file. 
 
 **Configuration:**
 1. Max upload file size is 10MB(Property Key: "spring.servlet.multipart.max-file-size" and "spring.servlet.multipart.max-request-size")
 2. Default location for download is current_working_directory/download or configure full path using property key "download.location"
 3. Default location for result is current_working_directory/result or configure full path using property key "result.location"
 4. Max Thread count is 10 to perform asynchronous analysis (Property Key: "max.thread.count")
 5. There are h2 db configuration
 6. Configured port is 9999. You can change with key "server.port"

Endpoint can be accessed via Rest API clients like Postman and Insomnia etc.
 
 
**End points**

 1. File upload:
 
	URL: /v1/upload
	
	Input 1: Multipart  file(txt file)
	
	Input 2: String     User Name
	
	Please note, this is only text file. 
 
 2. Get the file analysis status
 
	URL: /v1/getStatus
	
	Input 1: String     File name
	
	Input 2: String     User Name
	
	Please note, file upload within ms.So, It is difficult to catch the IN-Progress status in manual validation. 
 
 3. Get the file analysis details
 
	URL: /v1/getAnalysis 
	
	Input 1: String     File name
	
	Input 2: String     User Name
 
 
**Running Application:**

Clone the github project, import to eclipse/intellij
Main File: FileWordCountAnlayserApplication.java

During start up application will create 2 directories, download and result. 
1. The download folder is used to download the file submitted by user using /v1/upload. 
2. The result folder is used to store the anlaysis result.

Running Junit, please set the absolute path for download and result


**Addation note:**

Need to configure the corn job to clean up the download and result folder files older than 30days. Unix command given below. We need to change download and result directory accordingly.


find ./download/ -name "*.txt" -type f -mtime +30  -exec rm -rf {} \; 

find ./result/ -name "*.txt" -type f -mtime +30  -exec rm -rf {} \; 

