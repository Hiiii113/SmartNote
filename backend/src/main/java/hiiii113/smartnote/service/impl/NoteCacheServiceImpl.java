package hiiii113.smartnote.service.impl;

import hiiii113.smartnote.dto.NoteDetailDto;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.service.NoteCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteCacheServiceImpl implements NoteCacheService
{
    private final NoteMapper noteMapper;

    @Override
    @Cacheable(
            value = "noteCache",
            key = "#noteId",
            unless = "#result == null || @noteServiceImpl.getViewCount(#noteId) <= 20" // 除非这篇笔记不是热点数据
    )
    public NoteDetailDto getNodeDetailCache(Long noteId)
    {

        // 查询数据库
        Note note = noteMapper.selectById(noteId);

        // 转换为 DTO 返回
        NoteDetailDto dto = new NoteDetailDto();
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setTags(note.getTags());
        dto.setVisibility(note.getVisibility());
        dto.setUpdatedAt(note.getUpdatedAt());

        return dto;
    }
}
