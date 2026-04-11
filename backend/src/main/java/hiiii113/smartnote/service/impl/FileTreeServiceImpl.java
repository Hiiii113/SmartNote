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

/**
 * 文件树 service 实现类
 */
@Service
@RequiredArgsConstructor
public class FileTreeServiceImpl implements FileTreeService
{
    private final FolderService folderService;
    private final NoteService noteService;

    // 获取节点（通用方法）
    @Override
    public List<FileTreeNodeDto> getNodes(Long userId, Long parentId, Integer isDeleted)
    {
        // 查询文件夹
        List<Folder> folders = folderService.lambdaQuery()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, parentId)
                .eq(Folder::getIsDeleted, isDeleted)
                .list();

        // 查询笔记
        List<Note> notes = noteService.lambdaQuery()
                .eq(Note::getUserId, userId)
                .eq(Note::getFolderId, parentId)
                .eq(Note::getIsDeleted, isDeleted)
                .list();

        // 合并转换为 FileTreeNodeDto
        return convertToDto(folders, notes);
    }

    // 获取根目录的节点
    @Override
    public List<FileTreeNodeDto> getRootNodes(Long userId)
    {
        return getNodes(userId, 0L, 0);
    }

    // 获取子节点
    @Override
    public List<FileTreeNodeDto> getChildrenNodes(Long userId, Long parentId)
    {
        return getNodes(userId, parentId, 0);
    }

    // 获取回收站根目录
    @Override
    public List<FileTreeNodeDto> getTrashRootNodes(Long userId)
    {
        // 回收站根目录 parentId 设置为了 -1，并且根目录的 is_deleted 字段都是 1
        return getNodes(userId, -1L, 1);
    }

    // 将文件夹和笔记列表转换为 FileTreeNodeDto 列表，使用 stream
    private List<FileTreeNodeDto> convertToDto(List<Folder> folders, List<Note> notes)
    {
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
