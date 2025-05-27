package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    private JsonNullable<String> title = JsonNullable.undefined();
    private JsonNullable<String> status = JsonNullable.undefined();
    private JsonNullable<String> content = JsonNullable.undefined();
    private JsonNullable<Long> assigneeId = JsonNullable.undefined();

    @JsonProperty("label_ids")
    private JsonNullable<Set<Long>> labelIds = JsonNullable.undefined();
}
