package hiiii113.smartnote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import hiiii113.smartnote.entity.Log;

/**
 * 保存 log 实体 service
 */
public interface LogHandler
{
    // 保存log实体
    void saveOperationLog(Log log) throws JsonProcessingException;
}
