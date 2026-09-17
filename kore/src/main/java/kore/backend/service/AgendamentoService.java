package kore.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import kore.backend.model.Usuario;
import kore.backend.model.enums.StatusAgendamento;
import kore.backend.repository.FotoRepository;
import kore.backend.repository.ItemRepository;

import kore.backend.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.dto.AgendamentoResponseDTO;
import kore.backend.dto.HorarioDisponivelDTO;
import kore.backend.exception.AgendamentoNaoEncontradoException;
import kore.backend.model.Agendamento;
import kore.backend.model.Foto;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.service.policy.AgendamentoPolicy;
import kore.backend.service.validation.AgendamentoValidationService;

@Service
public class AgendamentoService {

    private final FotoRepository fotoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ItemRepository itemRepository;
    private final TransacaoRepository transacaoRepository;
    private final AgendamentoValidationService agendamentoValidationService;
    private final AgendamentoPolicy agendamentoPolicy;

    private static final Duration DURACAO_PADRAO = Duration.ofMinutes(30);

    public AgendamentoService(AgendamentoRepository agendamentoRepository, FotoRepository fotoRepository,
            ItemRepository itemRepository, TransacaoRepository transacaoRepository,
            AgendamentoValidationService agendamentoValidationService, AgendamentoPolicy agendamentoPolicy) {
        this.agendamentoRepository = agendamentoRepository;
        this.fotoRepository = fotoRepository;
        this.itemRepository = itemRepository;
        this.transacaoRepository = transacaoRepository;
        this.agendamentoValidationService = agendamentoValidationService;
        this.agendamentoPolicy = agendamentoPolicy;
    }

    @Transactional
    public Agendamento criar(AgendamentoRequestDTO request, Usuario usuario) {
        List<Foto> fotos = this.fotoRepository.findAllById(request.getReferencias());

        if (fotos.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma foto encontrada para as referências fornecidas");
        }

        agendamentoPolicy.validarCriacao(request, usuario);

        Agendamento agendamento = Agendamento.builder().referencias(fotos)
                .cliente(request.getCliente()).telefone(request.getTelefone())
                .formaPagamento(request.getFormaPagamento()).preco(request.getPreco()).inicio(request.getInicio())
                .fim(request.getFim()).usuario(usuario).status(StatusAgendamento.PENDENTE).build();

        for (Foto foto : fotos) {
            foto.setAgendamento(agendamento);
        }

        this.fotoRepository.saveAll(fotos);

        return this.agendamentoRepository.save(agendamento);
    }

    @Transactional
    public void deletar(Long id) {
        Agendamento agendamento = this.agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNaoEncontradoException::new);

        this.itemRepository.deleteAllByAgendamento(agendamento);

        List<Foto> fotos = this.fotoRepository.findAllByAgendamento(agendamento);

        for (Foto foto : fotos)
            // this.s3StorageService.delete(foto.getNome());

        this.fotoRepository.deleteAll(fotos);
        this.fotoRepository.deleteAllByAgendamento(agendamento);

