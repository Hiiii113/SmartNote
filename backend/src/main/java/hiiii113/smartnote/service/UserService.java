package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.LoginDto;
import hiiii113.smartnote.dto.RegisterDto;
import hiiii113.smartnote.entity.User;

public interface UserService extends IService<User>
{
    // 登录
    void login(LoginDto dto);

    // 注册
    void register(RegisterDto dto);
}
