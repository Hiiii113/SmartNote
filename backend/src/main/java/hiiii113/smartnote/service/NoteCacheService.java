package hiiii113.smartnote.service;

import hiiii113.smartnote.dto.NoteDetailDto;

public interface NoteCacheService
{
    // 查询笔记详情（缓存）
    NoteDetailDto getNodeDetailCache(Long noteId);
}
