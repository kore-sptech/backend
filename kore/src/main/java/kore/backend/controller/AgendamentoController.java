package kore.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kore.backend.config.OpenApiConfig;
import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.dto.AgendamentoResponseDTO;
import kore.backend.dto.HorarioDisponivelDTO;
import kore.backend.model.Agendamento;
import kore.backend.model.Usuario;
import kore.backend.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@Tag(name = "Agendamentos", description = "Gestão de agendamentos, horários e status")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEMA_NAME)
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Operation(summary = "Listar agendamentos", description = "Lista agendamentos entre datas informadas para o usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamentos listados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de data inválidos")
    })
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

    @Operation(summary = "Atualizar agendamento", description = "Atualiza os dados de um agendamento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(@PathVariable Long id,
            @Valid @RequestBody AgendamentoRequestDTO agendamento, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(agendamentoService.atualizar(id, agendamento, usuario));
    }

    @Operation(summary = "Criar agendamento", description = "Cria um novo agendamento para o usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Agendamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<Agendamento> criar(@Valid @RequestBody AgendamentoRequestDTO agendamento,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(agendamentoService.criar(agendamento, usuario));
    }

    @Operation(summary = "Confirmar agendamento", description = "Marca o agendamento como confirmado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Confirmado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/confirmar/{agendamentoId}")
    public ResponseEntity<Void> confirmar(
            @PathVariable Long agendamentoId, @AuthenticationPrincipal Usuario usuario) {
        this.agendamentoService.confirmar(agendamentoId, usuario);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Confirmar pagamento do agendamento", description = "Marca o pagamento do agendamento como confirmado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pagamento confirmado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/confirmar_pagamento/{agendamentoId}")
    public ResponseEntity<Void> confirmarPagamento(
            @PathVariable Long agendamentoId, @AuthenticationPrincipal Usuario usuario) {
        this.agendamentoService.confirmarPagamento(agendamentoId, usuario);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cancelado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/cancelar/{agendamentoId}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long agendamentoId, @AuthenticationPrincipal Usuario usuario) {
        this.agendamentoService.cancelar(agendamentoId, usuario);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Próximo horário disponível", description = "Retorna o próximo horário disponível para agendamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horário retornado com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum horário disponível encontrado")
    })
    @GetMapping("/proximo-disponivel")
    public ResponseEntity<HorarioDisponivelDTO> proximoDisponivel(
            @AuthenticationPrincipal Usuario usuario) {

        HorarioDisponivelDTO horario = agendamentoService
                .proximoDisponivel(usuario);

        return ResponseEntity.ok(horario);
    }

}
