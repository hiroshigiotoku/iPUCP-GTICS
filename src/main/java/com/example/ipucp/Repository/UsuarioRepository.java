package com.example.ipucp.Repository;

import com.example.ipucp.Entity.Usuario;
import com.example.ipucp.Dto.UsuarioIncidencias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    @Query(value = "SELECT * FROM usuario where idcargo=?1",
            nativeQuery = true)
    List<Usuario> buscarUsuarioPorCat(Integer id);

    @Query(value = "select inc.idinicidencia, tip.tipo_incidencia, inc.estado\n" +
            "from inicidencia inc, tipo tip\n" +
            "where inc.idtipo=tip.idtipo\n" +
            "and inc.codigo=?1",
            nativeQuery = true)
    List<UsuarioIncidencias> obtenerUsuarioIncidencias(String id);
}