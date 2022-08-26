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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl;

import org.apache.shardingsphere.infra.binder.statement.ddl.PrepareStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.EmptyShardingRouteResultException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingPrepareStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLPrepareStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingPrepareStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private RouteContext routeContext;
    
    @Test(expected = EmptyShardingRouteResultException.class)
    public void assertPostValidatePrepareWithEmptyRouteResultForPostgreSQL() {
        PrepareStatement sqlStatement = new PostgreSQLPrepareStatement();
        when(routeContext.getRouteUnits()).thenReturn(Collections.emptyList());
        new ShardingPrepareStatementValidator().postValidate(shardingRule, new PrepareStatementContext(sqlStatement),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test
    public void assertPostValidatePrepareWithDifferentDataSourceForPostgreSQL() {
        PrepareStatement sqlStatement = new PostgreSQLPrepareStatement();
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_0"), new RouteMapper("t_order_item", "t_order_item_0"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        new ShardingPrepareStatementValidator().postValidate(shardingRule, new PrepareStatementContext(sqlStatement),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertPostValidatePrepareWithSameDataSourceForPostgreSQL() {
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_0"), new RouteMapper("t_order_item", "t_order_item_0"))));
        routeUnits.add(new RouteUnit(new RouteMapper("ds_0", "ds_0"),
                Arrays.asList(new RouteMapper("t_order", "t_order_0"), new RouteMapper("t_order_item", "t_order_item_1"))));
        when(routeContext.getRouteUnits()).thenReturn(routeUnits);
        PrepareStatement sqlStatement = new PostgreSQLPrepareStatement();
        new ShardingPrepareStatementValidator().postValidate(shardingRule, new PrepareStatementContext(sqlStatement),
                Collections.emptyList(), database, mock(ConfigurationProperties.class), routeContext);
    }
}
