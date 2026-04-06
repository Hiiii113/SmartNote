package hiiii113.smartnote.controller;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.dto.LoginDto;
import hiiii113.smartnote.dto.RegisterDto;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
