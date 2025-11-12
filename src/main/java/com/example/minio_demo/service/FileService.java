package com.example.minio_demo.service;

import com.example.minio_demo.model.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class FileService {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    public FileService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    private void init() throws Exception {
        try{
            String bucketName = minioProperties.getBucketName();
            boolean bucketExists = minioClient.bucketExists(
              BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if(!bucketExists){
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                System.out.println("MinIO Bucket '" + bucketName + "' created successfully.");
            }else{
                System.out.println("MinIO Bucket '" + bucketName + "' already exists.");
            }
        }
        catch(Error e){
            throw new RuntimeException("Could not initialize MinIO bucket!", e);
        }
    }

    public String uploadFile(MultipartFile file){
        String bucketName = minioProperties.getBucketName();
        String fileName = file.getOriginalFilename();
        String randomUUID = UUID.randomUUID().toString();

        String extension = "";
        if(fileName != null && fileName.lastIndexOf('.')!=-1){
            extension = fileName.substring(fileName.lastIndexOf('.'));
        }

        String newFileName = randomUUID + extension;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(newFileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        return newFileName;
    }
}
