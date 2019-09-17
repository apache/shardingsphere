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

package org.apache.shardingsphere.core.route.router.sharding;

import com.google.common.base.Optional;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.ddl.ShardingDropIndexOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingConditionOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.route.type.standard.StandardRoutingEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.apache.shardingsphere.spi.database.DataSourceMetaData;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParsingSQLRoutingResultCheckerTest {
    
    @Test
    public void assertCheckWithShardingDropIndexOptimizedStatement() {
        new ParsingSQLRoutingResultChecker(getShardingRule(), getMetaData(), mock(ShardingDropIndexOptimizedStatement.class))
            .check(mock(RoutingEngine.class), getRoutingResult());
    }
    
    @Test
    public void assertCheckOthersRoutingResult() {
        new ParsingSQLRoutingResultChecker(getShardingRule(), getMetaData(), getShardingOptimizedStatement())
            .check(mock(RoutingEngine.class), getRoutingResult());
    }

    @Test
    public void assertCheckOthersRoutingResultWithAbsentDatabase() {
        String msg = null;
        try {
            new ParsingSQLRoutingResultChecker(getShardingRule(), getMetaData(), getShardingOptimizedStatement())
                .check(mock(RoutingEngine.class), getRoutingResultWithAbsentDatabase());
        } catch (ShardingException ex) {
            msg = ex.getMessage();
        }
        assertThat(msg, is("We get some absent DataNodes=[db_2.t_order_0] in routing result, please check the configuration of rule and data node."));
    }
    
    @Test
    public void assertCheckStandardRoutingResult() {
        new ParsingSQLRoutingResultChecker(getShardingRule(), getMetaData(), getShardingOptimizedStatement())
            .check(mock(StandardRoutingEngine.class), getRoutingResult());
    }
    
    @Test
    public void assertCheckStandardRoutingResultWithAbsentDatabaseAndDefaultStrategy() {
        String msg = null;
        try {
            new ParsingSQLRoutingResultChecker(getShardingRuleWithDefaultStrategy(), getMetaData(), getShardingOptimizedStatement())
                .check(mock(StandardRoutingEngine.class), getRoutingResultWithAbsentDatabase());
        } catch (ShardingException ex) {
            msg = ex.getMessage();
        }
        assertThat(msg, is("We get some absent DataNodes=[db_2.t_order_0] in routing result, DatabaseStrategy=[Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 3}'}], TableStrategy=[Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 3}'}], please check the configuration of rule and data node."));
    }
    
    private ShardingRule getShardingRuleWithDefaultStrategy() {
        ShardingRule result = mock(ShardingRule.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                if ("t_order".equals(invocation.getArgument(0))) {
                    return Optional.of(getTableRule());
                } else {
                    return Optional.absent();
                }
            }
        }).when(result).findTableRule(anyString());
        doReturn(getRuleConfigurationWithDefaultStrategy()).when(result).getRuleConfiguration();
        return result;
    }
    
    private ShardingRuleConfiguration getRuleConfigurationWithDefaultStrategy() {
        ShardingRuleConfiguration result = mock(ShardingRuleConfiguration.class);
        doReturn(getDefaultShardingStrategyConfiguration()).when(result).getDefaultDatabaseShardingStrategyConfig();
        doReturn(getDefaultShardingStrategyConfiguration()).when(result).getDefaultTableShardingStrategyConfig();
        when(result.getMasterSlaveRuleConfigs()).thenReturn(Collections.<MasterSlaveRuleConfiguration>emptyList());
        return result;
    }
    
    @Test
    public void assertCheckStandardRoutingResultWithAbsentDatabase() {
        String msg = null;
        try {
            new ParsingSQLRoutingResultChecker(getShardingRule(), getMetaData(), getShardingOptimizedStatement())
                .check(mock(StandardRoutingEngine.class), getRoutingResultWithAbsentDatabase());
        } catch (ShardingException ex) {
            msg = ex.getMessage();
        }
        assertThat(msg, is("We get some absent DataNodes=[db_2.t_order_0] in routing result, DatabaseStrategy=[Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 2}'}], TableStrategy=[Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 2}'}], please check the configuration of rule and data node."));
    }
    
    private ShardingOptimizedStatement getShardingOptimizedStatement() {
        ShardingOptimizedStatement result = mock(ShardingOptimizedStatement.class);
        doReturn(mock(DMLStatement.class)).when(result).getSQLStatement();
        return result;
    }
    
    @Test
    public void assertCheckStandardRoutingResultWithAbsentDatabaseAndRouteValues() {
        String msg = null;
        try {
            new ParsingSQLRoutingResultChecker(getShardingRule(), getMetaData(), getShardingConditionOptimizedStatement())
                .check(mock(StandardRoutingEngine.class), getRoutingResultWithAbsentDatabase());
        } catch (ShardingException ex) {
            msg = ex.getMessage();
        }
        assertThat(msg, is("We get some absent DataNodes=[db_2.t_order_0] in routing result, DatabaseStrategy=[Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 2}'}], TableStrategy=[Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 2}'}], with [t_order_1.order_id = 1], please check the configuration of rule and data node."));
    }
    
    private ShardingOptimizedStatement getShardingConditionOptimizedStatement() {
        ShardingConditionOptimizedStatement result = mock(ShardingConditionOptimizedStatement.class);
        doReturn(getShardingConditions()).when(result).getShardingConditions();
        doReturn(mock(DMLStatement.class)).when(result).getSQLStatement();
        return result;
    }
    
    private ShardingConditions getShardingConditions() {
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getRouteValues().add(getListRouteValue());
        ShardingConditions result = mock(ShardingConditions.class);
        when(result.getConditions()).thenReturn(Collections.singletonList(shardingCondition));
        return result;
    }
    
    private RouteValue getListRouteValue() {
        return new ListRouteValue<>("order_id", "t_order_1", Collections.singleton(1));
    }
    
    private ShardingRule getShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                if ("t_order".equals(invocation.getArgument(0))) {
                    return Optional.of(getTableRule());
                } else {
                    return Optional.absent();
                }
            }
        }).when(result).findTableRule(anyString());
        doReturn(getRuleConfiguration()).when(result).getRuleConfiguration();
        return result;
    }
    
    private TableRule getTableRule() {
        TableRule result = mock(TableRule.class);
        when(result.getActualTableNames("db_0")).thenReturn(Arrays.asList("t_order_0", "t_order_1"));
        when(result.getActualTableNames("db_1")).thenReturn(Arrays.asList("t_order_0", "t_order_1"));
        return result;
    }
    
    private ShardingRuleConfiguration getRuleConfiguration() {
        ShardingRuleConfiguration result = mock(ShardingRuleConfiguration.class);
        doReturn(getDefaultShardingStrategyConfiguration()).when(result).getDefaultDatabaseShardingStrategyConfig();
        doReturn(getDefaultShardingStrategyConfiguration()).when(result).getDefaultTableShardingStrategyConfig();
        doReturn(Collections.singleton(getTableRuleConfiguration())).when(result).getTableRuleConfigs();
        when(result.getMasterSlaveRuleConfigs()).thenReturn(Collections.<MasterSlaveRuleConfiguration>emptyList());
        return result;
    }
    
    private ShardingStrategyConfiguration getDefaultShardingStrategyConfiguration() {
        ShardingStrategyConfiguration result = mock(InlineShardingStrategyConfiguration.class);
        when(result.toString()).thenReturn("Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 3}'}");
        return result;
    }
    
    private TableRuleConfiguration getTableRuleConfiguration() {
        TableRuleConfiguration result = mock(TableRuleConfiguration.class);
        when(result.getLogicTable()).thenReturn("t_order");
        doReturn(getShardingStrategyConfiguration()).when(result).getDatabaseShardingStrategyConfig();
        doReturn(getShardingStrategyConfiguration()).when(result).getTableShardingStrategyConfig();
        return result;
    }
    
    private ShardingStrategyConfiguration getShardingStrategyConfiguration() {
        ShardingStrategyConfiguration result = mock(InlineShardingStrategyConfiguration.class);
        when(result.toString()).thenReturn("Inline{shardingColumn='order_id', algorithmExpression='t_order_${order_id % 2}'}");
        return result;
    }
    
    private RoutingResult getRoutingResultWithAbsentDatabase() {
        RoutingResult result = mock(RoutingResult.class);
        doReturn(getRoutingUnitsWithAbsentDatabase()).when(result).getRoutingUnits();
        return result;
    }
    
    private Collection<RoutingUnit> getRoutingUnitsWithAbsentDatabase() {
        RoutingUnit routingUnit = mock(RoutingUnit.class);
        doReturn("db_2").when(routingUnit).getDataSourceName();
        doReturn(Collections.singletonList(getTableUnit())).when(routingUnit).getTableUnits();
        return Collections.singleton(routingUnit);
    }
    
    private ShardingSphereMetaData getMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        doReturn(getDataSourceMetas()).when(result).getDataSources();
        doReturn(getTableMetas()).when(result).getTables();
        return result;
    }
    
    private TableMetas getTableMetas() {
        TableMetas tables = mock(TableMetas.class);
        when(tables.containsTable("t_order")).thenReturn(true);
        when(tables.containsTable("t_order_0")).thenReturn(true);
        when(tables.containsTable("t_order_1")).thenReturn(true);
        return tables;
    }
    
    private DataSourceMetas getDataSourceMetas() {
        DataSourceMetas dataSourceMetas = mock(DataSourceMetas.class);
        DataSourceMetaData dataSourceMetaData0 = mock(DataSourceMetaData.class);
        doReturn(dataSourceMetaData0).when(dataSourceMetas).getDataSourceMetaData("db_0");
        return dataSourceMetas;
    }
    
    private RoutingResult getRoutingResult() {
        RoutingResult result = mock(RoutingResult.class);
        doReturn(getRoutingUnits()).when(result).getRoutingUnits();
        return result;
    }
    
    private Collection<RoutingUnit> getRoutingUnits() {
        RoutingUnit routingUnit = mock(RoutingUnit.class);
        doReturn("db_0").when(routingUnit).getDataSourceName();
        doReturn(Collections.singletonList(getTableUnit())).when(routingUnit).getTableUnits();
        return Collections.singleton(routingUnit);
    }
    
    private TableUnit getTableUnit() {
        TableUnit result = mock(TableUnit.class);
        when(result.getActualTableName()).thenReturn("t_order_0");
        when(result.getLogicTableName()).thenReturn("t_order");
        return result;
    }
}
