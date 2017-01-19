package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;

import java.util.Collection;
import java.util.List;

/**
 * SQL上下文.
 *
 * @author zhangliang
 */
public interface SQLContext {
    
    /**
     * 获取表解析对象.
     * 
     * @return 表解析对象
     */
    Table getTable();
    
    /**
     * 获取条件对象上下文集合.
     * 
     * @return 条件对象上下文集合
     */
    Collection<ConditionContext> getConditionContexts();
    
    /**
     * 获取SQL语言标记对象集合.
     * 
     * @return SQL语言标记对象集合
     */
    List<SQLToken> getSqlTokens();
    
    /**
     * 生成SQL构建器.
     *
     * @return SQL构建器
     */
    SQLBuilder toSqlBuilder();
}
