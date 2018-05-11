/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.integrate.type.ms;

import io.shardingsphere.core.api.MasterSlaveDataSourceFactory;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.common.base.AbstractSQLAssertTest;
import io.shardingsphere.core.common.env.ShardingTestStrategy;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.integrate.jaxb.SQLShardingRule;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.AfterClass;

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

public abstract class AbstractMasterSlaveOnlyTest extends AbstractSQLAssertTest {
    
    private static Map<DatabaseType, MasterSlaveDataSource> masterSlaveDataSources = new HashMap<>();
    
    public AbstractMasterSlaveOnlyTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    protected static List<String> getInitFiles() {
        return Arrays.asList("integrate/dataset/masterslave/init/master_only.xml", "integrate/dataset/masterslave/init/slave_only.xml");
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
        return AbstractMasterSlaveOnlyTest.getInitFiles();
    }
    
    @Override
    protected final Map<DatabaseType, MasterSlaveDataSource> getDataSources() throws SQLException {
        if (!masterSlaveDataSources.isEmpty()) {
            return masterSlaveDataSources;
        }
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            masterSlaveDataSources.put(each.getKey(), getMasterSlaveDataSource(each.getValue()));
        }
        return masterSlaveDataSources;
    }
    
    private MasterSlaveDataSource getMasterSlaveDataSource(final Map<String, DataSource> masterSlaveDataSourceMap) throws SQLException {
        return (MasterSlaveDataSource) MasterSlaveDataSourceFactory.createDataSource(masterSlaveDataSourceMap, 
                new MasterSlaveRuleConfiguration("ds_ms", "dataSource_master_only", Collections.singletonList("dataSource_slave_only")), Collections.<String, Object>emptyMap());
    }
    
    @After
    public final void clearFlag() {
        HintManagerHolder.clear();
        MasterVisitedManager.clear();
    }
    
    @AfterClass
    public static void clear() throws SQLException {
        if (!masterSlaveDataSources.isEmpty()) {
            for (MasterSlaveDataSource each : masterSlaveDataSources.values()) {
                for (DataSource innerEach : each.getAllDataSources().values()) {
                    ((BasicDataSource) innerEach).close();
                }
            }
            masterSlaveDataSources.clear();
        }
    }
}
