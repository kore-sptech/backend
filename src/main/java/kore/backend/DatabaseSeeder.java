package kore.backend;

import kore.backend.model.Agendamento;
import kore.backend.model.Foto;
import kore.backend.model.enums.FormaPagamento;
import kore.backend.repository.AgendamentoRepository;
import kore.backend.repository.FotoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

	private final AgendamentoRepository agendamentoRepository;
	private final FotoRepository fotoRepository;

	public DatabaseSeeder(AgendamentoRepository agendamentoRepository, FotoRepository fotoRepository) {
		this.agendamentoRepository = agendamentoRepository;
		this.fotoRepository = fotoRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		boolean alreadySeeded = fotoRepository.existsByImageUrl("example.jpg");
		if (alreadySeeded) {
			return;
		}

		Faker faker = new Faker();
		LocalDateTime now = LocalDateTime.now();

		long[] minutosParaNotificar = { 9, 10, 11 };
		for (long minutos : minutosParaNotificar) {
			salvar(faker, now.plusMinutes(minutos), now.plusMinutes(minutos + 60));
		}

		salvar(faker, now.plusMinutes(10), now.plusMinutes(70));

		for (int i = 1; i <= 19; i++) {
			LocalDateTime inicio = now.plusDays(i);
			salvar(faker, inicio, inicio.plusHours(1));
		}
	}

	private void salvar(Faker faker, LocalDateTime inicio, LocalDateTime fim) {
		Agendamento agendamento = Agendamento.builder()
				.preco(faker.number().randomDouble(2, 50, 300))
				.cliente(faker.name().fullName())
				.telefone(faker.phoneNumber().cellPhone())
				.formaPagamento(FormaPagamento.DINHEIRO)
				.inicio(inicio)
				.fim(fim)
				.build();

		Agendamento saved = agendamentoRepository.save(agendamento);

		Foto foto = new Foto();
		foto.setImageUrl("example.jpg");
		foto.setNome("example-" + saved.getId());
		foto.setAgendamento(saved);
		fotoRepository.save(foto);
	}
}