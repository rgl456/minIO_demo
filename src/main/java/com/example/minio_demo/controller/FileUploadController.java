package com.example.minio_demo.controller;

import com.example.minio_demo.service.FileService;
import io.minio.GetObjectResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    private final FileService fileService;

    public FileUploadController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty!"));
        }
        try{
            String fileName = fileService.uploadFile(file);
            return new ResponseEntity<>(Map.of(fileName, " file uploaded successfully!"), HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(ResponseEntity.internalServerError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName){
        try {
            InputStream fileInputStream = fileService.download(fileName);
            byte[] content = fileInputStream.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(content);
        }
        catch (ErrorResponseException e) {
            System.err.println("File not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error streaming file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
