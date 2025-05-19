package hexlet.code.controller;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class TaskController {
    @Autowired
    private TaskRepository repository;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private TaskSpecification specBuilder;

    @GetMapping(path = "/tasks")
    public ResponseEntity<List<TaskDTO>> index(TaskDTO params) {
        var spec = specBuilder.build(params);
        var tasks = repository.findAll(spec);
        var res = tasks.stream().map(mapper::map).toList();
        return ResponseEntity
                .ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(res);
    }

    @GetMapping(path = "/tasks/{id}")
    public TaskDTO show(@PathVariable Long id) {
        var task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        var taskDTO = mapper.map(task);
        return taskDTO;
    }

    @PostMapping(path = "/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@RequestBody @Valid TaskCreateDTO taskCreateDTO) {
        var task = mapper.map(taskCreateDTO);
        repository.save(task);

        var taskDTO = mapper.map(task);
        return taskDTO;
    }

    @PutMapping(path = "/tasks/{id}")
    public TaskDTO update(@PathVariable Long id, @RequestBody TaskUpdateDTO taskUpdateDTO) {
        var task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!taskUpdateDTO.getTitle().isPresent() || taskUpdateDTO.getTitle().get().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле title обязательно и не может быть пустым");
        }

        if (!taskUpdateDTO.getStatus().isPresent() || taskUpdateDTO.getStatus().get().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле status обязательно и не может быть пустым");
        }

        mapper.update(taskUpdateDTO, task);
        repository.save(task);

        var taskDTO = mapper.map(task);
        return taskDTO;
    }

    @DeleteMapping(path = "/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        var task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        repository.delete(task);
    }
}
