package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
public class TaskCreateDTO {
    @NotBlank
    @Size(min = 1)
    private String title;

    private Integer index;
    private String content;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @JsonProperty("taskLabelIds")
    private Set<Long> labelIds = new HashSet<>();

    @NotBlank
    private String status;
}
