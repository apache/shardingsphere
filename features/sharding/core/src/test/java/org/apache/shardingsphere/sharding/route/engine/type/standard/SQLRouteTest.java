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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.route.engine.type.standard.assertion.ShardingRouteAssert;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

class SQLRouteTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertRoute(final String name, final String sql, final List<Object> params) {
        // TODO add assertion for ShardingRouteAssert.assertRoute
        ShardingRouteAssert.assertRoute(sql, params);
    }
    
    @Test
    void assertOracleWithClauseBind() {
        // HELP: moving the test somewhere
        String sql = "with U_ as  (select t.id, t.user_id, t.user_sn, t.user_no  from t_user t),  "
                + "Us1_ as  (select *  from (select t.name, t.detail from t_order_user_product1 t,  U_ where t.user_sn = U_.user_sn)  where rownum = 1),  "
                + "Us2_ as  (select *  from (select t.sub_product_url from t_order_user_product2 t,  U_ where t.user_no = U_.user_no)  where rownum = 1),  "
                + "I_ as  (select count(item.id) n  from t_order_item item,  U_  where item.user_id = U_.user_id and item.status in (1,2)), "
                + "Us3_ as (select *  from (select c.item_order_url  from t_order_item_ext c,  t_store r,  U_  where c.item_no = r.item_no and c.status = 1 "
                + "or ((select * from I_) >= 1)  and r.user_rn = U_.id  and r.store_no = 's1234')  where rownum = 1) "
                + "select Us1_.name, Us1_.detail, Us2_.sub_product_url,Us3_.item_order_url from Us1_, Us2_, Us3_";
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.EMPTY_MAP);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, databaseType, resourceMetaData, ruleMetaData, buildSchemas());
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine(databaseType,
                new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
        
        ShardingSphereMetaData shardingSphereMetaData = new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), resourceMetaData,
                ruleMetaData, new ConfigurationProperties(new Properties()));
        SQLStatementContext sqlStatementContext =
                new SQLBindEngine(shardingSphereMetaData, DefaultDatabase.LOGIC_NAME, new HintValueContext()).bind(sqlStatementParserEngine.parse(sql, false), Collections.emptyList());
        System.out.println(sqlStatementContext);
    }
    
    private static Map<String, ShardingSphereSchema> buildSchemas() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(3, 1F);
        tables.put("t_user", new ShardingSphereTable("t_user", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_sn", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("user_no", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_user_product1", new ShardingSphereTable("t_order_user_product1", Arrays.asList(
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("detail", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_sn", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_user_product2", new ShardingSphereTable("t_order_user_product2", Arrays.asList(
                new ShardingSphereColumn("user_no", Types.VARCHAR, true, false, false, true, false, false),
                new ShardingSphereColumn("sub_product_url", Types.VARCHAR, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_item", new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_item_ext", new ShardingSphereTable("t_order_item_ext", Arrays.asList(
                new ShardingSphereColumn("item_order_url", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("item_no", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_store", new ShardingSphereTable("t_store", Arrays.asList(
                new ShardingSphereColumn("item_no", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("user_rn", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("store_no", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        return Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new ShardingSphereSchema(tables, Collections.emptyMap()));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("noTableUnicastRandomDataSource", "SELECT 1, 1 + 2", Collections.singletonList(1)),
                    Arguments.of("withBroadcastTable", "SELECT a.user_id, status from t_order_item a join t_product b on a.product_id = b.product_id where a.user_id = ?",
                            Collections.singletonList(1)),
                    Arguments.of("allBindingWithBroadcastTable",
                            "SELECT a.user_id, a.status from t_order a join t_order_item b on a.order_id = b.order_id join t_product c on b.product_id = c.product_id where a.user_id = ?",
                            Collections.singletonList(1)),
                    Arguments.of("complexTableWithBroadcastTable",
                            "SELECT a.user_id, status from t_order a join t_user b on a.user_id = b.user_id join t_product c on a.product_id = c.product_id where a.user_id = ? and b.user_id =?",
                            Arrays.asList(1, 1)),
                    Arguments.of("insertTable", "INSERT INTO t_order (order_id, user_id) VALUES (?, ?)", Arrays.asList(1, 1)));
        }
    }
    
}
