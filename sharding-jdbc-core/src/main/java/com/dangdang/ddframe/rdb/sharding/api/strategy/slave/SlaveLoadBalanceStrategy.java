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

package com.dangdang.ddframe.rdb.sharding.api.strategy.slave;

import javax.sql.DataSource;
import java.util.List;

/**
 * 从库负载均衡策略.
 *
 * @author zhangliang
 */
public interface SlaveLoadBalanceStrategy {
    
    /**
     * 根据负载均衡策略获取从库数据源.
     * 
     * @param name 读写分离数据源名称
     * @param slaveDataSources 从库数据源列表
     * @return 选中的从库数据源
     */
    DataSource getDataSource(String name, List<DataSource> slaveDataSources);
}
