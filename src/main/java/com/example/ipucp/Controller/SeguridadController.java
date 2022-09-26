package com.example.ipucp.Controller;

import com.example.ipucp.Entity.*;
import com.example.ipucp.Dto.UsuarioIncidencias;
import com.example.ipucp.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/seguridad")
public class SeguridadController {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CargoRepository cargoRepository;

    @Autowired
    InicidenciaRepository inicidenciaRepository;

    @Autowired
    TipoRepository tipoRepository;

    @Autowired
    UrgenciaRepository urgenciaRepository;

    @GetMapping("")
    public String principal() {
        return "seguridad/principal";
    }

    @GetMapping("/incidencias")
    public String lista(Model model) {
        /*Tipo*/
        List<Tipo> listaTipos  = this.obtenerTipos();
        /*urgencias*/
        List<Urgencia> listaUrg = this.obtenerUrgencias();
        /**/
        List<Inicidencia> inicidenciaList = inicidenciaRepository.findAll();
        model.addAttribute("idtipoI",0);
        model.addAttribute("idUrgI",0);
        model.addAttribute("ListaIncidencias", inicidenciaList);
        model.addAttribute("ListaTipos", listaTipos);
        model.addAttribute("ListaUrgencia", listaUrg);
        return "seguridad/incidencias";
    }

    @PostMapping("/incidenciasFiltrado")
    public String listaFiltrada(Model model,@RequestParam("tipo") int idTipo ,@RequestParam("urgencia") int idUrgencia) {
        List<Inicidencia> listIncidencias = new ArrayList<>();
        String sentencia = "SELECT * FROM inicidencia ";
        if (idTipo != 0){
            if(idUrgencia != 0){
                listIncidencias.addAll(inicidenciaRepository.filtradoTipoUrgencia(idTipo,idUrgencia));
            } else {
                listIncidencias.addAll(inicidenciaRepository.filtradoTipo(idTipo));
            }
        }else{
            if(idUrgencia != 0){
                listIncidencias.addAll(inicidenciaRepository.filtradoUrgencia(idUrgencia));
            }else{
                listIncidencias.addAll(inicidenciaRepository.findAll());
            }
        }
        /*Tipo*/
        List<Tipo> listaTipos  = this.obtenerTipos();
        /*urgencias*/
        List<Urgencia> listaUrg = this.obtenerUrgencias();
        /**/
        model.addAttribute("idtipoI",idTipo);
        model.addAttribute("idUrgI",idUrgencia);
        model.addAttribute("ListaIncidencias",listIncidencias);
        model.addAttribute("ListaTipos", listaTipos);
        model.addAttribute("ListaUrgencia", listaUrg);
        return "seguridad/incidencias";
    }

    @GetMapping("/comentar_incidencia")
    public String comentarIncidencia(@RequestParam("id") Integer id, Model model) {

        Optional<Inicidencia> optInicidencia = inicidenciaRepository.findById(id);

        if(optInicidencia.isPresent()){
            Inicidencia inicidencia = optInicidencia.get();
            if(inicidencia.getEstado()==0){
                model.addAttribute("incidencia", inicidencia);
                return "seguridad/seguridad";
            }else{
                return "redirect:/seguridad/incidencias";
            }
        }else{
            return "redirect:/seguridad/incidencias";
        }
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
            model.addAttribute("incidenciaEstado",inicidenciaRepository.bucarEstadoIncidencia());
            model.addAttribute("incidenciaUrgencia",inicidenciaRepository.buscarUrgenciaIncidencia());
        return "seguridad/dashboard";
    }

    @GetMapping("/mapa_incidencias")
    public String mapa() {
        return "seguridad/seguridad_mapa";
    }

    @GetMapping("/lista_usuarios")
    public String listaUsuarios(Model model) {
        List<Usuario> ListaUsuarios = usuarioRepository.listarUsuarios();
        model.addAttribute("listaUsuarios", ListaUsuarios);
        return "seguridad/lista_usuarios";
    }

    @PostMapping("/BuscarCategoria")
    public String buscarCategoria(@RequestParam("idcat") Integer id, Model model){
        Optional<Cargo> optCargo = cargoRepository.findById(id);

        if(optCargo.isPresent()){
            List<Usuario> listaUsuarios = usuarioRepository.buscarUsuarioPorCat(id);
            model.addAttribute("listaUsuarios", listaUsuarios);
            return "seguridad/lista_usuarios";
        }else{
            return "redirect:/seguridad/lista_usuarios";
        }
    }

    @GetMapping("/reporte")
    public String reporteUsuario(Model model,
                                      @RequestParam("id") String id) {

        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();
            List<UsuarioIncidencias> listaIncidencias = usuarioRepository.obtenerUsuarioIncidencias(id);
            for(UsuarioIncidencias incidencias: listaIncidencias){
                System.out.println(incidencias.getIdinicidencia()+" "+incidencias.getTipo_incidencia()+" "+incidencias.getEstado());
            }
            model.addAttribute("usuario", usuario);
            model.addAttribute("listaIncidencias", listaIncidencias);
            return "seguridad/seguridad_reportar";
        } else {
            return "redirect:/seguridad/lista_usuarios";
        }
    }

    @PostMapping("/StrikeUsuario")
    public String StrikeUsuario(@RequestParam("id") String id, RedirectAttributes redirectAttributes){
        Optional<Usuario> optUsuario = usuarioRepository.findById(id);

        if (optUsuario.isPresent()){
            Usuario usuario = optUsuario.get();
            int strike = usuario.getStrikes();
            strike+=1;
            usuarioRepository.strikeUsuario(strike,id);
            redirectAttributes.addFlashAttribute("msg", "El usuario "+usuario.getNombre()+" "+usuario.getApellido()+" ha sido reportado.");
            return "redirect:/seguridad/lista_usuarios";
        }else{
            return "redirect:/seguridad/reporte?id="+id;
        }

    }

    @GetMapping("/detalle_incidencia")
    public String detalleIncidencia(Model model,
                                 @RequestParam("id") Integer id,@RequestParam("codigo") String codigo ){
        Optional<Inicidencia> optInicidencia = inicidenciaRepository.findById(id);
        Optional<Usuario> optUsuario = usuarioRepository.findById(codigo);

        if (optInicidencia.isPresent() && optUsuario.isPresent() ){
            Inicidencia inicidencia = optInicidencia.get();

            model.addAttribute("incidencia", inicidencia);

            return "seguridad/detalleid_seguridad";
        }else{
            return "redirect:/seguridad/reporte?id="+codigo;
        }

    }

    public List<Urgencia> obtenerUrgencias(){
        List<Urgencia> listaUrg = new ArrayList<>();
        Urgencia urgTodos = new Urgencia();
        urgTodos.setId(0);
        urgTodos.setTipoUrgencia("Todas las urgencias");
        listaUrg.add(urgTodos);
        listaUrg.addAll(urgenciaRepository.findAll());
        return listaUrg;
    }
    public List<Tipo> obtenerTipos(){
        List <Tipo>  listaTipos = new ArrayList<>();
        Tipo todos = new Tipo();
        todos.setId(0);
        todos.setTipoIncidencia("Todos");
        listaTipos.add(todos);
        listaTipos.addAll(tipoRepository.findAll());
        return listaTipos;
    }

}
