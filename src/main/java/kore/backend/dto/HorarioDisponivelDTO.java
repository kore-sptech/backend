package kore.backend.dto;

import java.time.LocalDateTime;

public record HorarioDisponivelDTO(
        LocalDateTime inicio,
        LocalDateTime fim) {

}
