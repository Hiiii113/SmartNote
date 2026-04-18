package hiiii113.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hiiii113.smartnote.dto.*;
import hiiii113.smartnote.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户 service
 */
public interface UserService extends IService<User>
{
    // 登录
    void login(LoginDto dto);

    // 注册
    void register(RegisterDto dto);

    // 获取用户信息
    User getUserInfo(Long userId);

    // 修改用户名
    void updateUsername(Long userId, UpdateUsernameDto dto);

    // 修改座右铭
    void updateMotto(Long userId, UpdateMottoDto dto);

    // 修改密码
    void updatePassword(Long userId, UpdatePasswordDto dto);

    // 修改头像
    void updateAvatar(MultipartFile file, Long userId);

    // 根据 account （手机号或邮箱）查找 User 对象
    User getUserByAccount(String account);
}
