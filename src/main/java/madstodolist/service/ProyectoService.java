package madstodolist.service;

import madstodolist.dto.ProyectoData;
import madstodolist.model.Equipo;
import madstodolist.model.Proyecto;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.ProyectoRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProyectoService {
    Logger logger = LoggerFactory.getLogger(ProyectoService.class);

    @Autowired
    private ProyectoRepository proyectoRepository;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public ProyectoData crearProyecto(String nombre, String descripcion, Long equipoId) {
        logger.debug("Creando proyecto " + nombre);

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("El equipo no existe"));

        Proyecto proyecto = new Proyecto(nombre, equipo);
        proyecto.setDescripcion(descripcion);

        proyecto = proyectoRepository.save(proyecto);
        return modelMapper.map(proyecto, ProyectoData.class);
    }

    @Transactional(readOnly = true)
    public List<ProyectoData> findAllProyectosByEquipo(Long equipoId) {
        logger.debug("Recuperando proyectos del equipo " + equipoId);
        return proyectoRepository.findByEquipoId(equipoId).stream()
                .map(p -> modelMapper.map(p, ProyectoData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProyectoData findById(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return null;
        return modelMapper.map(proyecto, ProyectoData.class);
    }
}
