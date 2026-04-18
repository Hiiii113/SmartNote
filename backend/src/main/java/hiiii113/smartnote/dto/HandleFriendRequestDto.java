package hiiii113.smartnote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理好友申请
 */
@Data
public class HandleFriendRequestDto
{
    // 是否同意
    @NotNull(message = "请指定是否同意该申请")
    private Boolean accept;
}
