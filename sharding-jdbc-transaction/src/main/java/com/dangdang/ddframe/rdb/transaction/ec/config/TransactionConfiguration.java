package com.dangdang.ddframe.rdb.transaction.ec.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 事务配置对象.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class TransactionConfiguration {
    
    /**
     * 同步的事务补偿的最大尝试次数.
     */
    private int syncMaxCompensationTryTimes = 3;
    
    /**
     * 异步的事务补偿的最大尝试次数.
     */
    // TODO 使用elastic-job做异步重试
    private int asyncMaxCompensationTryTimes = 3;
}
