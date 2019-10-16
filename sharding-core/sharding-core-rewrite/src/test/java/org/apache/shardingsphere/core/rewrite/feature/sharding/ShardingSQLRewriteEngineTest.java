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

package org.apache.shardingsphere.core.rewrite.feature.sharding;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.core.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.rewrite.feature.sharding.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.rewrite.feature.sharding.engine.ShardingSQLRewriteEngine;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingSQLRewriteEngineTest {
    
    private ShardingRule shardingRule;
    
    private RoutingUnit routingUnit;
    
    private Map<String, String> logicAndActualTables = Collections.singletonMap("table_x", "table_1");
    
    private SQLParseEngine parseEngine;
    
    @Before
    public void setUp() throws IOException {
        shardingRule = createShardingRule();
        routingUnit = createRoutingUnit();
        parseEngine = new SQLParseEngine(DatabaseTypes.getActualDatabaseType("MySQL"));
    }
    
    private ShardingRule createShardingRule() throws IOException {
        URL url = ShardingSQLRewriteEngineTest.class.getClassLoader().getResource("yaml/sharding-rewrite-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        return new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
    }
    
    private RoutingUnit createRoutingUnit() {
        RoutingUnit result = new RoutingUnit("db0");
        result.getTableUnits().add(new TableUnit("table_x", "table_1"));
        result.getTableUnits().add(new TableUnit("table_y", "table_y"));
        result.getTableUnits().add(new TableUnit("table_w", "table_1"));
        return result;
    }
    
    @Test
    public void assertRewriteInsertWithGeneratedKeyAndQueryAssistedShardingEncryptor() {
        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("INSERT INTO `table_w` set cipher = 'encrypt_10', query = 'assisted_query_10', plain = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    private SQLRewriteResult getSQLRewriteResult(final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        ShardingRouter shardingRouter = new ShardingRouter(shardingRule, mock(ShardingSphereMetaData.class), parseEngine);
        SQLStatement sqlStatement = shardingRouter.parse(sql, false);
        SQLRouteResult sqlRouteResult = shardingRouter.route(sql, parameters, sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(TableMetas.class), sqlRouteResult.getSqlStatementContext(), sql, parameters);
        new ShardingSQLRewriteContextDecorator(shardingRule, sqlRouteResult).decorate(sqlRewriteContext);
        new EncryptSQLRewriteContextDecorator(shardingRule.getEncryptRule(), isQueryWithCipherColumn).decorate(sqlRewriteContext);
        sqlRewriteContext.generateSQLTokens();
        return new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext);
    }
}
