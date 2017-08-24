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
 * Slave database load-balance strategy.
 *
 * @author zhangliang
 */
public interface SlaveLoadBalanceStrategy {
    
    /**
     * Get data source.
     * 
     * @param name master-slave logic data source name
     * @param slaveDataSources slave data sources's names
     * @return selected slave data source
     */
    DataSource getDataSource(String name, List<DataSource> slaveDataSources);
}
