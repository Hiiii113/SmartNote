package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.entity.NotePermission;
import hiiii113.smartnote.enums.NoteVisibilityTypeEnum;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.NoteMapper;
import hiiii113.smartnote.mapper.NotePermissionMapper;
import hiiii113.smartnote.service.NotePermissionService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记权限 service 实现类
 */
@Service
@RequiredArgsConstructor
public class NotePermissionServiceImpl extends ServiceImpl<NotePermissionMapper, NotePermission> implements NotePermissionService
{
    private final NoteMapper noteMapper;

    // 设置用户权限
    @Override
    public void setPermission(Long noteId, Long userId, Boolean canEdit)
    {
        // 先看这篇笔记是不是好友可见的
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getVisibility() != NoteVisibilityTypeEnum.FRIENDS)
        {
            String notice = null;
            if (note != null && note.getVisibility() == NoteVisibilityTypeEnum.PRIVATE)
            {
                notice = "当前笔记为私有，无法分享";
            }
            else if (note != null && note.getVisibility() == NoteVisibilityTypeEnum.PUBLIC)
            {
                notice = "当前笔记为公开，无需设置好友权限";
            }
            throw new BusinessException(notice, Result.CODE_BAD_REQUEST);
        }

        // 查找是否已存在
        NotePermission permission = lambdaQuery()
                .eq(NotePermission::getNoteId, noteId)
                .eq(NotePermission::getUserId, userId)
                .one();

        if (permission != null)
        {
            // 更新
            permission.setCanEdit(canEdit ? 1 : 0);
            this.updateById(permission);
        }
        else
        {
            // 新增
            permission = new NotePermission();
            permission.setNoteId(noteId);
            permission.setUserId(userId);
            permission.setCanEdit(canEdit ? 1 : 0);
            permission.setCreatedAt(LocalDateTime.now());
            this.save(permission);
        }
    }

    // 删除用户权限
    @Override
    public void removePermission(Long noteId, Long userId)
    {
        lambdaUpdate()
                .eq(NotePermission::getNoteId, noteId)
                .eq(NotePermission::getUserId, userId)
                .remove();
    }

    // 获取笔记的所有授权用户
    @Override
    public List<NotePermission> getNotePermissions(Long noteId)
    {
        return lambdaQuery()
                .eq(NotePermission::getNoteId, noteId)
                .list();
    }

    // 检查用户是否有权限
    @Override
    public boolean hasPermission(Long noteId, Long userId)
    {
        return lambdaQuery()
                .eq(NotePermission::getNoteId, noteId)
                .eq(NotePermission::getUserId, userId)
                .exists();
    }

    // 检查用户是否可编辑
    @Override
    public boolean canEdit(Long noteId, Long userId)
    {
        NotePermission permission = lambdaQuery()
                .eq(NotePermission::getNoteId, noteId)
                .eq(NotePermission::getUserId, userId)
                .one();

        // 两个都有的时候才返回 true
        return permission != null && permission.getCanEdit() == 1;
    }
}
