package com.ll.demo.article.service;

import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.Image;
import com.ll.demo.article.repository.ArticleRepository;
import com.ll.demo.article.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;

    @Transactional
    public Image create(Article article, MultipartFile file) throws IOException {

        String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files";

        UUID uuid = UUID.randomUUID();

        String fileName = uuid + "_" + file.getOriginalFilename();

        File saveFile = new File(projectPath, fileName);

        file.transferTo(saveFile);

        Image image = Image.builder()
                .article(article)
                .fileName(fileName)
                .build();

        imageRepository.save(image);
        return image;
    }

    @Transactional
    public void delete(Image image) throws IOException {

        String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files";
        String fileName = image.getFileName();
        Path filePath = Paths.get(projectPath, fileName);

        Files.deleteIfExists(filePath);

        imageRepository.delete(image);
    }

    @Transactional
    public void modify(Image image, MultipartFile multipartFile) throws IOException {

        Article article = image.getArticle();
        Image currentImage = image;
        delete(currentImage);
        create(article, multipartFile);
    }
}