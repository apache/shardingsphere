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

package org.apache.shardingsphere.sqlfederation.optimizer.it;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLOptimizeEngine;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable.TranslatableSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.util.SQLFederationPlannerUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class SQLOptimizeEngineIT {
    
    private static final String SCHEMA_NAME = "federate_jdbc";
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private final TestCase testcase;
    
    private SQLOptimizeEngine optimizeEngine;
    
    @SneakyThrows({IOException.class, JAXBException.class})
    @Parameters(name = "{0}")
    public static Collection<TestCase> data() {
        return TestCasesLoader.getInstance().generate();
    }
    
    @Before
    public void init() {
        Map<String, ShardingSphereTable> tables = new HashMap<>();
        tables.put("t_order_federate", createOrderFederationTableMetaData());
        tables.put("t_user_info", createUserInfoTableMetaData());
        tables.put("t_order", createTOrderTableMetaData());
        tables.put("t_order_item", createTOrderItemTableMetaData());
        tables.put("t_single_table", createTSingleTableMetaData());
        tables.put("t_order_federate_sharding", createTOrderFederateShardingMetaData());
        tables.put("t_order_item_federate_sharding", createTOrderItemFederateShardingMetaData());
        tables.put("t_merchant", createTMerchantMetaData());
        tables.put("t_product", createTProductMetaData());
        tables.put("t_product_detail", createTProductDetailMetaData());
        optimizeEngine = new SQLOptimizeEngine(createSqlToRelConverter(new ShardingSphereSchema(tables, Collections.emptyMap())), SQLFederationPlannerUtil.createHepPlanner());
    }
    
    private ShardingSphereTable createOrderFederationTableMetaData() {
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.VARCHAR, true, false, false, true, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false);
        return new ShardingSphereTable("t_order_federate", Arrays.asList(orderIdColumn, userIdColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createUserInfoTableMetaData() {
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, true, false, false, true, false);
        ShardingSphereColumn informationColumn = new ShardingSphereColumn("information", Types.VARCHAR, false, false, false, true, false);
        return new ShardingSphereTable("t_user_info", Arrays.asList(userIdColumn, informationColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderTableMetaData() {
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.BIGINT, true, false, false, true, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn merchantIdColumn = new ShardingSphereColumn("merchant_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn remarkColumn = new ShardingSphereColumn("remark", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false);
        return new ShardingSphereTable("t_order", Arrays.asList(orderIdColumn, userIdColumn, statusColumn, merchantIdColumn, remarkColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderItemTableMetaData() {
        ShardingSphereColumn itemIdColumn = new ShardingSphereColumn("item_id", Types.BIGINT, true, false, false, true, false);
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.BIGINT, false, false, false, true, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn productIdColumn = new ShardingSphereColumn("product_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn quantityColumn = new ShardingSphereColumn("quantity", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false);
        return new ShardingSphereTable("t_order_item", Arrays.asList(itemIdColumn, orderIdColumn, userIdColumn, productIdColumn, quantityColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTSingleTableMetaData() {
        ShardingSphereColumn singleIdColumn = new ShardingSphereColumn("single_id", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false);
        return new ShardingSphereTable("t_single_table", Arrays.asList(singleIdColumn, idColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderFederateShardingMetaData() {
        ShardingSphereColumn orderIdShardingColumn = new ShardingSphereColumn("order_id_sharding", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false);
        return new ShardingSphereTable("t_order_federate_sharding", Arrays.asList(orderIdShardingColumn, userIdColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderItemFederateShardingMetaData() {
        ShardingSphereColumn itemIdColumn = new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn remarksColumn = new ShardingSphereColumn("remarks", Types.VARCHAR, false, false, false, true, false);
        return new ShardingSphereTable("t_order_item_federate_sharding", Arrays.asList(itemIdColumn, orderIdColumn, userIdColumn, statusColumn, remarksColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTMerchantMetaData() {
        ShardingSphereColumn merchantIdColumn = new ShardingSphereColumn("merchant_id", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn countryIdColumn = new ShardingSphereColumn("country_id", Types.SMALLINT, false, false, false, true, false);
        ShardingSphereColumn merchantNameColumn = new ShardingSphereColumn("merchant_name", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn businessCodeColumn = new ShardingSphereColumn("business_code", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn telephoneColumn = new ShardingSphereColumn("telephone", Types.CHAR, false, false, false, true, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false);
        return new ShardingSphereTable("t_merchant", Arrays.asList(merchantIdColumn, countryIdColumn, merchantNameColumn, businessCodeColumn, telephoneColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTProductDetailMetaData() {
        ShardingSphereColumn detailIdColumn = new ShardingSphereColumn("detail_id", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn productIdColumn = new ShardingSphereColumn("product_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn descriptionColumn = new ShardingSphereColumn("description", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false);
        return new ShardingSphereTable("t_product_detail", Arrays.asList(detailIdColumn, productIdColumn, descriptionColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTProductMetaData() {
        ShardingSphereColumn productIdColumn = new ShardingSphereColumn("product_id", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn productNameColumn = new ShardingSphereColumn("product_name", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn categoryIdColumn = new ShardingSphereColumn("category_id", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn priceColumn = new ShardingSphereColumn("price", Types.DECIMAL, false, false, false, true, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false);
        return new ShardingSphereTable("t_product", Arrays.asList(productIdColumn, productNameColumn, categoryIdColumn, priceColumn, statusColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private SqlToRelConverter createSqlToRelConverter(final ShardingSphereSchema schema) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(new Properties());
        RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
        DatabaseType databaseType = DatabaseTypeEngine.getDatabaseType("H2");
        TranslatableSchema federationSchema = new TranslatableSchema(SCHEMA_NAME, schema, databaseType, new JavaTypeFactoryImpl(), null);
        CalciteCatalogReader catalogReader = SQLFederationPlannerUtil.createCatalogReader(SCHEMA_NAME, federationSchema, relDataTypeFactory, connectionConfig);
        SqlValidator validator = SQLFederationPlannerUtil.createSqlValidator(catalogReader, relDataTypeFactory, databaseType, connectionConfig);
        RelOptCluster cluster = RelOptCluster.create(SQLFederationPlannerUtil.createVolcanoPlanner(), new RexBuilder(relDataTypeFactory));
        return SQLFederationPlannerUtil.createSqlToRelConverter(catalogReader, validator, cluster, mock(SQLParserRule.class), databaseType, false);
    }
    
    @Test
    public void assertOptimize() {
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType())).parse(testcase.getSql(), false);
        String expected = optimizeEngine.optimize(sqlStatement).getBestPlan().explain().replaceAll("[\r\n]", "");
        assertThat(testcase.getAssertion().getExpectedResult(), is(expected));
    }
}
