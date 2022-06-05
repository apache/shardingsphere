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

package org.apache.shardingsphere.singletable.route;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SingleTableSQLRouterTest {
    
    @Test
    public void assertCreateRouteContextWithSingleDataSource() {
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class),
                createSingleDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        singleTableRule.getSingleTableDataNodes().put("t_order", Collections.singletonList(mockDataNode("ds_0")));
        ShardingSphereDatabase database = mockSingleDataSourceMetaData();
        RouteContext actual = new SingleTableSQLRouter().createRouteContext(createLogicSQL(), database, singleTableRule, new ConfigurationProperties(new Properties()));
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(actual.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is("ds_0"));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertTrue(routeUnits.get(0).getTableMappers().isEmpty());
        assertFalse(actual.isFederated());
    }
    
    @Test
    public void assertCreateRouteContextWithReadwriteSplittingDataSource() {
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class),
                createReadwriteSplittingDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        singleTableRule.getSingleTableDataNodes().put("t_order", Collections.singletonList(mockDataNode("write_ds")));
        ShardingSphereDatabase database = mockReadwriteSplittingDataSourceMetaData();
        RouteContext actual = new SingleTableSQLRouter().createRouteContext(createLogicSQL(), database, singleTableRule, new ConfigurationProperties(new Properties()));
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(actual.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is("readwrite_ds"));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("write_ds"));
        assertTrue(routeUnits.get(0).getTableMappers().isEmpty());
        assertFalse(actual.isFederated());
    }
    
    @Test
    public void assertCreateRouteContextWithMultiDataSource() {
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class),
                createMultiDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        singleTableRule.getSingleTableDataNodes().put("t_order", Collections.singletonList(mockDataNode("ds_0")));
        ShardingSphereDatabase database = mockMultiDataSourceMetaData();
        RouteContext actual = new SingleTableSQLRouter().createRouteContext(createLogicSQL(), database, singleTableRule, new ConfigurationProperties(new Properties()));
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(actual.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is("ds_0"));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        RouteMapper tableMapper = routeUnits.get(0).getTableMappers().iterator().next();
        assertThat(tableMapper.getActualName(), is("t_order"));
        assertThat(tableMapper.getLogicName(), is("t_order"));
        assertFalse(actual.isFederated());
    }
    
    private DataNode mockDataNode(final String dataSourceName) {
        DataNode result = new DataNode(dataSourceName, "t_order");
        result.setSchemaName(DefaultDatabase.LOGIC_NAME);
        return result;
    }
    
    private ShardingSphereDatabase mockSingleDataSourceMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        when(result.getResource().getDataSources()).thenReturn(dataSourceMap);
        return result;
    }
    
    private ShardingSphereDatabase mockReadwriteSplittingDataSourceMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("write_ds", mock(DataSource.class, RETURNS_DEEP_STUBS));
        when(result.getResource().getDataSources()).thenReturn(dataSourceMap);
        return result;
    }
    
    private ShardingSphereDatabase mockMultiDataSourceMetaData() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        dataSourceMap.put("ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        when(result.getResource().getDataSources()).thenReturn(dataSourceMap);
        when(result.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        return result;
    }
    
    private LogicSQL createLogicSQL() {
        IdentifierValue identifierValue = new IdentifierValue("t_order");
        TableNameSegment tableNameSegment = new TableNameSegment(1, 2, identifierValue);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(tableNameSegment);
        CreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setTable(simpleTableSegment);
        List<Object> parameters = new LinkedList<>();
        SQLStatementContext<CreateTableStatement> sqlStatementContext = new CreateTableStatementContext(createTableStatement);
        return new LogicSQL(sqlStatementContext, "create table", parameters);
    }
    
    private Map<String, DataSource> createSingleDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    private Map<String, DataSource> createReadwriteSplittingDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("readwrite_ds", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    private Map<String, DataSource> createMultiDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        result.put("ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
}
