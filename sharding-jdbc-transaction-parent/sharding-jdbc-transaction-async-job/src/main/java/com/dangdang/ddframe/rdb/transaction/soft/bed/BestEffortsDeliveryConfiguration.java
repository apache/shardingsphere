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

package com.dangdang.ddframe.rdb.transaction.soft.bed;

import com.dangdang.ddframe.rdb.transaction.soft.config.AsyncSoftTransactionZookeeperConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.config.AsyncSoftTransactionJobConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 最大努力送达型异步作业配置对象.
 *
 * @author caohao
 */
@Getter
@Setter
public class BestEffortsDeliveryConfiguration {
    
    /**
     * 事务管理器管理的数据源.
     */
    private Map<String, DataSource> targetDataSource;
    
    /**
     * 存储事务日志的数据源.
     */
    private Map<String, DataSource> transactionLogDataSource;
    
    /**
     * 注册中心配置对象.
     */
    private AsyncSoftTransactionZookeeperConfiguration zkConfig;
    
    /**
     * 作业配置对象.
     */
    private AsyncSoftTransactionJobConfiguration jobConfig;
    
    public DataSource getTargetDataSource(final String dataSourceName) {
        return targetDataSource.get(dataSourceName);
    }
    
    public DataSource getDefaultTransactionLogDataSource() {
        return transactionLogDataSource.values().iterator().next();
    }
}
