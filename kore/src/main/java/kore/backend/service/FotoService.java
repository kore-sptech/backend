package kore.backend.service;

import java.io.IOException;

import kore.backend.model.Foto;
import kore.backend.repository.FotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FotoService {

    private final FotoRepository fotoRepository;
    private final S3StorageService s3StorageService;

    public FotoService(FotoRepository fotoRepository, S3StorageService s3StorageService) {
        this.fotoRepository = fotoRepository;
        this.s3StorageService = s3StorageService;
    }

    public Foto salvar(MultipartFile file) throws IOException {
        String objectKey = this.s3StorageService.generateObjectKey(file.getOriginalFilename());
        String imageUrl = this.s3StorageService.upload(objectKey, file);

        Foto foto = new Foto(imageUrl, objectKey);

        return this.fotoRepository.save(foto);
    }
}
