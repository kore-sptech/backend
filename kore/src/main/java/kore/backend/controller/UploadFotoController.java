package kore.backend.controller;

import java.io.IOException;

import kore.backend.model.Foto;
import kore.backend.service.FotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/fotos")
@CrossOrigin(origins = "*")
public class UploadFotoController {

    private final FotoService fotoService;

    public UploadFotoController(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @RequestParam("foto") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Arquivo vazio");
        }

        String contentType = file.getContentType();

        if (!"image/jpeg".equals(contentType)
                && !"image/png".equals(contentType)) {
            return ResponseEntity.badRequest()
                    .body("Formato de arquivo não permitido");
        }

        try {
            Foto foto = this.fotoService.salvar(file);

            return ResponseEntity.ok(foto);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

}
