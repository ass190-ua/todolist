package madstodolist.service;

import madstodolist.dto.ChecklistItemData;
import madstodolist.model.ChecklistItem;
import madstodolist.model.Tarea;
import madstodolist.repository.ChecklistItemRepository;
import madstodolist.repository.TareaRepository;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChecklistItemService {

    Logger logger = LoggerFactory.getLogger(ChecklistItemService.class);

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public ChecklistItemData crearItem(Long tareaId, String texto) {
        logger.debug("Creando checklist item en tarea " + tareaId);

        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) {
            throw new ChecklistItemServiceException("No existe tarea con id " + tareaId);
        }

        ChecklistItem item = new ChecklistItem(tarea, texto);
        checklistItemRepository.save(item);

        return modelMapper.map(item, ChecklistItemData.class);
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemData> obtenerChecklistDeTarea(Long tareaId) {
        logger.debug("Obteniendo checklist de tarea " + tareaId);

        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) {
            throw new ChecklistItemServiceException("No existe tarea con id " + tareaId);
        }

        return checklistItemRepository.findByTareaOrderByIdAsc(tarea)
                .stream()
                .map(item -> modelMapper.map(item, ChecklistItemData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChecklistItemData findById(Long itemId) {
        ChecklistItem item = checklistItemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new ChecklistItemServiceException("No existe checklist item con id " + itemId);
        }
        return modelMapper.map(item, ChecklistItemData.class);
    }

    @Transactional
    public ChecklistItemData completarItem(Long itemId) {
        ChecklistItem item = checklistItemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new ChecklistItemServiceException("No existe checklist item con id " + itemId);
        }

        item.completar();
        checklistItemRepository.save(item);

        return modelMapper.map(item, ChecklistItemData.class);
    }

    @Transactional
    public ChecklistItemData desmarcarItem(Long itemId) {
        ChecklistItem item = checklistItemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new ChecklistItemServiceException("No existe checklist item con id " + itemId);
        }

        item.desmarcarCompletado();
        checklistItemRepository.save(item);

        return modelMapper.map(item, ChecklistItemData.class);
    }

    @Transactional
    public void borrarItem(Long itemId) {
        if (!checklistItemRepository.existsById(itemId)) {
            throw new ChecklistItemServiceException("No existe checklist item con id " + itemId);
        }
        checklistItemRepository.deleteById(itemId);
    }
}
