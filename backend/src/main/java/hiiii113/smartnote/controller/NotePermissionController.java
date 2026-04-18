package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.NotePermissionDto;
import hiiii113.smartnote.dto.SetPermissionDto;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.NotePermissionService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 获取笔记的权限列表
     *
     * @param noteId 笔记 id
     * @return List<NotePermissionDto>
     */
    @GetMapping
    @LogAnnotation(module = "笔记权限", operator = "获取权限列表")
    public Result<List<NotePermissionDto>> getPermissions(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 校验笔记所有权（只有所有者才能看到笔记的权限列表）
        checkOwner(userId, noteId);

        // 查询出 list
        List<NotePermissionDto> permissions = notePermissionService.getNotePermissions(noteId);
        return Result.ok(permissions);
    }

    /**
     * 设置用户权限
     *
     * @param noteId 笔记 id
     * @param dto    相关数据
     */
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
        return Result.ok();
    }

    /**
     * 删除用户权限
     *
     * @param noteId       笔记 id
     * @param targetUserId 目标好友 id
     */
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
        return Result.ok();
    }

    /**
     * 校验笔记所有权
     * 查看当前笔记是否存在、是否是该用户的笔记
     *
     * @param userId 用户 id
     * @param noteId 笔记 id
     */
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
}
