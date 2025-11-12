package com.example.minio_demo.controller;

import com.example.minio_demo.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

}
