package kore.backend.controller;

import kore.backend.dto.FotoRequestDTO;
import kore.backend.model.Foto;
import kore.backend.service.FotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/fotos")
@CrossOrigin(origins = "*")
public class UploadFotoController {

    private final FotoService fotoService;

    private final String UPLOAD_DIR = "../front-end/public/uploads/";

    public UploadFotoController(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @RequestParam("foto") MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists())
                dir.mkdirs();

            // Save file locally
            String fileName = UUID.randomUUID().toString().concat(file.getOriginalFilename());
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());

            FotoRequestDTO request = new FotoRequestDTO(fileName, "/public/uploads/".concat(fileName));
            Foto foto = this.fotoService.salvar(request);

            return ResponseEntity.ok(foto);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

}
