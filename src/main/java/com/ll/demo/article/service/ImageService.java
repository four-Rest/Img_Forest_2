package com.ll.demo.article.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.Image;
import com.ll.demo.article.repository.ImageRepository;
import com.ll.demo.global.config.S3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final AmazonS3 s3;
    private final S3Util s3Util;



    @Transactional
    public Image create(Article article, MultipartFile multipartFile) throws IOException {

        //저장 경로, 파일 이름 설정
        String imgPath = setImagePath();

        UUID uuid = UUID.randomUUID();

        String fileName = uuid + "_" + multipartFile.getOriginalFilename();

        //멀티파트 파일을 일반 파일로 전환
        File file = convertMultipartFileToFile(multipartFile);


        //S3에 업로드
        try {
            String fullPath = imgPath + fileName;
            s3.putObject(new PutObjectRequest(s3Util.getBucketName(), imgPath + fileName, file));
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }

        //이미지 객체 생성
        Image image = Image.builder()
                .article(article)
                .fileName(fileName)
                .path(imgPath)
                .build();

        //저장
        imageRepository.save(image);

        //로컬에 생성된 파일 삭제
        file.delete();

        return image;

    }

    @Transactional
    public File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }


    @Transactional
    public String setImagePath() {

        //현재 날짜를 폴더 이름으로 지정
        LocalDateTime createdTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String path = createdTime.format(formatter) + "/";

        //path폴더가 있는지 확인

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(s3Util.getBucketName())
                .withPrefix(path)
                .withDelimiter("/");

        ObjectListing objects = s3.listObjects(listObjectsRequest);

        List<String> commonPrefixes = objects.getCommonPrefixes();

        if (commonPrefixes.isEmpty()) {
            //path폴더가 없으면 생성
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(0L);
            objectMetadata.setContentType("application/x-directory");
            PutObjectRequest putObjectRequest = new PutObjectRequest(s3Util.getBucketName(), path, new ByteArrayInputStream(new byte[0]), objectMetadata);

            try {
                s3.putObject(putObjectRequest);
            } catch (AmazonS3Exception e) {
                e.printStackTrace();
            } catch (SdkClientException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    @Transactional
    public void delete(Image image) {
        String path = image.getPath();
        String fileName = image.getFileName();

        try {
            s3.deleteObject(s3Util.getBucketName(), path + fileName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }

        imageRepository.delete(image);
    }

    @Transactional
    public void modify(Image image, MultipartFile multipartFile) throws IOException {

        String newFilePath = setImagePath();

        //기존 이미지 파일 삭제
        String oldFileName = image.getFileName();

        String oldFilePath = image.getPath();

        try {
            s3.deleteObject(s3Util.getBucketName(), oldFilePath + oldFileName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }

        //새 이미지 파일 저장
        UUID uuid = UUID.randomUUID();

        String newFileName = uuid + "_" + multipartFile.getOriginalFilename();

        //멀티파트 파일을 일반 파일로 전환
        File file = convertMultipartFileToFile(multipartFile);


        //S3에 업로드
        try {
            s3.putObject(new PutObjectRequest(s3Util.getBucketName(), newFilePath + newFileName, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }

        //Image객체의 fileName, path를 새 이미지파일로 변경
        image.modifyFileName(newFileName);
        image.modifyPath(newFilePath);

        //로컬에 생성된 파일 삭제
        file.delete();
    }

}