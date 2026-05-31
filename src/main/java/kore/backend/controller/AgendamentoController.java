package kore.backend.controller;

import kore.backend.config.OpenApiConfig;
import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.dto.AgendamentoResponseDTO;
import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEMA_NAME)
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> listar(
            @RequestParam(name = "inicio", required = false) String inicio,
            @RequestParam(name = "fim", required = false) String fim,
            @AuthenticationPrincipal Usuario usuario

    ) {

        return ResponseEntity.ok(agendamentoService.listarEntreDatas(
                LocalDateTime.parse(inicio, DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse(fim, DateTimeFormatter.ISO_DATE_TIME), usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(@PathVariable Long id,
            @Valid @RequestBody AgendamentoRequestDTO agendamento, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(agendamentoService.atualizar(id, agendamento, usuario));
    }

    @PostMapping
    public ResponseEntity<Agendamento> criar(@Valid @RequestBody AgendamentoRequestDTO agendamento,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(agendamentoService.criar(agendamento, usuario));
    }
}
