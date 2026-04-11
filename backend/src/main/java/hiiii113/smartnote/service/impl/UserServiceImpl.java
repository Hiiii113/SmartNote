package hiiii113.smartnote.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.dto.*;
import hiiii113.smartnote.entity.User;
import hiiii113.smartnote.exception.BusinessException;
import hiiii113.smartnote.mapper.UserMapper;
import hiiii113.smartnote.service.UserService;
import hiiii113.smartnote.utils.PasswordUtil;
import hiiii113.smartnote.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
        }
        else
        {
            user = this.lambdaQuery().eq(User::getEmail, dto.getEmail()).one();
        }

        // 判断并登录
        if (user == null)
        {
            throw new BusinessException("该账户未注册！", Result.CODE_BAD_REQUEST);
        }
        else if (!PasswordUtil.matches(dto.getPassword(), user.getPassword()))
        {
            throw new BusinessException("用户名或密码错误！", Result.CODE_BAD_REQUEST);
        }
        else
        {
            StpUtil.login(user.getId());
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

    @Override
    public User getUserInfo(Long userId)
    {
        User user = getUserById(userId);
        // 清除敏感信息
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUsername(Long userId, UpdateUsernameDto dto)
    {
        User user = getUserById(userId);

        // 检查用户名是否已被使用
        User existingUser = this.lambdaQuery().eq(User::getUsername, dto.getUsername()).one();
        if (existingUser != null && !existingUser.getId().equals(userId))
        {
            throw new BusinessException("用户名已被使用", Result.CODE_BAD_REQUEST);
        }

        // 更新用户名
        user.setUsername(dto.getUsername());
        this.updateById(user);
    }

    @Override
    public void updateMotto(Long userId, UpdateMottoDto dto)
    {
        User user = getUserById(userId);
        // 更新座右铭
        user.setMotto(dto.getMotto());
        this.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDto dto)
    {
        User user = getUserById(userId);

        // 验证旧密码
        if (!PasswordUtil.matches(dto.getOldPassword(), user.getPassword()))
        {
            throw new BusinessException("旧密码错误", Result.CODE_BAD_REQUEST);
        }

        // 更新密码
        user.setPassword(PasswordUtil.encode(dto.getNewPassword()));
        this.updateById(user);
    }

    @Override
    public void updateAvatar(MultipartFile file, Long userId)
    {
        // 判空
        if (userId == null || file.isEmpty())
        {
            throw new BusinessException("用户ID和图片不能为空！", Result.CODE_BAD_REQUEST);
        }

        User user = getUserById(userId);

        // 获取并校验文件后缀
        String ext = validateAndGetImageExtension(file.getOriginalFilename());

        // 生成最终的文件名（当前时间 + 后缀）
        String fileName = System.currentTimeMillis() + ext;

        // 保存到本地目录（根目录下的 /upload）
        String path = System.getProperty("user.dir") + "/upload/";
        //noinspection ResultOfMethodCallIgnored 忽略警告
        new File(path).mkdirs();
        try
        {
            file.transferTo(new File(path + fileName));
        }
        catch (IOException e)
        {
            throw new BusinessException("图片保存失败！", Result.CODE_ERROR);
        }
        String imageUrl = "upload/" + fileName;

        // 更新数据库
        user.setAvatar(imageUrl);
        this.updateById(user);
    }

    @Override
    public User getUserByAccount(String account)
    {
        // 获取 User

        return lambdaQuery()
                .eq(User::getPhone, account)
                .or()
                .eq(User::getEmail, account)
                .one();
    }

    // 根据ID获取用户（自动抛出异常）
    private User getUserById(Long userId)
    {
        User user = this.getById(userId);
        if (user == null)
        {
            throw new BusinessException("用户不存在", Result.CODE_NOT_FOUND);
        }
        return user;
    }

    // 获取并校验图片后缀
    private String validateAndGetImageExtension(String filename)
    {
        if (filename == null || !filename.contains("."))
        {
            throw new BusinessException("文件名不合法！", Result.CODE_BAD_REQUEST);
        }
        String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)$"))
        {
            throw new BusinessException("只支持 jpg/jpeg/png/gif/webp/bmp 格式的图片！", Result.CODE_BAD_REQUEST);
        }
        return ext;
    }
}
