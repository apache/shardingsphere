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

package org.apache.shardingsphere.sqlfederation.compiler.it;

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
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.h2.H2DatabaseType;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationSchema;
import org.apache.shardingsphere.sqlfederation.compiler.planner.util.SQLFederationPlannerUtils;
import org.apache.shardingsphere.sqlfederation.compiler.statement.SQLStatementCompiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class SQLStatementCompilerIT {
    
    private static final String SCHEMA_NAME = "federate_jdbc";
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private SQLStatementCompiler sqlStatementCompiler;
    
    @BeforeEach
    void init() {
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
        tables.put("multi_types_first", createMultiTypesFirstTableMetaData());
        tables.put("multi_types_second", createMultiTypesSecondTableMetaData());
        sqlStatementCompiler = new SQLStatementCompiler(createSqlToRelConverter(new ShardingSphereSchema(tables, Collections.emptyMap())));
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
    
    private ShardingSphereTable createMultiTypesFirstTableMetaData() {
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn bitColumn = new ShardingSphereColumn("bit_column", Types.BIT, false, false, false, true, false);
        ShardingSphereColumn tinyIntColumn = new ShardingSphereColumn("tiny_int_column", Types.TINYINT, false, false, false, true, false);
        ShardingSphereColumn smallIntColumn = new ShardingSphereColumn("small_int_column", Types.SMALLINT, false, false, false, true, false);
        ShardingSphereColumn integerColumn = new ShardingSphereColumn("integer_column", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn bigIntColumn = new ShardingSphereColumn("big_int_column", Types.BIGINT, false, false, false, true, false);
        ShardingSphereColumn floatColumn = new ShardingSphereColumn("float_column", Types.FLOAT, false, false, false, true, false);
        ShardingSphereColumn realColumn = new ShardingSphereColumn("real_column", Types.REAL, false, false, false, true, false);
        ShardingSphereColumn doubleColumn = new ShardingSphereColumn("double_column", Types.DOUBLE, false, false, false, true, false);
        ShardingSphereColumn numericColumn = new ShardingSphereColumn("numeric_column", Types.NUMERIC, false, false, false, true, false);
        ShardingSphereColumn decimalColumn = new ShardingSphereColumn("decimal_column", Types.DECIMAL, false, false, false, true, false);
        ShardingSphereColumn charColumn = new ShardingSphereColumn("char_column", Types.CHAR, false, false, false, true, false);
        ShardingSphereColumn varcharColumn = new ShardingSphereColumn("varchar_column", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn longVarcharColumn = new ShardingSphereColumn("long_varchar_column", Types.LONGVARCHAR, false, false, false, true, false);
        ShardingSphereColumn dateColumn = new ShardingSphereColumn("date_column", Types.DATE, false, false, false, true, false);
        ShardingSphereColumn timeColumn = new ShardingSphereColumn("time_column", Types.TIME, false, false, false, true, false);
        ShardingSphereColumn timeStampColumn = new ShardingSphereColumn("time_stamp_column", Types.TIMESTAMP, false, false, false, true, false);
        ShardingSphereColumn binaryColumn = new ShardingSphereColumn("binary_column", Types.BINARY, false, false, false, true, false);
        ShardingSphereColumn varBinaryColumn = new ShardingSphereColumn("varbinary_column", Types.VARBINARY, false, false, false, true, false);
        ShardingSphereColumn longVarbinaryColumn = new ShardingSphereColumn("long_varbinary_column", Types.LONGVARBINARY, false, false, false, true, false);
        ShardingSphereColumn nullColumn = new ShardingSphereColumn("null_column", Types.NULL, false, false, false, true, false);
        ShardingSphereColumn otherColumn = new ShardingSphereColumn("other_column", Types.OTHER, false, false, false, true, false);
        return new ShardingSphereTable("multi_types_first", Arrays.asList(idColumn, bitColumn, tinyIntColumn, smallIntColumn, integerColumn, bigIntColumn, floatColumn,
                realColumn, doubleColumn, numericColumn, decimalColumn, charColumn, varcharColumn, longVarcharColumn, dateColumn, timeColumn, timeStampColumn, binaryColumn,
                varBinaryColumn, longVarbinaryColumn, nullColumn, otherColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createMultiTypesSecondTableMetaData() {
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false);
        ShardingSphereColumn bitColumn = new ShardingSphereColumn("bit_column", Types.BIT, false, false, false, true, false);
        ShardingSphereColumn tinyIntColumn = new ShardingSphereColumn("tiny_int_column", Types.TINYINT, false, false, false, true, false);
        ShardingSphereColumn smallIntColumn = new ShardingSphereColumn("small_int_column", Types.SMALLINT, false, false, false, true, false);
        ShardingSphereColumn integerColumn = new ShardingSphereColumn("integer_column", Types.INTEGER, false, false, false, true, false);
        ShardingSphereColumn bigIntColumn = new ShardingSphereColumn("big_int_column", Types.BIGINT, false, false, false, true, false);
        ShardingSphereColumn floatColumn = new ShardingSphereColumn("float_column", Types.FLOAT, false, false, false, true, false);
        ShardingSphereColumn realColumn = new ShardingSphereColumn("real_column", Types.REAL, false, false, false, true, false);
        ShardingSphereColumn doubleColumn = new ShardingSphereColumn("double_column", Types.DOUBLE, false, false, false, true, false);
        ShardingSphereColumn numericColumn = new ShardingSphereColumn("numeric_column", Types.NUMERIC, false, false, false, true, false);
        ShardingSphereColumn decimalColumn = new ShardingSphereColumn("decimal_column", Types.DECIMAL, false, false, false, true, false);
        ShardingSphereColumn charColumn = new ShardingSphereColumn("char_column", Types.CHAR, false, false, false, true, false);
        ShardingSphereColumn varcharColumn = new ShardingSphereColumn("varchar_column", Types.VARCHAR, false, false, false, true, false);
        ShardingSphereColumn longVarcharColumn = new ShardingSphereColumn("long_varchar_column", Types.LONGVARCHAR, false, false, false, true, false);
        ShardingSphereColumn dateColumn = new ShardingSphereColumn("date_column", Types.DATE, false, false, false, true, false);
        ShardingSphereColumn timeColumn = new ShardingSphereColumn("time_column", Types.TIME, false, false, false, true, false);
        ShardingSphereColumn timeStampColumn = new ShardingSphereColumn("time_stamp_column", Types.TIMESTAMP, false, false, false, true, false);
        ShardingSphereColumn binaryColumn = new ShardingSphereColumn("binary_column", Types.BINARY, false, false, false, true, false);
        ShardingSphereColumn varBinaryColumn = new ShardingSphereColumn("varbinary_column", Types.VARBINARY, false, false, false, true, false);
        ShardingSphereColumn longVarbinaryColumn = new ShardingSphereColumn("long_varbinary_column", Types.LONGVARBINARY, false, false, false, true, false);
        return new ShardingSphereTable("multi_types_second", Arrays.asList(idColumn, bitColumn, tinyIntColumn, smallIntColumn, integerColumn, bigIntColumn, floatColumn,
                realColumn, doubleColumn, numericColumn, decimalColumn, charColumn, varcharColumn, longVarcharColumn, dateColumn, timeColumn, timeStampColumn, binaryColumn,
                varBinaryColumn, longVarbinaryColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private SqlToRelConverter createSqlToRelConverter(final ShardingSphereSchema schema) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(new Properties());
        RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
        DatabaseType databaseType = DatabaseTypeFactory.get("H2");
        SQLFederationSchema sqlFederationSchema = new SQLFederationSchema(SCHEMA_NAME, schema, databaseType, new JavaTypeFactoryImpl());
        CalciteCatalogReader catalogReader = SQLFederationPlannerUtils.createCatalogReader(SCHEMA_NAME, sqlFederationSchema, relDataTypeFactory, connectionConfig);
        SqlValidator validator = SQLFederationPlannerUtils.createSqlValidator(catalogReader, relDataTypeFactory, databaseType, connectionConfig);
        RelOptCluster cluster = RelOptCluster.create(SQLFederationPlannerUtils.createVolcanoPlanner(), new RexBuilder(relDataTypeFactory));
        return SQLFederationPlannerUtils.createSqlToRelConverter(catalogReader, validator, cluster, mock(SQLParserRule.class), databaseType, false);
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertCompile(final TestCase testcase) {
        String databaseType = DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType());
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(testcase.getSql(), false);
        String actual = sqlStatementCompiler.compile(sqlStatement, databaseType).getPhysicalPlan().explain().replaceAll("[\r\n]", " ");
        assertThat(actual, is(testcase.getAssertion().getExpectedResult()));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @SneakyThrows({IOException.class, JAXBException.class})
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return TestCasesLoader.getInstance().generate().stream().map(Arguments::of);
        }
    }
}
