/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.core.check.datasource;

import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AbstractDataSourceCheckerTest {
    
    private DataSource dataSource;
    
    private AbstractDataSourceChecker dataSourceChecker;
    
    private Collection<DataSource> dataSources;
    
    @Before
    public void setUp() {
        dataSourceChecker = new AbstractDataSourceChecker() {
            
            @Override
            protected String getDatabaseType() {
                return "H2";
            }
            
            @Override
            public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
            }
            
            @Override
            public void checkVariable(final Collection<? extends DataSource> dataSources) {
            }
        };
        dataSources = new LinkedList<>();
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(ResourceUtil.mockJobConfig());
        dataSource = new PipelineDataSourceManager().getDataSource(jobContext.getTaskConfig().getDumperConfig().getDataSourceConfig());
        dataSources.add(dataSource);
    }
    
    @Test
    public void assertCheckConnection() {
        dataSourceChecker.checkConnection(dataSources);
    }
    
    @Test(expected = PipelineJobPrepareFailedException.class)
    public void assertCheckConnectionFailed() throws SQLException {
        DataSource mockDatasource = mock(DataSource.class);
        when(mockDatasource.getConnection()).thenThrow(new SQLException("error"));
        dataSourceChecker.checkConnection(Collections.singletonList(mockDatasource));
    }
    
    @Test
    public void assertCheckTargetTable() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
        }
        dataSourceChecker.checkTargetTable(dataSources, Collections.singletonList("t_order"));
    }
    
    @Test(expected = PipelineJobPrepareFailedException.class)
    public void assertCheckTargetTableFailed() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
        dataSourceChecker.checkTargetTable(dataSources, Collections.singletonList("t_order"));
    }
}
