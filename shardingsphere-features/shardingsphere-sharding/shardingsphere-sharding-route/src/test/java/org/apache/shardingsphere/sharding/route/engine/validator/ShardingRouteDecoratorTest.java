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

package org.apache.shardingsphere.sharding.route.engine.validator;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouteDecorator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Sharding route decorator.
 */
public final class ShardingRouteDecoratorTest {
    
    @Test
    public void assertDecorateCorrectly() {
        ShardingRouteDecorator shardingRouteDecorator = new ShardingRouteDecorator();
        RouteContext routeContext = mock(RouteContext.class, Answers.RETURNS_DEEP_STUBS);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, withSettings().extraInterfaces(TableAvailable.class));
        TablesContext tablesContext = mock(TablesContext.class);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(((TableAvailable) sqlStatementContext).getAllTables()).thenReturn(Lists.newArrayList(mock(SimpleTableSegment.class)));
        when(tablesContext.getTableNames()).thenReturn(Lists.newArrayList("tableName"));
        when(routeContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, Answers.RETURNS_DEEP_STUBS);
        
//        when(shardingSphereMetaData.getRuleSchemaMetaData()).thenReturn();
        ShardingRule shardingRule = mock(ShardingRule.class, Answers.RETURNS_DEEP_STUBS);
        ConfigurationProperties properties = mock(ConfigurationProperties.class, Answers.RETURNS_DEEP_STUBS);
    
        RouteContext routeContextDecorated = shardingRouteDecorator.decorate(routeContext, shardingSphereMetaData, shardingRule, properties);
        assertFieldOfInstance(routeContextDecorated, "sqlStatementContext", is(routeContext));
    }
    
    @SneakyThrows
    private <S, T> void assertFieldOfInstance(final S classInstance, final String fieldName, final Matcher<T> matcher) {
        Field field = classInstance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        T value = (T) field.get(classInstance);
        assertThat(value, matcher);
    }
}
