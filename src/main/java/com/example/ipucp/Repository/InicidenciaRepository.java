package com.example.ipucp.Repository;

import com.example.ipucp.Dto.IncidenciaEstado;
import com.example.ipucp.Dto.IncidenciaUrgencia;
import com.example.ipucp.Entity.Inicidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InicidenciaRepository extends JpaRepository<Inicidencia, Integer> {
    @Query(value="select * from inicidencia where codigo= ?1 ;",nativeQuery = true)
    List<Inicidencia> userIncidencias(String codigo);


    @Query(value="SELECT estado, count(idinicidencia) as cantidad FROM inicidencia group by estado order by estado desc", nativeQuery = true)
    List<IncidenciaEstado> bucarEstadoIncidencia();

    @Query(value = "SELECT u.tipo_urgencia , count(i.idinicidencia) as cantidad FROM inicidencia i inner join urgencia u where i.idurgencia = u.idurgencia group by i.idurgencia order by i.idurgencia asc",nativeQuery = true)
    List<IncidenciaUrgencia> buscarUrgenciaIncidencia();

    @Transactional
    @Modifying
    @Query(value = "UPDATE inicidencia SET `comentario` = ?1 , `estado` = '1' WHERE (`idinicidencia` = ?2);",nativeQuery = true)
    void comentarIncidencia(String comentario, int id);

    @Query(value="SELECT * FROM inicidencia where idtipo = ?1 order by idinicidencia desc;",nativeQuery = true)
    List<Inicidencia> filtradoTipo(int idTipo);

    @Query(value = "SELECT * FROM inicidencia where idtipo = ?1 and idurgencia = ?2 order by idinicidencia desc;",nativeQuery = true)
    List<Inicidencia> filtradoTipoUrgencia(int idTipo, int idUrgencia);

    @Query(value = "SELECT * FROM inicidencia where idurgencia = ?1 order by idinicidencia desc;",nativeQuery = true)
    List<Inicidencia> filtradoUrgencia(int idUrgencia);
}