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

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSource;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * Master-slave data source factory.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     * 
     * <p>One master data source can configure multiple slave data source.</p>
     * 
     * @param name data source name
     * @param masterDataSource data source for master
     * @param slaveDataSource data source for slave
     * @param otherSlaveDataSources other data sources for slave
     * @return master-slave data source
     */
    public static DataSource createDataSource(final String name, final DataSource masterDataSource, final DataSource slaveDataSource, final DataSource... otherSlaveDataSources) {
        return new MasterSlaveDataSource(name, masterDataSource, Lists.asList(slaveDataSource, otherSlaveDataSources));
    }
}
