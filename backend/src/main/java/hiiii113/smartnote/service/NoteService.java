package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.CreateNoteDto;
import hiiii113.smartnote.dto.NoteDetailDto;
import hiiii113.smartnote.dto.NoteSyncMessageDto;
import hiiii113.smartnote.dto.UpdateNoteDto;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;

import java.util.List;

/**
 * 笔记 service
 */
public interface NoteService extends IService<Note>
{
    // 创建笔记
    void createNote(Long userId, CreateNoteDto dto);

    // 更新笔记
    void updateNote(Long userId, Long noteId, UpdateNoteDto dto);

    // 删除笔记（逻辑删除，移入回收站）
    void deleteNote(Long userId, Long noteId);

    // 恢复笔记（移回根目录）
    void restoreNote(Long userId, Long noteId);

    // 获取笔记详情
    NoteDetailDto getNoteDetail(Long userId, Long noteId);

    // 永久删除笔记
    void permanentDelete(Long userId, Long noteId);

    // 获取笔记内容（用于AI分析）
    String getNoteContent(Long userId, Long noteId);

    // 修改笔记可见性
    void updateVisibility(Long userId, Long noteId, NoteVisibilityTypeEnum visibility);

    // 查询笔记时候的权限校验
    boolean checkGetNodeDetailPermission(Long userId, Long noteId);

    // 增加笔记全局访问次数
    void incrementViewCount(Long noteId);

    // 获取最近常看三篇笔记
    List<Note> getHotNotes(Long userId, Long noteId);

    // 获取笔记同步消息
    NoteSyncMessageDto getNoteSyncMessage(Long noteId);
}
