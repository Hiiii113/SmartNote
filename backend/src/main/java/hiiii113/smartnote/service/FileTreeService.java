package hiiii113.smartnote.service;

import hiiii113.smartnote.dto.FileTreeNodeDto;
import hiiii113.smartnote.utils.Result;

import java.util.List;

public interface FileTreeService
{
    List<FileTreeNodeDto> getRootNotes(Long userId);
}
