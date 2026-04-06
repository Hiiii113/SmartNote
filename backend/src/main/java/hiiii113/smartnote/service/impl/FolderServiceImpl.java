package hiiii113.smartnote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hiiii113.smartnote.entity.Folder;
import hiiii113.smartnote.mapper.FolderMapper;
import hiiii113.smartnote.service.FolderService;
import org.springframework.stereotype.Service;

@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService
{
}
