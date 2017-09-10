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

import com.dangdang.ddframe.rdb.sharding.rule.MasterSlaveRule;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;

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
     * @param masterSlaveRule master-slave rule
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final MasterSlaveRule masterSlaveRule) throws SQLException {
        return new MasterSlaveDataSource(masterSlaveRule);
    }
}
