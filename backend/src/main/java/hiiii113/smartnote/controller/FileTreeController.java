package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.FileTreeNodeDto;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.FileTreeService;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件树
 */
@RestController
@RequestMapping("/nodes")
@RequiredArgsConstructor
public class FileTreeController
{
    private final FileTreeService fileTreeService;

    /**
     * 获取主页面根目录节点
     *
     * @return List<FileTreeNodeDto>
     */
    @GetMapping("/root")
    @LogAnnotation(module = "文件树", operator = "获取根目录")
    public Result<List<FileTreeNodeDto>> getRootNodes()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取 root 并返回
        List<FileTreeNodeDto> res = fileTreeService.getRootNodes(userId);
        // 返回
        return Result.ok(res);
    }

    /**
     * 获取某个文件夹下的子节点
     *
     * @param parentId 文件夹 id
     * @return List<FileTreeNodeDto>
     */
    @GetMapping("/{parentId}")
    @LogAnnotation(module = "文件树", operator = "获取子节点")
    public Result<List<FileTreeNodeDto>> getChildrenNodes(@PathVariable Long parentId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取子节点并返回
        List<FileTreeNodeDto> res = fileTreeService.getChildrenNodes(userId, parentId);
        // 返回
        return Result.ok(res);
    }

    /**
     * 获取回收站根目录节点
     *
     * @return List<FileTreeNodeDto>
     */
    @GetMapping("/trash/root")
    @LogAnnotation(module = "文件树", operator = "获取回收站")
    public Result<List<FileTreeNodeDto>> getTrashRootNodes()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取回收站根目录的节点
        List<FileTreeNodeDto> res = fileTreeService.getTrashRootNodes(userId);
        // 返回
        return Result.ok(res);
    }

    /**
     * 获取回收站某文件夹下的子节点（已删除）
     */
    @GetMapping("/trash/{parentId}")
    @LogAnnotation(module = "文件树", operator = "获取回收站子节点")
    public Result<List<FileTreeNodeDto>> getTrashChildrenNodes(@PathVariable Long parentId)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取
        List<FileTreeNodeDto> res = fileTreeService.getTrashChildrenNodes(userId, parentId);
        return Result.ok(res);
    }

    /**
     * 搜索节点
     *
     * @param keyword 关键词
     * @return List<FileTreeNodeDto>
     */
    @GetMapping("/search")
    @LogAnnotation(module = "文件树", operator = "搜索节点")
    public Result<List<FileTreeNodeDto>> searchNodes(@RequestParam String keyword)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 搜索节点
        List<FileTreeNodeDto> res = fileTreeService.searchNodes(userId, keyword);
        // 返回
        return Result.ok(res);
    }
}
