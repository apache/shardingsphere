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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
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
        ShardingRule shardingRule = createAllShardingRule();
        SingleTableRule singleTableRule = createSingleTableRule(Collections.singletonList(shardingRule));
        Map<String, ShardingSphereSchema> schemas = buildSchemas();
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine("MySQL",
                new CacheOption(2000, 65535L, false), new CacheOption(128, 1024L, false), false);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Arrays.asList(shardingRule, singleTableRule));
        ShardingSphereResource resource = mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, DatabaseTypeFactory.getInstance("MySQL"), resource, ruleMetaData, schemas);
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(databases, parameters, sqlStatementParserEngine.parse(sql, false), DefaultDatabase.LOGIC_NAME);
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, parameters);
        return new SQLRouteEngine(Arrays.asList(shardingRule, singleTableRule), props).route(new ConnectionContext(), queryContext, database);
    }
    
    private Map<String, ShardingSphereSchema> buildSchemas() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(3, 1);
        tables.put("t_order", new ShardingSphereTable("t_order", Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_item", new ShardingSphereTable("t_order_item", Arrays.asList(new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true),
                new ShardingSphereColumn("c_date", Types.TIMESTAMP, false, false, false, true)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_other", new ShardingSphereTable("t_other", Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_category", new ShardingSphereTable("t_category", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        return Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new ShardingSphereSchema(tables, Collections.emptyMap()));
    }
}
