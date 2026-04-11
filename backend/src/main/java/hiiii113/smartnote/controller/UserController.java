package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.*;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息的 controller
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController
{
    // 业务层
    private final UserService userService;

    // 登录
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDto dto)
    {
        // 调用 service 层代码
        userService.login(dto);
        // 获取 Token
        String token = StpUtil.getTokenValue();
        // 返回信息和 Token
        return Result.ok("登录成功！", token);
    }

    // 注册
    @PostMapping
    public Result<Void> register(@Valid @RequestBody RegisterDto dto)
    {
        userService.register(dto);
        return Result.created("注册成功！");
    }

    // 登出
    @GetMapping("/logout")
    public Result<Void> logout()
    {
        // 清除 Token
        StpUtil.logout();
        // 返回退出登录成功
        return Result.ok("退出登录成功！");
    }

    // 获取用户信息
    @GetMapping("/info")
    public Result<User> getUserInfo()
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取信息
        User user = userService.getUserInfo(userId);
        return Result.ok(user);
    }

    // 修改用户名
    @PutMapping("/username")
    public Result<Void> updateUsername(@RequestBody UpdateUsernameDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改用户名
        userService.updateUsername(userId, dto);
        return Result.ok("修改成功！");
    }

    // 修改座右铭
    @PutMapping("/motto")
    public Result<Void> updateMotto(@RequestBody UpdateMottoDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改座右铭
        userService.updateMotto(userId, dto);
        return Result.ok("修改成功！");
    }

    // 修改密码
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDto dto)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 修改密码
        userService.updatePassword(userId, dto);
        return Result.ok("密码修改成功！");
    }

    // 修改头像
    @PostMapping("/avatar")
    public Result<Void> updateAvatar(@RequestParam("file") MultipartFile file)
    {
        // 获取用户 id
        Long userId = StpUtil.getLoginIdAsLong();
        // 保存头像
        userService.updateAvatar(file, userId);
        return Result.ok("头像修改成功！");
    }
}
