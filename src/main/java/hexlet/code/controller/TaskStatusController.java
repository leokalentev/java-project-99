package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class TaskStatusController {
    @Autowired
    private TaskStatusRepository repository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusMapper mapper;

    @GetMapping(path = "/task_statuses")
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var tasksStatus = repository.findAll();
        var res = tasksStatus.stream().map(mapper::map).toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasksStatus.size()))
                .body(res);
    }

    @GetMapping(path = "/task_statuses/{id}")
    public TaskStatusDTO show(@PathVariable Long id) {
        var taskStatus = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found"));
        var taskStatusDTO = mapper.map(taskStatus);
        return taskStatusDTO;
    }

    @PostMapping(path = "/task_statuses")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(@RequestBody @Valid TaskStatusCreateDTO newTask) {
        if (repository.existsByName(newTask.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус с таким названием уже существует");
        }

        if (repository.existsBySlug(newTask.getSlug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус с таким слагом уже существует");
        }


        var taskStatus = mapper.map(newTask);
        repository.save(taskStatus);

        var taskStatusDTO = mapper.map(taskStatus);
        return taskStatusDTO;
    }

    @PutMapping(path = "/task_statuses/{id}")
    @PreAuthorize("isAuthenticated()")
    public TaskStatusDTO update(@PathVariable Long id, @RequestBody TaskStatusUpdateDTO newTask)
            throws ResponseStatusException {
        var taskStatus = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found"));

        newTask.getName().ifPresent(name -> {
            if (name.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле name не может быть пустым");
            }
            if (repository.existsByName(name) && !name.equals(taskStatus.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус с таким названием уже существует");
            }
            taskStatus.setName(name);
        });

        newTask.getSlug().ifPresent(slug -> {
            if (slug.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле slug не может быть пустым");
            }
            if (repository.existsBySlug(slug) && !slug.equals(taskStatus.getSlug())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус с таким слагом уже существует");
            }
            taskStatus.setSlug(slug);
        });

        repository.save(taskStatus);
        return mapper.map(taskStatus);
    }

    @DeleteMapping(path = "/task_statuses/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        var taskStatus = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found task status"));

        if (taskRepository.existsByTaskStatusId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "У статуса есть задача");
        }
        repository.delete(taskStatus);
    }
}
