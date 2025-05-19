package hexlet.code.specification;

import hexlet.code.dto.TaskDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Component
public class TaskSpecification {

    public Specification<Task> build(TaskDTO params) {
        return Specification.where(titleContains(params.getTitle()))
                .and(assigneeIs(params.getAssigneeId()))
                .and(statusIs(params.getStatus()))
                .and(hasAnyLabel(params.getLabelIds()));
    }

    private Specification<Task> titleContains(String substr) {
        if (substr == null || substr.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + substr.toLowerCase() + "%");
    }

    private Specification<Task> assigneeIs(Long userId) {
        if (userId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(root.get("assignee").get("id"), userId);
    }

    private Specification<Task> statusIs(String slug) {
        if (slug == null || slug.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(root.get("taskStatus").get("slug"), slug);
    }

    private Specification<Task> hasAnyLabel(Set<Long> labelIds) {
        if (CollectionUtils.isEmpty(labelIds)) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            Join<Task, Label> labels = root.join("labels", JoinType.LEFT);
            return labels.get("id").in(labelIds);
        };
    }
}
