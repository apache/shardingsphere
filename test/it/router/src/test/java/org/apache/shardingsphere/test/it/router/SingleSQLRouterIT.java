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

package org.apache.shardingsphere.test.it.router;

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
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class SingleSQLRouterIT {
    
    private static final String DATABASE_NAME = "foo_db";
    
    @Test
    void assertRoute() {
        String sql = "SELECT (SELECT MAX(id) FROM t_category b WHERE b.id = ?) FROM t_category a WHERE id = ? ";
        RouteContext actual = route(sql, Arrays.asList(1, 1));
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("ds_0"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        RouteMapper tableMapper = routeUnit.getTableMappers().iterator().next();
        assertThat(tableMapper.getLogicName(), is("t_category"));
        assertThat(tableMapper.getActualName(), is("t_category"));
    }
    
    private static RouteContext route(final String sql, final List<Object> params) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put("ds_0", new MockedDataSource());
        dataSources.put("ds_1", new MockedDataSource());
        SingleRule singleRule = new SingleRule(new SingleRuleConfiguration(), DATABASE_NAME, databaseType, dataSources, Collections.emptyList());
        singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("ds_0", DATABASE_NAME, "t_category");
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DATABASE_NAME, databaseType, new ResourceMetaData(dataSources),
                new RuleMetaData(Collections.singleton(singleRule)), buildSchemas(databaseType), props);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), globalRuleMetaData, props);
        SQLParserEngine parserEngine = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()).getSQLParserEngine(databaseType);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, DATABASE_NAME, new HintValueContext()).bind(parserEngine.parse(sql, false));
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        connectionContext.setCurrentDatabaseName(DATABASE_NAME);
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, params, new HintValueContext(), connectionContext, metaData);
        return new SQLRouteEngine(Collections.singleton(singleRule), props).route(queryContext, globalRuleMetaData, database);
    }
    
    private static Collection<ShardingSphereSchema> buildSchemas(final DatabaseType databaseType) {
        ShardingSphereTable table = new ShardingSphereTable("t_category", Collections.singleton(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        return Collections.singleton(new ShardingSphereSchema(DATABASE_NAME, databaseType, Collections.singleton(table), Collections.emptyList()));
    }
}
