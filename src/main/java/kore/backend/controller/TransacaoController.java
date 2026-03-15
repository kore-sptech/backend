package kore.backend.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kore.backend.dto.MetricasDTO;
import kore.backend.dto.TransacaoDTO;
import kore.backend.model.enums.CategoriaTransacao;
import kore.backend.model.Transacao;
import kore.backend.service.TransacaoService;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Transacao>> listarTransacoes(
            @PageableDefault(size = 4, page = 0, sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable) {
        List<Transacao> transacoes = transacaoService.listarTransacoes(pageable);
        return ResponseEntity.ok(transacoes);
    }

    @PostMapping
    public ResponseEntity<Transacao> criarTransacao(@RequestBody TransacaoDTO transacaoDTO) {
        System.out.println(transacaoDTO);

        Transacao transacao = this.transacaoService.criarTransacao(transacaoDTO);

        return ResponseEntity.ok(transacao);
    }

    @GetMapping("/metricas")
    public ResponseEntity<MetricasDTO> calcularMetricas() {
        MetricasDTO metricas = this.transacaoService.calcularMetricas();

        System.out.println(metricas);
        return ResponseEntity.ok(metricas);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<String>> listarGastosPorCategoria() {
        return ResponseEntity.ok(List.of(CategoriaTransacao.INSUMOS.name(),
                CategoriaTransacao.MATERIAS.name(), CategoriaTransacao.OUTROS.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTransacao(@PathVariable Long id) {
        transacaoService.deletarTransacao(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transacao> atualizarTransacao(@PathVariable Long id, @RequestBody TransacaoDTO transacaoDTO) {
        Transacao transacaoAtualizada = transacaoService.atualizarTransacao(id, transacaoDTO);
        return ResponseEntity.ok(transacaoAtualizada);
    }

}
