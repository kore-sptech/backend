package kore.backend.repository;

import kore.backend.model.Usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // como estou utilizando o h2 para testes, uma query manual estava atrapalhando. optei pelo uso do jpa.
//    @Query(value = "SELECT * from test.Usuario where email =:emailUsuario", nativeQuery = true)
//    Usuario buscarEmail(@Param("emailUsuario") String emailUsuario);

    Optional<Usuario> findByEmail(String email);

}
