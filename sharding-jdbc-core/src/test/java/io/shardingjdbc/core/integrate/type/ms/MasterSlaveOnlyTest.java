/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.integrate.type.ms;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.common.base.AbstractSQLAssertTest;
import io.shardingjdbc.core.common.env.ShardingTestStrategy;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.hint.HintManagerHolder;
import io.shardingjdbc.core.integrate.jaxb.SQLShardingRule;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import org.junit.After;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MasterSlaveOnlyTest extends AbstractSQLAssertTest {
    
    private static Map<DatabaseType, MasterSlaveDataSource> masterSlaveDataSources = new HashMap<>();
    
    public MasterSlaveOnlyTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    @Override
    protected File getExpectedFile(final String expected) {
        String expectedFile = null == expected ? "integrate/dataset/EmptyTable.xml"
                : String.format("integrate/dataset/masterslave/expect/" + expected, getShardingStrategy());
        URL url = AbstractSQLAssertTest.class.getClassLoader().getResource(expectedFile);
        if (null == url) {
            throw new RuntimeException("Wrong expected file:" + expectedFile);
        }
        return new File(url.getPath());
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.masterslaveonly;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList("integrate/dataset/masterslave/init/master_only.xml", "integrate/dataset/masterslave/init/slave_only.xml");
    }
    
    @Override
    protected final Map<DatabaseType, MasterSlaveDataSource> getDataSources() throws SQLException {
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            masterSlaveDataSources.put(each.getKey(), getMasterSlaveDataSource(each.getValue()));
        }
        return masterSlaveDataSources;
    }
    
    private MasterSlaveDataSource getMasterSlaveDataSource(final Map<String, DataSource> masterSlaveDataSourceMap) throws SQLException {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig.setName("ds_ms");
        masterSlaveRuleConfig.setMasterDataSourceName("dataSource_master_only");
        masterSlaveRuleConfig.setSlaveDataSourceNames(Collections.singletonList("dataSource_slave_only"));
        return (MasterSlaveDataSource)MasterSlaveDataSourceFactory.createDataSource(masterSlaveDataSourceMap, masterSlaveRuleConfig);
    }
    
    @After
    public final void clearFlag() {
        HintManagerHolder.clear();
        MasterSlaveDataSource.resetDMLFlag();
    }
}