        this.agendamentoRepository.delete(agendamento);
    }

    public List<AgendamentoResponseDTO> listarEntreDatas(LocalDateTime inicio, LocalDateTime fim, Usuario usuario) {

        return this.agendamentoRepository.findByInicioBetweenAndUsuario(inicio, fim, usuario).stream()
                .map(AgendamentoResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public Agendamento atualizar(Long id, AgendamentoRequestDTO agendamento, Usuario usuario) {
        Agendamento agendamentoEncontrado = this.agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNaoEncontradoException::new);

        if (!agendamento.getInicio().equals(agendamentoEncontrado.getInicio())
                || !agendamento.getFim().equals(agendamentoEncontrado.getFim())) {
            agendamentoPolicy.validarAtualizacao(agendamento, usuario, id);
        }

        List<Foto> fotosNovas = this.fotoRepository.findAllById(agendamento.getReferencias());

        if (fotosNovas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma foto encontrada para os IDs fornecidos");
        }

        if (agendamento.getPreco() != null && agendamentoEncontrado.getTransacao() != null
                && !agendamento.getPreco().equals(agendamentoEncontrado.getPreco())) {
            agendamentoEncontrado.getTransacao().setValor(agendamento.getPreco());
            this.transacaoRepository.save(agendamentoEncontrado.getTransacao());
        }

        agendamentoEncontrado.setPreco(agendamento.getPreco());
        agendamentoEncontrado.setCliente(agendamento.getCliente());
        agendamentoEncontrado.setTelefone(agendamento.getTelefone());
        agendamentoEncontrado.setFormaPagamento(agendamento.getFormaPagamento());
        agendamentoEncontrado.setInicio(agendamento.getInicio());
        agendamentoEncontrado.setFim(agendamento.getFim());

        List<Foto> fotosAntigas = agendamentoEncontrado.getReferencias();
        for (Foto fotoAntiga : fotosAntigas) {
            if (!fotosNovas.contains(fotoAntiga)) {
                fotoAntiga.setAgendamento(null);
                this.fotoRepository.save(fotoAntiga);
            }
        }

        for (Foto fotoNova : fotosNovas) {
            fotoNova.setAgendamento(agendamentoEncontrado);
        }

        agendamentoEncontrado.setReferencias(fotosNovas);
        this.fotoRepository.saveAll(fotosNovas);
        return agendamentoRepository.save(agendamentoEncontrado);
    }

    @Transactional
    public void confirmar(Long agendamentoId, Usuario usuario) {
        Agendamento agendamento = this.agendamentoRepository.findById(agendamentoId)
                .orElseThrow(AgendamentoNaoEncontradoException::new);

        agendamentoPolicy.validarConfirmacao(agendamento, usuario);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);

        this.agendamentoRepository.save(agendamento);
    }

    @Transactional
    public void confirmarPagamento(Long agendamentoId, Usuario usuario) {
        Agendamento agendamento = this.agendamentoRepository.findById(agendamentoId)
                .orElseThrow(AgendamentoNaoEncontradoException::new);

        agendamentoPolicy.validarPagamento(agendamento);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO_PAGAMENTO);
        this.agendamentoRepository.save(agendamento);
    }

    @Transactional
    public void cancelar(Long agendamentoId, Usuario usuario) {
        Agendamento agendamento = this.agendamentoRepository.findById(agendamentoId)
                .orElseThrow(AgendamentoNaoEncontradoException::new);

        agendamentoPolicy.validarCancelamento(agendamento);
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        this.agendamentoRepository.save(agendamento);
    }

    public HorarioDisponivelDTO proximoDisponivel(Usuario usuario) {

        LocalDateTime agora = LocalDateTime.now();

        List<Agendamento> agendamentos = agendamentoRepository
                .findByUsuarioAndFimAfterAndStatusNotOrderByInicioAsc(
                        usuario, agora, StatusAgendamento.CANCELADO);

        LocalDateTime cursor = agora;

        for (Agendamento agendamento : agendamentos) {

            LocalDateTime inicioOcupado = agendamento.getInicio();
            LocalDateTime fimOcupado = agendamento.getFim();

            // se esse agendamento já terminou antes do cursor, ignora
            if (!fimOcupado.isAfter(cursor)) {
                continue;
            }

            // existe espaço livre entre o cursor e o início desse agendamento?
            Duration gap = Duration.between(cursor, inicioOcupado);
            if (gap.compareTo(DURACAO_PADRAO) >= 0) {
                // preenche a janela INTEIRA até o próximo agendamento começar
                return new HorarioDisponivelDTO(cursor, inicioOcupado);
            }

            // não coube: avança o cursor para o fim desse agendamento
            cursor = fimOcupado;
        }

        // não achou gap entre os agendamentos existentes ->
        // horário logo após o último agendamento, sem próximo compromisso pra limitar o
        // "fim"
        return new HorarioDisponivelDTO(cursor, cursor.plus(DURACAO_PADRAO));
    }
}
