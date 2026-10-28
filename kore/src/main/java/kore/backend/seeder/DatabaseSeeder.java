package kore.backend.seeder;

import kore.backend.model.*;
import kore.backend.model.enums.*;
import kore.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemRepository itemRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final TransacaoRepository transacaoRepository;
    private final FotoRepository fotoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Verificando se o banco de dados precisa de seed...");

        // 1. Garantir Usuário Padrão
        Usuario usuario = usuarioRepository.findByEmail("kore@gmail.com").orElseGet(() -> {
            log.info("Criando usuário padrão: kore@gmail.com");
            Usuario novo = Usuario.builder()
                    .nome("Kore Tattoo Studio")
                    .email("kore@gmail.com")
                    .senha(passwordEncoder.encode("kore2006@2026"))
                    .build();
            return usuarioRepository.save(novo);
        });

        // Se já existem produtos, assumimos que o seed já foi executado para evitar duplicação em restarts
        if (produtoRepository.count() > 0) {
            log.info("Banco de dados já populado. Seed ignorado.");
            return;
        }

        log.info("Iniciando povoamento do banco de dados com dados coesos fornecidos para estúdio de tatuagem...");

        Faker faker = new Faker(new Locale("pt", "BR"));
        Random random = new Random();

        // 2. Criar Categorias
        Categoria catTintas = categoriaRepository.save(new Categoria("Tintas e Pigmentos", "Frascos de tinta profissional para tatuagem", usuario.getId()));
        Categoria catMaquinas = categoriaRepository.save(new Categoria("Máquinas e Fontes", "Máquinas rotativas tipo Pen e fontes de alimentação", usuario.getId()));
        Categoria catDescartaveis = categoriaRepository.save(new Categoria("Materiais Descartáveis e Luvas", "Luvas nitrílicas, batoques, papel toalha, bandagens", usuario.getId()));
        Categoria catCuidados = categoriaRepository.save(new Categoria("Cuidados e Higiene", "Vaselinas, antissépticos e cremes cicatrizantes", usuario.getId()));
        Categoria catMoveis = categoriaRepository.save(new Categoria("Ambiente do Estúdio / Bancada", "Visão geral da maca, bancada limpa e organizada do estúdio", usuario.getId()));

        class ProdutoSeedData {
            String nome;
            String descricao;
            String tipo;
            Categoria categoria;
            String urlImagem;

            ProdutoSeedData(String nome, String descricao, String tipo, Categoria categoria, String urlImagem) {
                this.nome = nome;
                this.descricao = descricao;
                this.tipo = tipo;
                this.categoria = categoria;
                this.urlImagem = urlImagem;
            }
        }

        List<ProdutoSeedData> dadosProdutos = List.of(
                // Tintas e Pigmentos
                new ProdutoSeedData("Tinta Azul Céu, Inkerz Study 30ml", "Tinta de estudo em frasco de 30ml, indicada para prática em pele artificial.", "CONSUMIVEL", catTintas, "https://http2.mlstatic.com/D_Q_NP_2X_711784-MLA91644553454_092025-AB.webp"),
                new ProdutoSeedData("Tinta Azul Médio, Inkerz Study 30ml", "Mesma linha de estudo, frasco de 30ml para pele artificial.", "CONSUMIVEL", catTintas, "https://http2.mlstatic.com/D_Q_NP_2X_905507-MLA91969627497_092025-AB.webp"),
                new ProdutoSeedData("Tinta Vermelho Bombeiro, Inkerz 30ml", "Tinta para tatuagem vegana em frasco de 30ml.", "CONSUMIVEL", catTintas, "https://http2.mlstatic.com/D_Q_NP_2X_891421-MLA99357921464_112025-AB.webp"),
                new ProdutoSeedData("Tinta Vermelho Ferrari, Inkerz 30ml", "Tinta profissional para tatuagem em frasco de 30ml.", "CONSUMIVEL", catTintas, "https://http2.mlstatic.com/D_Q_NP_2X_702171-MLA99602928072_122025-AB.webp"),
                new ProdutoSeedData("Tinta Vermelho Cereja, Inkerz 30ml", "Frasco de 30ml da linha de 14 cores, com menção à Anvisa no anúncio.", "CONSUMIVEL", catTintas, "https://http2.mlstatic.com/D_Q_NP_2X_821841-MLA99614447370_122025-AB.webp"),

                // Máquinas e Fontes
                new ProdutoSeedData("Pen Rotativa DragoArt Warrior (vermelha, RCA)", "Máquina de tatuagem rotativa tipo pen, com conexão RCA.", "EQUIPAMENTO", catMaquinas, "https://http2.mlstatic.com/D_Q_NP_2X_743119-MLA99523995558_122025-AB.webp"),
                new ProdutoSeedData("Pen Rotativa DragoArt Rocket Max 8v (vermelha)", "Caneta de tatuagem rotativa de 8v.", "EQUIPAMENTO", catMaquinas, "https://http2.mlstatic.com/D_Q_NP_2X_900402-MLA99484729760_112025-AB.webp"),
                new ProdutoSeedData("Fonte sem fio DragoArt MC-W1 (preta)", "Fonte sem fio para máquina de tatuagem.", "EQUIPAMENTO", catMaquinas, "https://http2.mlstatic.com/D_Q_NP_2X_604551-MLA92147917689_092025-AB.webp"),
                new ProdutoSeedData("Fonte sem fio WX-1 Plus (preta)", "Fonte de alimentação sem fio para tatuagem.", "EQUIPAMENTO", catMaquinas, "https://http2.mlstatic.com/D_Q_NP_2X_619929-MLA99901864161_112025-AB.webp"),

                // Materiais Descartáveis e Luvas
                new ProdutoSeedData("Luvas nitrílicas pretas Inoven, tam. M (100 un)", "Luvas descartáveis antiderrapantes, caixa com 100 unidades.", "ESTOQUE", catDescartaveis, "https://http2.mlstatic.com/D_Q_NP_2X_986311-MLA85458470958_062025-AB.webp"),
                new ProdutoSeedData("Luvas nitrílicas rosa Talge, tam. M (100 un)", "Luvas hipoalergênicas e antiderrapantes, caixa com 100 unidades.", "ESTOQUE", catDescartaveis, "https://http2.mlstatic.com/D_Q_NP_2X_748319-MLA110409079284_052026-AB.webp"),
                new ProdutoSeedData("Protetor para clipcord 6x60cm (EQUIPAART)", "Protetor plástico preto para o cabo da máquina.", "CONSUMIVEL", catDescartaveis, "https://http2.mlstatic.com/D_Q_NP_2X_753108-MLA100087522517_122025-AB.webp"),
                new ProdutoSeedData("Filme plástico PVC esticável 60cm x 600m", "Rolo grande de filme plástico, usado para forrar e proteger superfícies.", "CONSUMIVEL", catDescartaveis, "https://http2.mlstatic.com/D_Q_NP_2X_951031-MLB72805308466_112023-AB.webp"),

                // Cuidados e Higiene
                new ProdutoSeedData("Vaselina Tattoo Jelly Amazon 730g", "Vaselina especial para tatuagem, pote de 730g.", "CONSUMIVEL", catCuidados, "https://http2.mlstatic.com/D_Q_NP_2X_906598-MLA93763905715_092025-AB.webp"),
                new ProdutoSeedData("Vaselina Slip Electric Ink 150g", "Vaselina da Electric Ink para tatuagem, 150g.", "CONSUMIVEL", catCuidados, "https://http2.mlstatic.com/D_Q_NP_2X_802405-MLA113071751724_072026-AB.webp"),
                new ProdutoSeedData("Pomada Hidratante para Tatuagem (restaurador dérmico)", "Pomada hidratante para a pele tatuada.", "CONSUMIVEL", catCuidados, "https://http2.mlstatic.com/D_Q_NP_2X_645646-MLA116205853277_082026-AB.webp"),
                new ProdutoSeedData("Creme Hidratante Vegano para Tatuagem (melancia)", "Creme hidratante vegano para tatuagem, aroma de melancia.", "CONSUMIVEL", catCuidados, "https://http2.mlstatic.com/D_Q_NP_2X_804394-MLB77675271421_072024-AB.webp"),

                // Ambiente e Mobiliário
                new ProdutoSeedData("Maca 3 Posições Blue Hell Compact (preta)", "Maca de 3 posições para tatuagem, estofada em preto.", "ESTOQUE", catMoveis, "https://http2.mlstatic.com/D_Q_NP_2X_772279-MLA93414011584_092025-AB.webp"),
                new ProdutoSeedData("Bancada de Trabalho Storm Double", "Bancada de trabalho para tatuagem, da linha Storm Double.", "EQUIPAMENTO", catMoveis, "https://http2.mlstatic.com/D_Q_NP_2X_668445-MLA94693564602_102025-AB.webp"),
                new ProdutoSeedData("Apoio de Braço Blue Hell Storm Double (preto, dobrável)", "Apoio de braço dobrável para tatuagem.", "EQUIPAMENTO", catMoveis, "https://http2.mlstatic.com/D_Q_NP_2X_802910-MLA95669316524_102025-AB.webp")
        );

        List<Produto> produtosCriados = new ArrayList<>();

        for (ProdutoSeedData data : dadosProdutos) {
            Produto produto = new Produto();
            produto.setNome(data.nome);
            produto.setDescricao(data.descricao);
            produto.setPossuiValidade(true);
            produto.setQtdMinAlerta(5);
            produto.setFkUsuario(usuario.getId());
            produto.setTipo(data.tipo);
            produto.setCategoria(data.categoria);
            produto.setImagemKey(data.urlImagem);

            Produto salvo = produtoRepository.save(produto);
            produtosCriados.add(salvo);

            int qtdItens = 20 + random.nextInt(11); // 20 a 30
            int metade = qtdItens / 2;

            for (int j = 0; j < qtdItens; j++) {
                Item item = new Item();
                item.setProduto(salvo);

                double valorUnitario = 15.0 + (random.nextInt(35) * 5.0);
                if (random.nextBoolean()) {
                    valorUnitario += 0.50;
                }
                item.setValorUnitario((float) valorUnitario);
                item.setSeAtivo(true);

                LocalDateTime dtEntrada = LocalDateTime.now().minusDays(random.nextInt(60));
                item.setDataEntrada(dtEntrada);

                if (j % 5 == 0) {
                    item.setDataValidade(LocalDateTime.now().plusDays(random.nextInt(10) + 1));
                } else if (j % 12 == 0) {
                    item.setDataValidade(LocalDateTime.now().minusDays(random.nextInt(5) + 1));
                } else {
                    item.setDataValidade(LocalDateTime.now().plusDays(random.nextInt(300) + 30));
                }

                Item itemSalvo = itemRepository.save(item);

                if (j >= metade) {
                    transacaoServiceCriarCompra(salvo, Double.valueOf(itemSalvo.getValorUnitario()), usuario, dtEntrada);
                }
            }
        }

        // 3. Agendamentos sem conflito de horário (espaçamento variável de 15 min, 30 min ou 1h, término entre 20h e 21h)
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        int diasNoMes = hoje.lengthOfMonth();

        class AgendamentoRef {
            String servico;
            String imagem;

            AgendamentoRef(String servico, String imagem) {
                this.servico = servico;
                this.imagem = imagem;
            }
        }

        List<AgendamentoRef> referenciasAgendamento = List.of(
                new AgendamentoRef("Tatuagem Realismo Preto e Cinza (Sleeve)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSdXjwNpIW5IsoOFUxJY1Ek-KKQAKF0jzPTUubkV5noPOkYDaJOPGSC03s&s=10"),
                new AgendamentoRef("Tatuagem Realismo / P&B (Retrato)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTQ19u8djTf2tmRvIkN4PYS94drq34lFByfk5JTRxTBft1ss5QfEtLROgVG&s=10"),
                new AgendamentoRef("Tatuagem Fine Line / Minimalista", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTT9pkFLNNCN37KjJkWMdxj70RhvpzfmwI_1rqhp2uU-AKdg-hPJVQlryY&s=10"),
                new AgendamentoRef("Tatuagem Oriental / Blackwork (Fechamento)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRwNuFiN8eeROtHAxqxJnqnsMhqtGND6ao813t2wZj0i6ZETsCSdxJsXYY&s=10"),
                new AgendamentoRef("Aplicação de Piercing Labret Titânio", "https://http2.mlstatic.com/D_Q_NP_2X_677695-MLB89826269327_082025-AB.webp"),
                new AgendamentoRef("Aplicação de Piercing Argola Titânio Click", "https://http2.mlstatic.com/D_Q_NP_2X_610924-MLB111789260034_062026-AB-piercing-argola-titnio-click-segmento-helix-conch-nariz.webp"),
                new AgendamentoRef("Piercing Transversal Industrial Zircônia", "https://http2.mlstatic.com/D_Q_NP_2X_936452-MLB108011931177_032026-AB-piercing-transversal-industrial-titnio-zircnia-cravejada.webp")
        );

        int[] espacamentosMinutos = {15, 30, 60}; // 15 min, 30 min ou 1 hora
        int[] duracoesSessaoMinutos = {90, 120, 150, 180}; // 1h30, 2h, 2h30, 3h

        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate dataAgendamento = inicioMes.withDayOfMonth(dia);

            // Cada dia começa às 10:00
            LocalTime horarioAtual = LocalTime.of(10, 0);
            LocalTime limiteNoite = LocalTime.of(21, 0); // Limite máximo de término até 21:00

            // 2 a 4 agendamentos por dia
            int qtdPorDia = 2 + random.nextInt(3);

            for (int k = 0; k < qtdPorDia; k++) {
                int duracao = duracoesSessaoMinutos[random.nextInt(duracoesSessaoMinutos.length)];
                LocalDateTime inicioSessao = LocalDateTime.of(dataAgendamento, horarioAtual);
                LocalDateTime fimSessao = inicioSessao.plusMinutes(duracao);

                // Se o término ultrapassar 21:00, encerramos os agendamentos deste dia
                if (fimSessao.toLocalTime().isAfter(limiteNoite)) {
                    break;
                }

                String cliente = faker.name().fullName();
                String telefone = faker.phoneNumber().cellPhone();

                AgendamentoRef ref = referenciasAgendamento.get(random.nextInt(referenciasAgendamento.size()));
                String servico = ref.servico;

                double preco = 250.0 + (random.nextInt(20) * 50.0);
                if (random.nextBoolean()) {
                    preco += 0.50;
                }

                Agendamento agendamento = new Agendamento();
                agendamento.setCliente(cliente);
                agendamento.setTelefone(telefone);
                agendamento.setPreco(preco);
                agendamento.setInicio(inicioSessao);
                agendamento.setFim(fimSessao);
                agendamento.setFormaPagamento(random.nextBoolean() ? FormaPagamento.PIX : FormaPagamento.CREDITO);
                agendamento.setUsuario(usuario);

                if (inicioSessao.isBefore(LocalDateTime.now())) {
                    agendamento.setStatus(StatusAgendamento.CONFIRMADO_PAGAMENTO);
                } else if (inicioSessao.toLocalDate().isEqual(hoje)) {
                    agendamento.setStatus(StatusAgendamento.CONFIRMADO);
                } else {
                    agendamento.setStatus(StatusAgendamento.PENDENTE);
                }

                Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

                if (inicioSessao.isBefore(LocalDateTime.now())) {
                    Transacao tx = new Transacao();
                    tx.setValor(preco);
                    tx.setNome("Sessão: " + servico + " — " + cliente);
                    tx.setTipo(TipoTransacao.ENTRADA);
                    tx.setCategoria(CategoriaTransacao.SESSAO);
                    tx.setUsuario(usuario);
                    tx.setSessao(agendamentoSalvo);
                    transacaoRepository.save(tx);
                }

                Foto foto = new Foto();
                foto.setNome("Referência: " + servico + " (" + cliente + ")");
                foto.setImageUrl(ref.imagem);
                foto.setAgendamento(agendamentoSalvo);
                fotoRepository.save(foto);

                // Calcular próximo horário: fim da sessão + espaçamento variável (15m, 30m ou 60m)
                int espacamento = espacamentosMinutos[random.nextInt(espacamentosMinutos.length)];
                horarioAtual = fimSessao.toLocalTime().plusMinutes(espacamento);

                // Se o próximo horário ultrapassar 20:30, não cabe mais nenhum atendimento
                if (horarioAtual.isAfter(LocalTime.of(20, 30))) {
                    break;
                }
            }
        }

        // 4. Notificações Iniciais
        notificacaoRepository.save(Notificacao.builder()
                .titulo("Estúdio Organizado")
                .mensagem("Agendamentos recalculados sem sobreposições e com horários fluidos.")
                .tipo(TipoNotificacao.ALERTA)
                .lida(false)
                .build());

        log.info("Database Seeder executado com sucesso com horários sequenciais e sem conflitos!");
    }

    private void transacaoServiceCriarCompra(Produto produto, Double valor, Usuario usuario, LocalDateTime data) {
        Transacao transacao = new Transacao();
        transacao.setValor(valor);
        transacao.setNome("Reposição de estoque — " + produto.getNome());
        transacao.setTipo(TipoTransacao.SAIDA);
        transacao.setCategoria(CategoriaTransacao.MATERIAS);
        transacao.setUsuario(usuario);
        transacao.setProduto(produto);
        transacao.setDataCriacao(data);
        transacaoRepository.save(transacao);
    }
}
