package com.recharge.mapper;

import com.recharge.domain.Lock;
import org.apache.ibatis.annotations.Param;

public interface LockMapper {

    public int updateLock(@Param("lock") Lock lock);
}
