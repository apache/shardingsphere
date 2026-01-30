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

package org.apache.shardingsphere.sharding.route.engine.type.standard.assertion;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sharding.route.engine.fixture.ShardingRouteEngineFixtureBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Sharding route assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteAssert {
    
    /**
     * Assert route.
     *
     * @param sql SQL
     * @param params parameters
     * @return route context
     */
    public static RouteContext assertRoute(final String sql, final List<Object> params) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingRule shardingRule = ShardingRouteEngineFixtureBuilder.createAllShardingRule();
        SingleRule singleRule = ShardingRouteEngineFixtureBuilder.createSingleRule(Collections.singleton(shardingRule));
        TimestampServiceRule timestampServiceRule = ShardingRouteEngineFixtureBuilder.createTimeServiceRule();
        SQLParserRule sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(new CacheOption(2000, 65535L), new CacheOption(128, 1024L)));
        SQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(databaseType);
        ShardingSphereRule broadcastRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class);
        when(ruleAttribute.getDistributedTableNames()).thenReturn(Collections.singleton("t_product"));
        when(broadcastRule.getAttributes().findAttribute(TableMapperRuleAttribute.class)).thenReturn(Optional.of(ruleAttribute));
        RuleMetaData ruleMetaData = new RuleMetaData(Arrays.asList(shardingRule, broadcastRule, singleRule, timestampServiceRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, buildSchemas());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, "foo_db", new HintValueContext()).bind(sqlParserEngine.parse(sql, false));
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        connectionContext.setCurrentDatabaseName("foo_db");
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, params, new HintValueContext(), connectionContext, metaData);
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        return new SQLRouteEngine(Arrays.asList(shardingRule, singleRule), props).route(queryContext, mock(RuleMetaData.class), database);
    }
    
    private static Collection<ShardingSphereSchema> buildSchemas() {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("t_order", Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("product_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order_item", Arrays.asList(new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("product_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("c_date", Types.TIMESTAMP, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_other", Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_category", Collections.singleton(new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_product", Collections.singleton(new ShardingSphereColumn("product_id", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_user", Collections.singleton(new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_hint_test", Collections.singleton(new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        return Collections.singleton(new ShardingSphereSchema("foo_db", mock(DatabaseType.class), tables, Collections.emptyList()));
    }
}
