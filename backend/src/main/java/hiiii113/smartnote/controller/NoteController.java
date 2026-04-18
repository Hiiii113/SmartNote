package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.CreateNoteDto;
import hiiii113.smartnote.dto.NoteDetailDto;
import hiiii113.smartnote.dto.UpdateNoteDto;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记的 controller
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController
{
    private final NoteService noteService;

    /**
     * 新建笔记
     * @param dto 相关数据 dto
     */
    @PostMapping
    @LogAnnotation(module = "笔记", operator = "新建笔记")
    public Result<Void> createNote(@RequestBody CreateNoteDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 新建一个笔记
        noteService.createNote(userId, dto);
        return Result.ok();
    }

    /**
     * 获取笔记详情
     * @param noteId 笔记 id
     * @return NoteDetailDto
     */
    @GetMapping("/{noteId}")
    @LogAnnotation(module = "笔记", operator = "获取笔记详情")
    public Result<NoteDetailDto> getNoteDetail(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取笔记详情
        NoteDetailDto dto = noteService.getNoteDetail(userId, noteId);
        return Result.ok(dto);
    }

    /**
     * 更新笔记
     * @param noteId 笔记 id
     * @param dto 相关数据
     */
    @PutMapping("/{noteId}")
    @LogAnnotation(module = "笔记", operator = "更新笔记")
    public Result<Void> updateNote(@PathVariable Long noteId, @RequestBody UpdateNoteDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 更新笔记
        noteService.updateNote(userId, noteId, dto);
        return Result.ok();
    }

    /**
     * 删除笔记
     * 设置 is-deleted 字段为 1
     * @param noteId 笔记 id
     */
    @DeleteMapping("/{noteId}")
    @LogAnnotation(module = "笔记", operator = "删除笔记")
    public Result<Void> deleteNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 删除笔记
        noteService.deleteNote(userId, noteId);
        return Result.ok();
    }

    /**
     * 恢复笔记
     * @param noteId 笔记 id
     */
    @PostMapping("/{noteId}/restore")
    @LogAnnotation(module = "笔记", operator = "恢复笔记")
    public Result<Void> restoreNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 恢复笔记
        noteService.restoreNote(userId, noteId);
        return Result.ok();
    }

    /**
     * 永久删除笔记
     * @param noteId 笔记 id
     */
    @DeleteMapping("/{noteId}/permanent")
    @LogAnnotation(module = "笔记", operator = "永久删除笔记")
    public Result<Void> permanentDeleteNote(@PathVariable Long noteId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 永久删除
        noteService.permanentDelete(userId, noteId);
        return Result.ok();
    }

    /**
     * 修改笔记可见性
     * @param noteId 笔记 id
     * @param visibility 可见性
     */
    @PutMapping("/{noteId}/visibility")
    @LogAnnotation(module = "笔记", operator = "修改可见性")
    public Result<Void> updateVisibility(@PathVariable Long noteId, @RequestParam NoteVisibilityTypeEnum visibility)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改笔记可见性
        noteService.updateVisibility(userId, noteId, visibility);
        return Result.ok();
    }

    /**
     * 获取最近常看的 3 篇笔记
     * @return List<Note>
     */
    @GetMapping("/hot")
    @LogAnnotation(module = "笔记", operator = "获取热点笔记")
    public Result<List<Note>> getHotNotes()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取
        List<Note> notes = noteService.getHotNotes(userId, null);
        return Result.ok(notes);
    }
}
