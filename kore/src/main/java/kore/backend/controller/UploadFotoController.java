package kore.backend.controller;

import java.io.IOException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Fotos", description = "Upload e gerenciamento de imagens")
public class UploadFotoController {

    private final FotoService fotoService;

    public UploadFotoController(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @Operation(summary = "Upload de foto", description = "Realiza o upload de uma foto (JPEG ou PNG)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto enviada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio ou formato não permitido"),
            @ApiResponse(responseCode = "500", description = "Erro ao processar o upload")
    })
    @PostMapping
    public ResponseEntity<Foto> upload(
            @RequestParam("foto") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        String contentType = file.getContentType();

        if (!"image/jpeg".equals(contentType)
                && !"image/png".equals(contentType)) {
            throw new IllegalArgumentException("Formato de arquivo não permitido");
        }

        try {
            Foto foto = this.fotoService.salvar(file);

            return ResponseEntity.ok(foto);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o upload da foto", e);
        }
    }

}
