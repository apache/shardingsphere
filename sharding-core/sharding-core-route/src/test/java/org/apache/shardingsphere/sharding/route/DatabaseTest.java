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

package org.apache.shardingsphere.sharding.route;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouteDecorator;
import org.apache.shardingsphere.sharding.route.fixture.HintShardingAlgorithmFixture;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseTest {
    
    private ShardingRule shardingRule;

    private ConfigurationProperties properties = new ConfigurationProperties(new Properties());
    
    @Before
    public void setRouteRuleContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        shardingRuleConfig.setDefaultDataSourceName("ds_0");
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        shardingRule = new ShardingRule(shardingRuleConfig, dataSourceMap.keySet());
    }
    
    @Test
    public void assertDatabaseAllRoutingSQL() {
        String originSQL = "select * from tesT";
        SQLParserEngine sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine("MySQL");
        ShardingSphereMetaData metaData = getMetaDataForAllRoutingSQL();
        RouteContext routeContext = new DataNodeRouter(metaData, properties, sqlParserEngine).route(originSQL, Collections.emptyList(), false);
        ShardingRouteDecorator shardingRouteDecorator = new ShardingRouteDecorator();
        RouteContext actual = shardingRouteDecorator.decorate(routeContext, metaData, shardingRule, properties);
        assertThat(actual.getRouteResult().getRouteUnits().size(), is(1));
        Collection<String> actualDataSources = actual.getRouteResult().getActualDataSourceNames();
        assertThat(actualDataSources.size(), is(1));
    }
    
    private ShardingSphereMetaData getMetaDataForAllRoutingSQL() {
        DataSourceMetas dataSourceMetas = mock(DataSourceMetas.class);
        when(dataSourceMetas.getDataSourceMetaData("ds_0")).thenReturn(mock(DataSourceMetaData.class));
        ColumnMetaData idColumnMetaData = new ColumnMetaData("id", Types.INTEGER, "int", true, false, false);
        ColumnMetaData nameColumnMetaData = new ColumnMetaData("user_id", Types.INTEGER, "int", false, false, false);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.get("tesT")).thenReturn(new TableMetaData(Arrays.asList(idColumnMetaData, nameColumnMetaData), Arrays.asList(new IndexMetaData("id"), new IndexMetaData("user_id"))));
        when(schemaMetaData.containsTable("tesT")).thenReturn(true);
        return new ShardingSphereMetaData(dataSourceMetas, schemaMetaData);
    }
    
    @Test
    public void assertDatabaseSelectSQLPagination() {
        String originSQL = "select user_id from tbl_pagination limit 0,5";
        SQLParserEngine sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine("MySQL");
        ShardingSphereMetaData metaData = getMetaDataForPagination();
        RouteContext routeContext = new DataNodeRouter(metaData, properties, sqlParserEngine).route(originSQL, Collections.emptyList(), false);
        ShardingRouteDecorator shardingRouteDecorator = new ShardingRouteDecorator();
        RouteContext actual = shardingRouteDecorator.decorate(routeContext, metaData, shardingRule, properties);
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(0L));
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orElse(null), is(5L));
        originSQL = "select user_id from tbl_pagination limit 5,5";
        routeContext = new DataNodeRouter(metaData, properties, sqlParserEngine).route(originSQL, Collections.emptyList(), false);
        shardingRouteDecorator = new ShardingRouteDecorator();
        actual = shardingRouteDecorator.decorate(routeContext, metaData, shardingRule, properties);
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(5L));
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orElse(null), is(5L));
    }
    
    private ShardingSphereMetaData getMetaDataForPagination() {
        ColumnMetaData idColumnMetaData = new ColumnMetaData("id", Types.INTEGER, "int", true, false, false);
        ColumnMetaData nameColumnMetaData = new ColumnMetaData("user_id", Types.INTEGER, "int", false, false, false);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.get("tbl_pagination")).thenReturn(
                new TableMetaData(Arrays.asList(idColumnMetaData, nameColumnMetaData), Arrays.asList(new IndexMetaData("id"), new IndexMetaData("user_id"))));
        when(schemaMetaData.containsTable("tbl_pagination")).thenReturn(true);
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.getSchema()).thenReturn(schemaMetaData);
        DataSourceMetas dataSourceMetas = mock(DataSourceMetas.class);
        when(dataSourceMetas.getDataSourceMetaData("ds_0")).thenReturn(mock(DataSourceMetaData.class));
        when(result.getDataSources()).thenReturn(dataSourceMetas);
        return result;
    }
    
    @Test
    public void assertDatabasePrepareSelectSQLPagination() {
        String shardingPrefix = "user_db";
        String shardingTable = "t_user";
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration(shardingTable, shardingPrefix + "${1..2}." + shardingTable);
        tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("city_id", shardingPrefix + "${city_id % 2 + 1}"));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        String originSQL = "select city_id from t_user where city_id in (?,?) limit 5,10";
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchema()).thenReturn(mock(SchemaMetaData.class));
        SQLParserEngine sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine("MySQL");
        RouteContext routeContext = new DataNodeRouter(metaData, properties, sqlParserEngine).route(originSQL, Lists.newArrayList(13, 173), false);
        ShardingRouteDecorator shardingRouteDecorator = new ShardingRouteDecorator();
        RouteContext actual = shardingRouteDecorator.decorate(routeContext, metaData, shardingRule, properties);
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(5L));
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orElse(null), is(10L));
        assertThat(actual.getRouteResult().getRouteUnits().size(), is(1));
        originSQL = "select city_id from t_user where city_id in (?,?) limit 5,10";
        routeContext = new DataNodeRouter(metaData, properties, sqlParserEngine).route(originSQL, Lists.newArrayList(89, 84), false);
        shardingRouteDecorator = new ShardingRouteDecorator();
        actual = shardingRouteDecorator.decorate(routeContext, metaData, shardingRule, properties);
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualOffset(), is(5L));
        assertThat(((SelectStatementContext) actual.getSqlStatementContext()).getPaginationContext().getActualRowCount().orElse(null), is(10L));
    }
}
