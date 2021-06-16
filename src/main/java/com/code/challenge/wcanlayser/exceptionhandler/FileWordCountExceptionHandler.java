package com.code.challenge.wcanlayser.exceptionhandler;

import com.code.challenge.wcanlayser.model.ResponseMessage;
import com.fasterxml.jackson.core.JsonParseException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Exception handling
 */
@ControllerAdvice
public class FileWordCountExceptionHandler extends ResponseEntityExceptionHandler {
    @Value("${spring.servlet.multipart.max-file-size}")
    String fileSize;

    @ExceptionHandler(value = {MaxUploadSizeExceededException.class, FileSizeLimitExceededException.class})
    protected ResponseEntity<Object> handleException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, new ResponseMessage(String.format("File size exceeded. " +
                        "Maximum permitted size is %s", fileSize)),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {FileNotFoundException.class, JsonParseException.class})
    protected ResponseEntity<Object> handleFileNotFoundException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, new ResponseMessage("Analysis result not found.. Please upload the file. "),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = DuplicateFileException.class)
    protected ResponseEntity<Object> handleDuplicateException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex,  new ResponseMessage(ex.getMessage()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = IOException.class)
    protected ResponseEntity<Object> handleIOException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex,  new ResponseMessage("Something went wrong!!! Please try again. " ),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
