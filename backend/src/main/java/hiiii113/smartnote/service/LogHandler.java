package hiiii113.smartnote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import hiiii113.smartnote.entity.Log;

/**
 * 用于保存 log 实体
 */
public interface LogHandler
{
    // 保存log实体
    void saveOperationLog(Log log) throws JsonProcessingException;
}
