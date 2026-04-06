package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.FileTreeNodeDto;
import hiiii113.smartnote.service.FileTreeService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController
{
    private final FileTreeService fileTreeService;

    // 获取根目录的文件或笔记
    @GetMapping("/root")
    public Result<List<FileTreeNodeDto>> getRootNodes()
    {
        Long userId = StpUtil.getLoginIdAsLong();

        List<FileTreeNodeDto> res = fileTreeService.getRootNotes(userId);

        return Result.ok(res);
    }
}
