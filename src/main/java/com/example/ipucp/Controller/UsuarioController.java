package com.example.ipucp.Controller;

import com.example.ipucp.Dao.PerfilDao;
import com.example.ipucp.Entity.*;
import com.example.ipucp.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.Instant;
import java.util.*;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    InicidenciaRepository inicidenciaRepository;
    @Autowired
    TipoRepository tipoRepository;
    @Autowired
    UrgenciaRepository urgenciaRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    UbicacionRepository ubicacionRepository;

    @Autowired
    ComentarioRepository comentarioRepository;
    @Autowired
    PerfilDao perfilDao;

    @GetMapping("/mapa")
    public String mapa() {
        return "usuario/menu_mapa";
    }

    @GetMapping("/listar")
    public String listar(Model model) {

        HashMap<Inicidencia, String> datos = new HashMap<Inicidencia, String>();
        HashMap<Inicidencia,String> user = new HashMap<Inicidencia,String>();
        List<Inicidencia> lista  =inicidenciaRepository.orderReciente();
        model.addAttribute("incidenciaList", lista);

        for(Inicidencia incidencia: lista){
            datos.put(incidencia,perfilDao.obtenerImagen("Incidencia_"+ String.valueOf(incidencia.getId())).getFileBase64());
            user.put(incidencia,perfilDao.obtenerImagen(incidencia.getCodigo().getId()).getFileBase64());
        }
        model.addAttribute("hashmap",datos);
        model.addAttribute("iperfil",user);
        return "usuario/menu";
    }

    @PostMapping("/listarFiltrado")
    public String listarFiltrado(Model model, @RequestParam("orden") int form) {
        List<Inicidencia> listIncidencias = new ArrayList<>();

        if (form == 2){
            List<Inicidencia> lista  =inicidenciaRepository.orderMaspopular();
            model.addAttribute("incidenciaList", lista);
        }
        else{
            List<Inicidencia> lista  =inicidenciaRepository.orderReciente();
            model.addAttribute("incidenciaList", lista);
        }

        return "usuario/menu";
    }

    @GetMapping("/destacar")
    public String destacarIncidencia(@RequestParam("id") Integer id) {
        inicidenciaRepository.destacarIncidencia(id);
        return "redirect:/usuario/listar";
    }


    @PostMapping("/save")
    public String guardarNuevaIncidencia(@ModelAttribute("incidencia") @Valid Inicidencia incidencia, BindingResult bindingResult, Model model, RedirectAttributes attr,
                                         HttpSession session,@RequestParam(name = "fot") MultipartFile img) {
        if(bindingResult.hasErrors()){

            List<Tipo> listaTipo  =tipoRepository.findAll();
            List<Urgencia> listaUrgencia  =urgenciaRepository.findAll();
            List<Ubicacion> listaUbicacion = ubicacionRepository.findAll();
            model.addAttribute("listaTipo", listaTipo);
            model.addAttribute("listaUrgencia", listaUrgencia);
            model.addAttribute("listaUbicacion", listaUbicacion);
            model.addAttribute("errorCompany", "Ningún campo puede dejarse vacío, intente crear nuevamente por favor");
            System.out.println(bindingResult.getFieldError());
            return "usuario/newIncidencia";
        }else{

            Usuario user = (Usuario) session.getAttribute("usuario");
            Instant fecha = inicidenciaRepository.fecha();
            incidencia.setFecha(fecha);
            incidencia.setCodigo(user);
            inicidenciaRepository.save(incidencia);
            int i = incidencia.getId();
            String idInci = String.valueOf(i);

            if(img.isEmpty()){
                System.out.println("imagen vacia");
            }else{
                try {
                    byte[] bytes = img.getBytes();
                    Perfil perfil = new Perfil();
                    perfil.setName("Incidencia_"+idInci+".png");
                    perfil.setFileBase64(Base64.getEncoder().encodeToString(bytes));
                    System.out.println("llegue hasta aqui");
                    perfilDao.subirImagen(perfil);
                }catch (Exception e){
                    System.out.println("Hay excepcion");
                }

            }

            attr.addFlashAttribute("msg","Incidencia creada exitosamente.");
            return "redirect:/usuario/misIncidencias";
        }

    }

    @GetMapping("/detalle")
    public String detalleIncidencia(Model model,
                                    @RequestParam("id") Integer id){
        Optional<Inicidencia> optInicidencia = inicidenciaRepository.findById(id);


        if (optInicidencia.isPresent()){
            Inicidencia inicidencia = optInicidencia.get();

            model.addAttribute("incidencia", inicidencia);
            model.addAttribute("imgInc",perfilDao.obtenerImagen("Incidencia_"+String.valueOf(id)).getFileBase64());
            model.addAttribute("imgUs",perfilDao.obtenerImagen(inicidencia.getCodigo().getId()).getFileBase64());
            return "usuario/detalleIncid";
        }else{
            return "redirect:/usuario/misIncidencias";
        }
    }

    @GetMapping("/newInciden")
    public String newInciden(@ModelAttribute("incidencia") Inicidencia incidencia, Model model) {
        List<Tipo> listaTipo  =tipoRepository.findAll();
        List<Urgencia> listaUrgencia  =urgenciaRepository.findAll();
        List<Ubicacion> listaUbicacion = ubicacionRepository.findAll();
        model.addAttribute("listaTipo", listaTipo);
        model.addAttribute("listaUrgencia", listaUrgencia);
        model.addAttribute("listaUbicacion", listaUbicacion);
        return "usuario/newIncidencia";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        Usuario perfilUsuario = usuarioRepository.userPerfil(u.getId());
        model.addAttribute("user",perfilUsuario);
        model.addAttribute("imgperfil",perfilDao.obtenerImagen(u.getId()).getFileBase64());
        return "usuario/perfil";
    }
    @GetMapping("/misIncidencias")
    public String misIncidencias(Model model,HttpSession session, @ModelAttribute("incidencia") Inicidencia incidencia) {
        Usuario user = (Usuario) session.getAttribute("usuario");
        model.addAttribute("listaIncidencias",inicidenciaRepository.userIncidencias(user.getId()));
        return "usuario/incidencias";
    }

    @GetMapping("/ListaComentarios")
    public String comentariosIncidencia() {
        return "usuario/lista_comentarios";
    }

    @GetMapping("/lista_comentarios")
    public String listacomentarios(HttpSession session, Model model, @RequestParam("id") Integer id, @ModelAttribute("comentario") Comentario comentario ) {
        List<Comentario> listaComentariosSeguridad = comentarioRepository.IncidenciasComentariosSeguridad(id);
        List<Comentario> listaComentariosUsuario = comentarioRepository.IncidenciasComentariosUsuario(id);

        Usuario user = (Usuario) session.getAttribute("usuario");

        Optional<Inicidencia> incidencia_flotante = inicidenciaRepository.findById(id);

        if (incidencia_flotante.isPresent()) {
            Inicidencia incidencia = incidencia_flotante.get();
            int i = 0;
            if (Objects.equals(incidencia.getCodigo().getId(), user.getId())) {
                i = 1;
            }

            if (listaComentariosSeguridad.size() == 0 || i == 0) {
                return "redirect:/usuario/misIncidencias";
            } else {
                model.addAttribute("id", id);
                model.addAttribute("incidencia", incidencia);
                model.addAttribute("listaComentariosSeguridad", listaComentariosSeguridad);
                model.addAttribute("listaComentariosUsuario", listaComentariosUsuario);
                return "usuario/lista_comentarios";
            }
        }else{
            return "redirect:/usuario/misIncidencias";
        }
    }

    @PostMapping("/comentar")
    public String comentar(@RequestParam("id") Integer id,
                           @RequestParam("comentarioUsuario") String comentarioUsuario,
                           RedirectAttributes redirectAttributes, @ModelAttribute("incidencia") @Valid Inicidencia incidencia,
                           BindingResult bindingResult, Model model, HttpSession session){
        Usuario user = (Usuario) session.getAttribute("usuario");

        if(bindingResult.hasErrors()){
            System.out.println("----------------------------- Error detectado --------------------------");
            System.out.println(bindingResult.getFieldError());
            model.addAttribute("id",id);
            model.addAttribute("listaIncidencias",inicidenciaRepository.userIncidencias(user.getId()));
            return "usuario/misIncidencias";
        }else {
            int max = incidencia.getMax_usuario();
            max+=1;
            inicidenciaRepository.comentarIncidenciaUsuario(comentarioUsuario,max,id);
            comentarioRepository.comentarIncidenciaUsuario(comentarioUsuario,id);
            if(max==5){
                inicidenciaRepository.cambiarEstadoIncidencia(id);
                redirectAttributes.addFlashAttribute("msg2","Incidencia comentada. Se ha llegado al máximo de comentarios por incidencia.");
                return "redirect:/usuario/misIncidencias";
            }else{
                redirectAttributes.addFlashAttribute("msg3","Incidencia comentada. Recuerda que hay un máximo de 5 comentarios por incidencia.");
                return "redirect:/usuario/misIncidencias";
            }
        }
    }

    @PostMapping("/incidenciaResuelta")
    public String incidenciaResuelta(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes) {
        inicidenciaRepository.cambiarEstadoIncidencia(id);
        redirectAttributes.addFlashAttribute("msg4","La incidencia con ID #"+id+" ha sido resuelta.");
        return "redirect:/usuario/misIncidencias";
    }
    @PostMapping("/guardarImagenes")
    public String imagenSave(@RequestParam("fruta") String nombre ,@RequestParam(name = "f_subir",required = false) MultipartFile img, RedirectAttributes redirectAttributes, HttpSession session){
        int icono;
        if(nombre.equals("pina")){
            icono = 1;
        }else if(nombre.equals("sandia")){
            icono =2;
        }else{
            icono = 3;
        }
        Usuario user = (Usuario) session.getAttribute("usuario");
        usuarioRepository.saveAvatar(icono,user.getId());

        if(img.isEmpty()){
            System.out.println("imagen vacia");
        }else{
            try {
                byte[] bytes = img.getBytes();
                Perfil perfil = new Perfil();
                perfil.setName(user.getId()+".png");
                perfil.setFileBase64(Base64.getEncoder().encodeToString(bytes));
                System.out.println("llegue hasta aqui");
                perfilDao.subirImagen(perfil);
            }catch (Exception e){
                System.out.println("Hay excepcion");
            }

        }

        redirectAttributes.addFlashAttribute("mensaje","Se han realizado los cambios correctamente.");
        return "redirect:/usuario/perfil";
    }


}
