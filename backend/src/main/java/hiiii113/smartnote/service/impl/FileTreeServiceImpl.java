package hiiii113.smartnote.service.impl;

import hiiii113.smartnote.dto.FileTreeNodeDto;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.enums.FileTreeNodeTypeEnum;
import hiiii113.smartnote.service.FileTreeService;
import hiiii113.smartnote.service.FolderService;
import hiiii113.smartnote.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FileTreeServiceImpl implements FileTreeService
{
    private final FolderService folderService;
    private final NoteService noteService;

    @Override
    public List<FileTreeNodeDto> getRootNotes(Long userId)
    {
        // 查询文件夹
        List<Folder> folders = folderService.lambdaQuery()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, 0) // 0-根目录
                .list();

        // 查询笔记
        List<Note> notes = noteService.lambdaQuery()
                .eq(Note::getUserId, userId)
                .eq(Note::getFolderId, 0) // 0-根目录
                .list();

        // 合并两个 list 为 FileTreeNodeDto

        return Stream.concat(
                folders.stream().map(folder ->
                {
                    FileTreeNodeDto dto = new FileTreeNodeDto();
                    dto.setId(folder.getId());
                    dto.setName(folder.getName());
                    dto.setParentId(folder.getParentId());
                    dto.setType(FileTreeNodeTypeEnum.FOLDER);
                    return dto;
                }),
                notes.stream().map(note ->
                {
                    FileTreeNodeDto dto = new FileTreeNodeDto();
                    dto.setId(note.getId());
                    dto.setName(note.getTitle());
                    dto.setParentId(note.getFolderId());
                    dto.setType(FileTreeNodeTypeEnum.NOTE);
                    return dto;
                })
        ).collect(Collectors.toList());
    }
}
