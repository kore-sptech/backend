package kore.backend.repository;

import kore.backend.model.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query(value = "SELECT * from Usuario where email =:emailUsuario", nativeQuery = true)
    Usuario buscarEmail(@Param("emailUsuario") String emailUsuario);

    Optional<Usuario> findByEmail(String email);

}
