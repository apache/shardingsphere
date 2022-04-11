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

package org.apache.shardingsphere.sharding.route.engine.type.standard;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractSQLRouteTest extends AbstractRoutingEngineTest {
    
    protected final RouteContext assertRoute(final String sql, final List<Object> parameters) {
        return assertRoute(sql, parameters, 1);
    }
    
    protected final RouteContext assertRoute(final String sql, final List<Object> parameters, final int routeUnitSize) {
        ShardingRule shardingRule = createAllShardingRule();
        SingleTableRule singleTableRule = createAllSingleTableRule(Collections.singletonList(shardingRule));
        Map<String, ShardingSphereSchema> schemas = buildSchemas();
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine("MySQL", 
                new CacheOption(2000, 65535L, 4), new CacheOption(128, 1024L, 4), false);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Arrays.asList(shardingRule, singleTableRule));
        ShardingSphereResource resource = mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("sharding_db", resource, ruleMetaData, schemas);
        Map<String, ShardingSphereMetaData> metaDataMap = Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataMap, parameters, sqlStatementParserEngine.parse(sql, false), DefaultSchema.LOGIC_NAME);
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, sql, parameters);
        return new SQLRouteEngine(Arrays.asList(shardingRule, singleTableRule), props).route(logicSQL, metaData);
    }
    
    private Map<String, ShardingSphereSchema> buildSchemas() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order", new TableMetaData("t_order", Arrays.asList(new ColumnMetaData("order_id", Types.INTEGER, true, false, false),
                new ColumnMetaData("user_id", Types.INTEGER, false, false, false),
                new ColumnMetaData("status", Types.INTEGER, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tableMetaDataMap.put("t_order_item", new TableMetaData("t_order_item", Arrays.asList(new ColumnMetaData("item_id", Types.INTEGER, true, false, false),
                new ColumnMetaData("order_id", Types.INTEGER, false, false, false),
                new ColumnMetaData("user_id", Types.INTEGER, false, false, false),
                new ColumnMetaData("status", Types.VARCHAR, false, false, false),
                new ColumnMetaData("c_date", Types.TIMESTAMP, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tableMetaDataMap.put("t_other", new TableMetaData("t_other", Collections.singletonList(
                new ColumnMetaData("order_id", Types.INTEGER, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tableMetaDataMap.put("t_category", new TableMetaData("t_category", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        return Collections.singletonMap("sharding_db", new ShardingSphereSchema(tableMetaDataMap));
    }
}
