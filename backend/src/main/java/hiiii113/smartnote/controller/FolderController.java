package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.CreateFolderDto;
import hiiii113.smartnote.dto.RenameFolderDto;
import hiiii113.smartnote.service.FolderService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文件夹的 controller
 */
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController
{
    private final FolderService folderService;

    // 创建文件夹
    @PostMapping
    public Result<Void> createFolder(@RequestBody CreateFolderDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 创建文件夹，传入 dto
        folderService.createFolder(userId, dto);
        return Result.ok("创建成功！");
    }

    // 重命名文件夹
    @PutMapping("/{folderId}")
    public Result<Void> renameFolder(@PathVariable Long folderId, @RequestBody RenameFolderDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 重命名
        folderService.renameFolder(userId, folderId, dto.getName());
        return Result.ok("重命名成功！");
    }

    // 删除文件夹（逻辑删除，设置字段 is_deleted = 1）
    @DeleteMapping("/{folderId}")
    public Result<Void> deleteFolder(@PathVariable Long folderId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 删除（实际为更新字段）
        folderService.deleteFolder(userId, folderId);
        return Result.ok("移入回收站成功！");
    }

    // 恢复文件夹
    @PostMapping("/{folderId}/restore")
    public Result<Void> restoreFolder(@PathVariable Long folderId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 恢复文件夹（更新字段 is_deleted 为 0）
        folderService.restoreFolder(userId, folderId);
        return Result.ok("恢复成功！");
    }

    // 永久删除文件夹（物理删除）
    @DeleteMapping("/{folderId}/permanent")
    public Result<Void> permanentDelete(@PathVariable Long folderId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 永久删除
        folderService.permanentDelete(userId, folderId);
        return Result.ok("删除成功！");
    }
}
