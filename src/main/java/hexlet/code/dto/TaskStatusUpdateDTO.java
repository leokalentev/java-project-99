package hexlet.code.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    @NotNull
    private JsonNullable<String> name = JsonNullable.undefined();

    @NotNull
    private JsonNullable<String> slug = JsonNullable.undefined();
}
