package com.example.minio_demo.service;

import com.example.minio_demo.model.MinioProperties;
import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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
    private void init() throws Exception{
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
        try{
            String bucketName = minioProperties.getBucketName();
            String fileName = file.getOriginalFilename();
            String randomUUID = UUID.randomUUID().toString();

            String extension = "";
            if (fileName != null && fileName.lastIndexOf('.') != -1) {
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
        catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO: " + e.getMessage(), e);
        }
    }

    public InputStream download(String fileName){
        try{
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(minioProperties.getBucketName()).object(fileName).build()
            );
        }
        catch (MinioException e){
            throw new RuntimeException("Error downloading file from MinIO: " + e.getMessage(), e);
        }
        catch (Exception e){
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage(), e);
        }
    }

}
