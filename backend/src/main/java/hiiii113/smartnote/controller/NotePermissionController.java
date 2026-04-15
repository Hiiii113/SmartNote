package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.NotePermissionDto;
import hiiii113.smartnote.dto.SetPermissionDto;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.entity.NotePermission;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.NotePermissionService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记权限 controller
 */
@RestController
@RequestMapping("/notes/{noteId}/permissions")
@RequiredArgsConstructor
public class NotePermissionController
{
    private final NoteService noteService;
    private final NotePermissionService notePermissionService;
    private final UserService userService;

    // 获取笔记的权限列表（前端查看并操作）
    @GetMapping
    @LogAnnotation(module = "笔记权限", operator = "获取权限列表")
    public Result<List<NotePermissionDto>> getPermissions(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 校验笔记所有权
        checkOwner(userId, noteId);

        // 查询出 list
        List<NotePermission> permissions = notePermissionService.getNotePermissions(noteId);
        // 转换成 dto 并返回给前端
        List<NotePermissionDto> dtoList = permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return Result.ok(dtoList);
    }

    // 设置用户权限
    @PostMapping
    @LogAnnotation(module = "笔记权限", operator = "设置权限")
    public Result<Void> setPermission(@PathVariable Long noteId, @RequestBody SetPermissionDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 校验笔记所有权
        checkOwner(userId, noteId);

        // 设置权限
        notePermissionService.setPermission(noteId, dto.getUserId(), dto.getCanEdit());
        return Result.ok("设置成功");
    }

    // 删除用户权限
    @DeleteMapping("/{targetUserId}")
    @LogAnnotation(module = "笔记权限", operator = "删除权限")
    public Result<Void> removePermission(@PathVariable Long noteId, @PathVariable Long targetUserId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 校验笔记所有权
        checkOwner(userId, noteId);

        // 删除权限
        notePermissionService.removePermission(noteId, targetUserId);
        return Result.ok("删除成功");
    }

    // 校验笔记所有权
    private void checkOwner(Long userId, Long noteId)
    {
        Note note = noteService.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在", Result.CODE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId))
        {
            throw new BusinessException("无权操作", Result.CODE_FORBIDDEN);
        }
    }

    // 转换为 DTO
    private NotePermissionDto convertToDto(NotePermission permission)
    {
        NotePermissionDto dto = new NotePermissionDto();
        dto.setId(permission.getId());
        dto.setUserId(permission.getUserId());
        dto.setCanEdit(permission.getCanEdit());
        dto.setCreatedAt(permission.getCreatedAt());

        // 查询用户信息
        User user = userService.getById(permission.getUserId());
        if (user != null)
        {
            dto.setUsername(user.getUsername());
            dto.setAvatar(user.getAvatar());
        }
        return dto;
    }
}
