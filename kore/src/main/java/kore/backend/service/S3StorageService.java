package kore.backend.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.public-base-url:}")
    private String publicBaseUrl;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(String objectKey, MultipartFile file) throws IOException {

        validateImage(file);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes()));

        return buildPublicUrl(objectKey);
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build());
    }

    public String generateObjectKey(String originalFilename) {
        String sanitizedFilename = originalFilename == null || originalFilename.isBlank()
                ? "foto"
                : Paths.get(originalFilename)
                        .getFileName()
                        .toString()
                        .replaceAll("[\\\\/]+", "_");

        return "fotos/"
                + UUID.randomUUID()
                + "-"
                + sanitizedFilename;
    }

    public String generateObjectKey(Long produtoId, String originalFilename) {
        String sanitizedFilename = originalFilename == null || originalFilename.isBlank()
                ? "foto"
                : Paths.get(originalFilename)
                        .getFileName()
                        .toString()
                        .replaceAll("[\\\\/]+", "_");

        return "produtos/"
                + produtoId
                + "/"
                + UUID.randomUUID()
                + "-"
                + sanitizedFilename;
    }

    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Imagem vazia");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png")
                        && !contentType.equals("image/webp"))) {

            throw new IllegalArgumentException(
                    "Formato inválido. Utilize JPG, PNG ou WEBP.");
        }

        long maxSize = 5 * 1024 * 1024;

        log.info("Tamanho do arquivo: {} bytes", file.getSize());
        log.info("Tamanho máximo permitido: {} bytes", maxSize);

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "A imagem deve possuir no máximo 5 MB");
        }
    }

    public String buildPublicUrl(String objectKey) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.endsWith("/")
                    ? publicBaseUrl + objectKey
                    : publicBaseUrl + "/" + objectKey;
        }

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + objectKey;
    }
}