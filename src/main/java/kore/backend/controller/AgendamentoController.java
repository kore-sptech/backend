package kore.backend.controller;

import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.dto.AgendamentoResponseDTO;
import kore.backend.model.Agendamento;
import kore.backend.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> listar(
            @RequestParam(name = "inicio", required = false) String inicio,
            @RequestParam(name = "fim", required = false) String fim

    ) {

        // fomato de entrada das datas 2026-04-13T03:00:00.000Z

        if (inicio != null && fim != null) {
            return ResponseEntity.ok(agendamentoService.listarEntreDatas(
                    LocalDateTime.parse(inicio, DateTimeFormatter.ISO_DATE_TIME),
                    LocalDateTime.parse(fim, DateTimeFormatter.ISO_DATE_TIME)));
        }
        return ResponseEntity.ok(agendamentoService.listarDaSemana());
    }

    @PostMapping
    public ResponseEntity<Agendamento> criar(@Valid @RequestBody AgendamentoRequestDTO agendamento) {
        return ResponseEntity.ok(agendamentoService.criar(agendamento));
    }
}
