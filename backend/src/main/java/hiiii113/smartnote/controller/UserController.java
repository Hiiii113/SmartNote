package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.*;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.log.LogAnnotation;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController
{
    // 业务层
    private final UserService userService;

    /**
     * 登录
     *
     * @param dto 相关数据
     * @return String 类型的 Token
     */
    @PostMapping("/login")
    @LogAnnotation(module = "用户", operator = "登录")
    public Result<String> login(@Valid @RequestBody LoginDto dto)
    {
        // 调用 service 层代码
        userService.login(dto);
        // 获取 Token
        String token = StpUtil.getTokenValue();
        // 返回信息和 Token
        return Result.ok("登录成功", token);
    }

    /**
     * 注册
     *
     * @param dto 相关数据
     */
    @PostMapping
    @LogAnnotation(module = "用户", operator = "注册")
    public Result<Void> register(@Valid @RequestBody RegisterDto dto)
    {
        // 注册
        userService.register(dto);
        return Result.created("注册成功");
    }

    /**
     * 登出
     */
    @GetMapping("/logout")
    @LogAnnotation(module = "用户", operator = "登出")
    public Result<Void> logout()
    {
        // 清除 Token
        StpUtil.logout();
        // 返回退出登录成功
        return Result.ok("退出登录成功");
    }

    /**
     * 获取用户信息
     *
     * @return User
     */
    @GetMapping("/info")
    @LogAnnotation(module = "用户", operator = "获取用户信息")
    public Result<User> getUserInfo()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取信息
        User user = userService.getUserInfo(userId);
        return Result.ok(user);
    }

    /**
     * 修改用户名
     *
     * @param dto 相关数据
     */
    @PutMapping("/username")
    @LogAnnotation(module = "用户", operator = "修改用户名")
    public Result<Void> updateUsername(@Valid @RequestBody UpdateUsernameDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改用户名
        userService.updateUsername(userId, dto);
        return Result.ok("用户名修改成功");
    }

    /**
     * 修改座右铭
     *
     * @param dto 相关数据
     */
    @PutMapping("/motto")
    @LogAnnotation(module = "用户", operator = "修改座右铭")
    public Result<Void> updateMotto(@Valid @RequestBody UpdateMottoDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改座右铭
        userService.updateMotto(userId, dto);
        return Result.ok("座右铭修改成功");
    }

    /**
     * 修改密码
     *
     * @param dto 相关数据
     */
    @PutMapping("/password")
    @LogAnnotation(module = "用户", operator = "修改密码")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改密码
        userService.updatePassword(userId, dto);
        return Result.ok("密码修改成功");
    }

    /**
     * 修改头像
     *
     * @param file 头像文件
     */
    @PostMapping("/avatar")
    @LogAnnotation(module = "用户", operator = "修改头像")
    public Result<Void> updateAvatar(@RequestParam("file") MultipartFile file)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 保存头像
        userService.updateAvatar(file, userId);
        return Result.ok("头像修改成功");
    }
}
