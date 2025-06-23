package hexlet.code.controller;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@AllArgsConstructor
public class LabelController {

    private final LabelRepository repository;
    private final LabelMapper mapper;
    private final TaskRepository taskRepository;

    @GetMapping(path = "/labels")
    public ResponseEntity<List<LabelDTO>> index() {
        var labels = repository.findAll();
        var res = labels.stream().map(mapper::map).toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(res);
    }

    @GetMapping(path = "/labels/{id}")
    public LabelDTO show(@PathVariable Long id) {
        var label = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        var labelDTO = mapper.map(label);
        return labelDTO;
    }

    @PostMapping(path = "/labels")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody LabelCreateDTO labelCreateDTO) {
        var label = mapper.map(labelCreateDTO);
        repository.save(label);

        var labelDTO = mapper.map(label);
        return labelDTO;
    }

    @PutMapping(path = "/labels/{id}")
    public LabelDTO update(@PathVariable Long id, @Valid @RequestBody LabelUpdateDTO newLabel) {
        var label = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        mapper.update(newLabel, label);
        repository.save(label);

        var labelDTO = mapper.map(label);
        return labelDTO;
    }

    @DeleteMapping(path = "/labels/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        var label = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        repository.delete(label);
    }
}
