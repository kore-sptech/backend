package kore.backend;

import kore.backend.model.Agendamento;
import kore.backend.model.Foto;
import kore.backend.model.Usuario;
import kore.backend.model.enums.FormaPagamento;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.FotoRepository;
import kore.backend.repository.UsuarioRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

	private final UsuarioRepository usuarioRepository;
	private final AgendamentoRepository agendamentoRepository;
	private final FotoRepository fotoRepository;
	private final PasswordEncoder passwordEncoder;

	public DatabaseSeeder(AgendamentoRepository agendamentoRepository, FotoRepository fotoRepository,
			PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository) {
		this.agendamentoRepository = agendamentoRepository;
		this.fotoRepository = fotoRepository;
		this.passwordEncoder = passwordEncoder;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		boolean alreadySeeded = fotoRepository.existsByImageUrl("example.jpg");
		if (alreadySeeded) {
			return;
		}

		Usuario usuario = Usuario.builder()
				.nome("kore")
				.email("kore@gmail.com")
				.senha(this.passwordEncoder.encode("kore123"))
				.build();

		this.usuarioRepository.save(usuario);

		Faker faker = new Faker();
		LocalDateTime now = LocalDateTime.now();

		long[] minutosParaNotificar = { 9, 10, 11 };
		for (long minutos : minutosParaNotificar) {
			salvar(faker, usuario, now.plusMinutes(minutos), now.plusMinutes(minutos + 60));
		}

		salvar(faker, usuario, now.plusMinutes(10), now.plusMinutes(70));

		for (int i = 1; i <= 19; i++) {
			LocalDateTime inicio = now.plusDays(i);
			salvar(faker, usuario, inicio, inicio.plusHours(1));
		}
	}

	private void salvar(Faker faker, Usuario usuario, LocalDateTime inicio, LocalDateTime fim) {
		Agendamento agendamento = Agendamento.builder()
				.preco(faker.number().randomDouble(2, 50, 300))
				.cliente(faker.name().fullName())
				.telefone(faker.phoneNumber().cellPhone())
				.formaPagamento(FormaPagamento.DINHEIRO)
				.inicio(inicio)
				.fim(fim)
				.usuario(usuario)
				.build();

		Agendamento saved = agendamentoRepository.save(agendamento);

		Foto foto = new Foto();
		foto.setImageUrl("example.jpg");
		foto.setNome("example-" + saved.getId());
		foto.setAgendamento(saved);
		fotoRepository.save(foto);
	}
}