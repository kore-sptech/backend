package kore.backend.service;

import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

import kore.backend.model.Usuario;
import kore.backend.repository.FotoRepository;
import kore.backend.repository.ItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kore.backend.dto.AgendamentoRequestDTO;
import kore.backend.dto.AgendamentoResponseDTO;
import kore.backend.exception.AgendamentoNaoEncondradoException;
import kore.backend.model.Agendamento;
import kore.backend.model.Foto;
// import kore.backend.model.Item;
import kore.backend.repository.AgendamentoRepository;

@Service
public class AgendamentoService {

    private final FotoRepository fotoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ItemRepository itemRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, FotoRepository fotoRepository,
            ItemRepository itemRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.fotoRepository = fotoRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarDaSemana() {
        LocalDate hoje = LocalDate.now();

        LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime inicioInclusivo = inicioSemana.atStartOfDay();
        LocalDateTime fimExclusivo = inicioSemana.plusWeeks(1).atStartOfDay();

        List<Agendamento> agendamentos = agendamentoRepository
                .findByInicioGreaterThanEqualAndInicioLessThanOrderByInicioDesc(inicioInclusivo, fimExclusivo);

        return agendamentos.stream()
                .map(AgendamentoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Agendamento criar(AgendamentoRequestDTO request, Usuario usuario) {
        List<Foto> fotos = this.fotoRepository.findAllById(request.getReferencias());

        if (fotos.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma foto encontrada para as referências fornecidas");
        }

        // List<Item> items = this.itemRepository.findAllById(request.getItems());

        // if (items.isEmpty()) {
        // throw new IllegalArgumentException("Nenhum item encontrado para os IDs
        // fornecidos");
        // }

        if (request.getInicio() == null || request.getFim() == null) {
            throw new IllegalArgumentException("Início e fim do agendamento são obrigatórios");
        }

        if (!request.getFim().isAfter(request.getInicio())) {
            throw new IllegalArgumentException("Fim do agendamento deve ser após o início");
        }

        boolean existeConflitoNaAgenda = this.agendamentoRepository
                .existsByInicioLessThanAndFimGreaterThan(request.getFim(), request.getInicio());

        if (existeConflitoNaAgenda) {
            throw new IllegalArgumentException("Já existe um agendamento nesse horário");
        }

        Agendamento agendamento = Agendamento
                .builder()
                .referencias(fotos)
                // .items(items)
                .cliente(request.getCliente())
                .telefone(request.getTelefone())
                .formaPagamento(request.getFormaPagamento())
                .preco(request.getPreco())
                .inicio(request.getInicio())
                .fim(request.getFim())
                .usuario(usuario)
                .build();

        for (Foto foto : fotos)
            foto.setAgendamento(agendamento);

        // for (Item item : items)
        // item.setAgendamento(agendamento);

        this.fotoRepository.saveAll(fotos);
        // this.itemRepository.saveAll(items);

        return this.agendamentoRepository.save(agendamento);
    }

    @Transactional
    public void deletar(
            Long id) {
        Agendamento agendamento = this.agendamentoRepository.findById(id).orElseThrow(
                AgendamentoNaoEncondradoException::new);

        this.itemRepository.deleteAllByAgendamento(agendamento);

        List<Foto> fotos = this.fotoRepository.findAllByAgendamento(agendamento);

        for (Foto foto : fotos)
            Paths.get(foto.getImageUrl()).toFile().delete();

        this.fotoRepository.deleteAll(fotos);
        this.fotoRepository.deleteAllByAgendamento(agendamento);

        this.agendamentoRepository.delete(agendamento);
    }

    public List<AgendamentoResponseDTO> listarEntreDatas(LocalDateTime inicio, LocalDateTime fim, Usuario usuario) {

        return this.agendamentoRepository.findByInicioBetweenAndUsuario(inicio, fim, usuario)
                .stream()
                .map(AgendamentoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Agendamento atualizar(Long id, AgendamentoRequestDTO agendamento, Usuario usuario) {

        System.out.println("Referencias recebidas: " + agendamento.getReferencias());

        Agendamento agendamentoEncontrado = this.agendamentoRepository.findById(id)
                .orElseThrow(AgendamentoNaoEncondradoException::new);

        // Validar conflitos de data se as datas foram alteradas
        if (!agendamento.getInicio().equals(agendamentoEncontrado.getInicio()) ||
                !agendamento.getFim().equals(agendamentoEncontrado.getFim())) {

            if (!agendamento.getFim().isAfter(agendamento.getInicio())) {
                throw new IllegalArgumentException("Fim do agendamento deve ser após o início");
            }

            // Verificar conflito apenas com outros agendamentos (ID diferente)
            List<Agendamento> agendamentosEmConflito = this.agendamentoRepository
                    .findByInicioBetweenAndUsuario(agendamento.getInicio(), agendamento.getFim(), usuario);

            boolean existeConflitoComOutro = agendamentosEmConflito.stream()
                    .anyMatch(a -> !a.getId().equals(id));

            if (existeConflitoComOutro) {
                throw new IllegalArgumentException("Já existe um agendamento nesse horário");
            }
        }

        // Buscar as fotos novas
        List<Foto> fotosNovas = this.fotoRepository.findAllById(agendamento.getReferencias());

        if (fotosNovas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma foto encontrada para os IDs fornecidos");
        }

        // Atualizar dados do agendamento
        agendamentoEncontrado.put(agendamento);

        // Gerenciar relacionamentos com fotos
        List<Foto> fotosAntigas = agendamentoEncontrado.getReferencias();

        // Desassociar fotos que não estão mais na nova lista
        for (Foto fotoAntiga : fotosAntigas) {
            if (!fotosNovas.contains(fotoAntiga)) {
                fotoAntiga.setAgendamento(null);
                this.fotoRepository.save(fotoAntiga);
            }
        }

        // Associar as novas fotos
        for (Foto fotoNova : fotosNovas) {
            fotoNova.setAgendamento(agendamentoEncontrado);
        }

        // Atualizar lista de referências
        agendamentoEncontrado.setReferencias(fotosNovas);

        // Salvar fotos e agendamento
        this.fotoRepository.saveAll(fotosNovas);
        return agendamentoRepository.save(agendamentoEncontrado);

    }
}
