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

package io.shardingjdbc.example.jdbc;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.example.jdbc.repository.RawJdbcRepository;
import io.shardingjdbc.example.jdbc.util.DataSourceUtil;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class RawJdbcJavaMasterSlaveOnlyMain {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws SQLException {
    // CHECKSTYLE:ON
        new RawJdbcRepository(getMasterSlaveDataSource()).testAll();
    }
    
    private static MasterSlaveDataSource getMasterSlaveDataSource() throws SQLException {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig.setName("ds_jdbc_master_slave");
        masterSlaveRuleConfig.setMasterDataSourceName("ds_jdbc_master");
        masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("ds_jdbc_slave_0", "ds_jdbc_slave_1"));
        return new MasterSlaveDataSource(masterSlaveRuleConfig.build(createDataSourceMap()));
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        final Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("ds_jdbc_master", DataSourceUtil.createDataSource("ds_jdbc_master"));
        result.put("ds_jdbc_slave_0", DataSourceUtil.createDataSource("ds_jdbc_slave_0"));
        result.put("ds_jdbc_slave_1", DataSourceUtil.createDataSource("ds_jdbc_slave_1"));
        return result;
    }
}
