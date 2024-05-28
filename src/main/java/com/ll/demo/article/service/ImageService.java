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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

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



//    @Transactional
//    public Image create(Article article, MultipartFile multipartFile) throws IOException {
//
//        //저장 경로, 파일 이름 설정
//        String imgPath = setImagePath();
//
//        UUID uuid = UUID.randomUUID();
//
//        String fileName = uuid + "_" + multipartFile.getOriginalFilename();
//
//        //멀티파트 파일을 일반 파일로 전환
//        File file = convertMultipartFileToFile(multipartFile);
//
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        Thumbnails.of(file)
//                .scale(1.0)
//                .outputQuality(0.8)
//                .toOutputStream(os);
//
//        byte[] optimizedImageBytes = os.toByteArray();
//
//        File optimizedFile = new File(file.getParentFile(),fileName);
//        try (FileOutputStream optimizedFileStream = new FileOutputStream(optimizedFile)) {
//            optimizedFileStream.write(optimizedImageBytes);
//       }
//
//        //Object storage에 업로드
//        try {
//            s3.putObject(new PutObjectRequest(s3Util.getBucketName(), imgPath + "/" + fileName, file)
//                    .withCannedAcl(CannedAccessControlList.PublicRead));
//        } catch (AmazonS3Exception e) {
//            e.printStackTrace();
//        } catch(SdkClientException e) {
//            e.printStackTrace();
//        }
//
//        //이미지 객체 생성
//        Image image = Image.builder()
//                .article(article)
//                .fileName(fileName)
//                .path(imgPath)
//                .build();
//
//        //저장
//        imageRepository.save(image);
//
//        //로컬에 생성된 파일 삭제
//        file.delete();
//        // optimizedFile.delete();
//
//
//        return image;
//
//    }

    @Transactional
    public Image create(Article article, MultipartFile multipartFile) throws IOException {

        //저장 경로, 파일 이름 설정
        String imgPath = setImagePath();

        // 멀티파트 파일을 PNG 형식의 파일로 전환, PNG 파일의 이름을 받아옴
        File pngFile = convertMultipartFileToPngFile(multipartFile);
        String pngFileName = pngFile.getName();

        //Object storage에 업로드
        try {
            s3.putObject(new PutObjectRequest(s3Util.getBucketName(), imgPath + "/" + pngFileName, pngFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }

        //이미지 객체 생성
        Image image = Image.builder()
                .article(article)
                .fileName(pngFileName)
                .path(imgPath)
                .build();

        //저장
        imageRepository.save(image);

        //로컬에 생성된 파일 삭제
        pngFile.delete();

        return image;

    }

//    @Transactional
//    public File convertMultipartFileToFile(MultipartFile file) throws IOException {
//        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
//        FileOutputStream fos = new FileOutputStream(convertedFile);
//        fos.write(file.getBytes());
//        fos.close();
//        return convertedFile;
//    }


    @Transactional
    public File convertMultipartFileToPngFile(MultipartFile multipartFile) throws  IOException {
        // 파일명에서 확장자를 제외한 부분과 새로운 UUID를 결합하여 PNG 파일명 생성
        String originalFileName= Objects.requireNonNull(multipartFile.getOriginalFilename()).split("\\.")[0];
        String newFileName = UUID.randomUUID() + "_" + originalFileName + ".png";
        File convertedFile = new File(newFileName);

        BufferedImage image = ImageIO.read(multipartFile.getInputStream());

        // PNG 형식으로 파일에 쓰기 (TwelveMonkeys 라이브러리 사용)
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if(!writers.hasNext()) {
            throw new IllegalArgumentException("No writers : png");
        }
        ImageWriter writer = writers.next();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(convertedFile)) {
            writer.setOutput(ios);

            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            // 무손실 압축이므로 압축 품질을 설정하지 않음

            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }

        return convertedFile;
    }

    @Transactional
    public String setImagePath() {

        //현재 날짜를 폴더 이름으로 지정
        LocalDateTime createdTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String path = createdTime.format(formatter);

        //path폴더가 있는지 확인
        String folderPath = path + "/";

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(s3Util.getBucketName())
                .withPrefix(folderPath)
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
            s3.deleteObject(s3Util.getBucketName(), path + "/" + fileName);
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
            s3.deleteObject(s3Util.getBucketName(), oldFilePath + "/" + oldFileName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }

        //새 이미지 파일 저장
//        UUID uuid = UUID.randomUUID();
//
//        String newFileName = uuid + "_" + multipartFile.getOriginalFilename();
//
//        //멀티파트 파일을 일반 파일로 전환
//        File file = convertMultipartFileToFile(multipartFile);

        File pngFile = convertMultipartFileToPngFile(multipartFile);
        String newPngFileName = pngFile.getName();


        //Object storage에 업로드
        try {
            s3.putObject(new PutObjectRequest(s3Util.getBucketName(), newFilePath + "/" + newPngFileName, pngFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }

        //Image객체의 fileName, path를 새 이미지파일로 변경
        image.modifyFileName(newPngFileName);
        image.modifyPath(newFilePath);

        //로컬에 생성된 파일 삭제
        pngFile.delete();
    }

}