package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.entity.AiSummary;
import hiiii113.smartnote.entity.Note;
import hiiii113.smartnote.entity.NotePermission;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.AiSummaryMapper;
import hiiii113.smartnote.service.AiSummaryService;
import hiiii113.smartnote.service.NotePermissionService;
import hiiii113.smartnote.service.NoteService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiSummaryServiceImpl extends ServiceImpl<AiSummaryMapper, AiSummary> implements AiSummaryService
{
    private final NoteService noteService;
    private final NotePermissionService notePermissionService;

    @Override
    public void saveAiSummary(Long noteId, String response)
    {
        // 笔记已经存在记录则覆盖
        if (lambdaQuery().eq(AiSummary::getNoteId, noteId).exists())
        {
            // 构建数据
            AiSummary aiSummary = new AiSummary();
            aiSummary.setNoteId(noteId);
            aiSummary.setSummary(response);

            // 保存
            this.lambdaUpdate()
                    .eq(AiSummary::getNoteId, noteId)
                    .update(aiSummary);
        }
        else
        {
            // 构建数据
            AiSummary aiSummary = new AiSummary();
            aiSummary.setNoteId(noteId);
            aiSummary.setSummary(response);

            // 保存
            this.save(aiSummary);
        }
    }

    @Override
    public String getAiSummary(Long userId, Long noteId)
    {
        // 校验
        Note note = noteService.getById(noteId);
        if (note == null)
        {
            throw new BusinessException("笔记不存在!", Result.CODE_BAD_REQUEST);
        }

        // 非所有者或授权不可查看
        if (!note.getUserId().equals(userId))
        {
            // 检查权限
            NotePermission permission = notePermissionService.getOne(
                    new LambdaQueryWrapper<NotePermission>()
                            .eq(NotePermission::getNoteId, noteId)
                            .eq(NotePermission::getUserId, userId)
            );
            // 无授权记录的话
            if (permission == null)
            {
                throw new BusinessException("无权访问该笔记!", Result.CODE_FORBIDDEN);
            }
        }

        // 查询 AI 总结
        AiSummary summary = lambdaQuery()
                .eq(AiSummary::getNoteId, noteId)
                .one();

        return summary != null ? summary.getSummary() : null;
    }
}
