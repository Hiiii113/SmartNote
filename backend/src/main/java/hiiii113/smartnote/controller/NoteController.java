package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.CreateNoteDto;
import hiiii113.smartnote.dto.NoteDetailDto;
import hiiii113.smartnote.dto.UpdateNoteDto;
import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记的 controller
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController
{
    private final NoteService noteService;

    // 新建笔记
    @PostMapping
    public Result<Void> createNote(@RequestBody CreateNoteDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 新建一个笔记
        noteService.createNote(userId, dto);
        return Result.ok("新建成功！");
    }

    // 获取笔记详情
    @GetMapping("/{noteId}")
    public Result<NoteDetailDto> getNoteDetail(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取笔记详情
        NoteDetailDto dto = noteService.getNoteDetail(userId, noteId);
        return Result.ok(dto);
    }

    // 更新笔记
    @PutMapping("/{noteId}")
    public Result<Void> updateNote(@PathVariable Long noteId, @RequestBody UpdateNoteDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 更新笔记
        noteService.updateNote(userId, noteId, dto);
        return Result.ok("更新成功！");
    }

    // 删除笔记（设置 is-deleted 字段为 1）
    @DeleteMapping("/{noteId}")
    public Result<Void> deleteNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 删除笔记
        noteService.deleteNote(userId, noteId);
        return Result.ok("移入回收站成功！");
    }

    // 恢复笔记
    @PostMapping("/{noteId}/restore")
    public Result<Void> restoreNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 恢复笔记（更新字段）
        noteService.restoreNote(userId, noteId);
        return Result.ok("恢复成功！");
    }

    // 永久删除笔记
    @DeleteMapping("/{noteId}/permanent")
    public Result<Void> permanentDeleteNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 永久删除
        noteService.permanentDelete(userId, noteId);
        return Result.ok("删除成功！");
    }

    // 修改笔记可见性
    @PutMapping("/{noteId}/visibility")
    public Result<Void> updateVisibility(@PathVariable Long noteId, @RequestParam NoteVisibilityTypeEnum visibility)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改笔记可见性
        noteService.updateVisibility(userId, noteId, visibility);
        return Result.ok("可见性修改成功！");
    }
}
