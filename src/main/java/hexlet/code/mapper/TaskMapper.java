package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired private UserRepository userRepository;
    @Autowired private TaskStatusRepository taskStatusRepository;
    @Autowired private LabelRepository labelRepository;

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.slug", target = "status")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(target = "labelIds", expression = "java(mapLabelsToIds(task.getLabels()))")
    public abstract TaskDTO map(Task task);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "labelIds", target = "labels")
    public abstract Task map(TaskCreateDTO dto);

    protected TaskStatus mapTaskStatus(String slug) {
        if (slug == null) {
            throw new ResourceNotFoundException("Поле status обязательно");
        }
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus not found"));
    }

    protected User mapAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    protected Set<Label> mapLabels(Set<Long> labelIds) {
        return new HashSet<>(labelRepository.findAllById(labelIds));
    }

    protected Set<Long> mapLabelsToIds(Set<Label> labels) {
        if (labels == null) {
            return Collections.emptySet();
        }
        return labels.stream().map(Label::getId).collect(Collectors.toSet());
    }

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "labelIds", target = "labels")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task task);
}

