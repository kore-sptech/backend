package kore.backend.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioDTO(
        @Size (min = 10, max = 100)
        @NotNull
        String email,
        @Size(min = 3, max = 50)
        @NotNull
        String nome,
        @NotNull
        @Size(min = 5, max = 30)
        String senha
) {

        @Override
        public String toString() {
                return "UsuarioDTO{" +
                        "email='" + email + '\'' +
                        ", nome='" + nome + '\'' +
                        ", senha='" + senha + '\'' +
                        '}';
        }
}
