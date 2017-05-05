/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.api.config;

import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.transaction.soft.constants.TransactionLogDataSourceType;
import com.dangdang.ddframe.rdb.transaction.soft.datasource.TransactionLogDataSource;
import com.dangdang.ddframe.rdb.transaction.soft.datasource.impl.MemoryTransactionLogDataSource;
import com.dangdang.ddframe.rdb.transaction.soft.datasource.impl.RdbTransactionLogDataSource;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.dangdang.ddframe.rdb.transaction.soft.constants.TransactionLogDataSourceType.RDB;

/**
 * 柔性事务配置对象.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@Setter
public class SoftTransactionConfiguration {
    
    /**
     * 事务管理器管理的数据源.
     */
    @Getter(AccessLevel.NONE)
    private final DataSource targetDataSource;
    
    /**
     * 同步的事务送达的最大尝试次数.
     */
    private int syncMaxDeliveryTryTimes = 3;
    
    /**
     * 事务日志存储类型.
     */
    private TransactionLogDataSourceType storageType = RDB;
    
    /**
     * 存储事务日志的数据源.
     */
    private DataSource transactionLogDataSource;
    
    /**
     * 内嵌的最大努力送达型异步作业配置对象.
     */
    private Optional<NestedBestEffortsDeliveryJobConfiguration> bestEffortsDeliveryJobConfiguration = Optional.absent();
    
    /**
     * 获取事务管理器管理的数据库连接.
     * 
     * @param dataSourceName 数据源名称
     * @return 事务管理器管理的数据库连接
     */
    public Connection getTargetConnection(final String dataSourceName) throws SQLException {
        if (!(targetDataSource instanceof ShardingDataSource)) {
            return targetDataSource.getConnection();
        }
        return ((ShardingDataSource) targetDataSource).getConnection().getConnection(dataSourceName, SQLType.SELECT);
    }

    /**
     * 构建事务日志事务源.
     *
     * @return 存储事务日志的数据源
     */
    public TransactionLogDataSource buildTransactionLogDataSource() {
        TransactionLogDataSource result;
        switch (storageType) {
            case MEMORY:
                result = new MemoryTransactionLogDataSource();
                break;
            case RDB:
                result = new RdbTransactionLogDataSource(transactionLogDataSource);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return result;
    }
}
