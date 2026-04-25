package kore.backend.mapper;

import kore.backend.dto.response.UsuarioResponseDTO;
import kore.backend.model.Usuario;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class UsuarioMapper {

    public static UsuarioResponseDTO toResponse(Usuario u){
        return new UsuarioResponseDTO(u.getId(), u.getNome(), u.getEmail());
    }

    public static List<UsuarioResponseDTO> toResponseList(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(UsuarioMapper::toResponse)
                .toList();
    }
}
