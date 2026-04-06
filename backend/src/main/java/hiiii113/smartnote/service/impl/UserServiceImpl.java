package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.LoginDto;
import hiiii113.smartnote.dto.RegisterDto;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.UserMapper;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.PasswordUtil;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService
{
    private static final String brandName = "SmartNote_";
    private static final Random random = new Random();

    // 登录
    @Override
    public void login(LoginDto dto)
    {
        // 手机号和邮箱不能同时为空
        if ((dto.getEmail() == null && dto.getPhone() == null) || (dto.getEmail() != null && dto.getPhone() != null))
        {
            throw new BusinessException("邮箱和手机号必须且只能填写一个", Result.CODE_BAD_REQUEST);
        }

        // 查询用户
        User user;
        if (dto.getPhone() != null)
        {
            user = this.lambdaQuery().eq(User::getPhone, dto.getPhone()).one();
            // 没有查询到或者密码不匹配时
            if (user == null || !PasswordUtil.matches(dto.getPassword(), user.getPassword()))
            {
                throw new BusinessException("手机号或密码错误！", Result.CODE_BAD_REQUEST);
            }
        }
        else
        {
            user = this.lambdaQuery().eq(User::getEmail, dto.getEmail()).one();
            if (user == null || !PasswordUtil.matches(dto.getPassword(), user.getPassword()))
            {
                throw new BusinessException("邮箱或密码错误！", Result.CODE_BAD_REQUEST);
            }
        }
    }

    @Override
    public void register(RegisterDto dto)
    {
        // 手机号和邮箱必须且只能填写一个
        if ((dto.getEmail() == null && dto.getPhone() == null) || (dto.getEmail() != null && dto.getPhone() != null))
        {
            throw new BusinessException("邮箱和手机号必须且只能填写一个", Result.CODE_BAD_REQUEST);
        }

        // 查询手机号或者邮箱是否为空
        User user;
        if (dto.getPhone() != null)
        {
            user = this.lambdaQuery().eq(User::getPhone, dto.getPhone()).one();
            if (user != null)
            {
                throw new BusinessException("手机号已经注册", Result.CODE_BAD_REQUEST);
            }

            // 构造对象
            User newUser = new User();
            newUser.setUsername(generateUsername());
            newUser.setPhone(dto.getPhone());
            newUser.setPassword(PasswordUtil.encode(dto.getPassword()));
            // 保存
            save(newUser);
        }
        else
        {
            user = this.lambdaQuery().eq(User::getEmail, dto.getEmail()).one();
            if (user != null)
            {
                throw new BusinessException("邮箱已经注册", Result.CODE_BAD_REQUEST);
            }

            // 构造对象
            User newUser = new User();
            newUser.setUsername(generateUsername());
            newUser.setEmail(dto.getEmail());
            newUser.setPassword(PasswordUtil.encode(dto.getPassword()));
            // 保存
            save(newUser);
        }
    }

    // 生成不重复的用户名
    private String generateUsername()
    {
        String username;
        int maxAttempts = 10; // 最多尝试10次
        for (int i = 0; i < maxAttempts; i++)
        {
            int randomNum = random.nextInt(100000000); // 0 到 99999999
            username = brandName + String.format("%08d", randomNum);
            // 检查是否重复
            if (this.lambdaQuery().eq(User::getUsername, username).one() == null)
            {
                return username;
            }
        }
        // 如果10次都重复，用时间戳兜底
        return brandName + System.currentTimeMillis();
    }
}
