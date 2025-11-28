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

package org.apache.shardingsphere.sqlfederation.compiler.compiler.it;

import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.fun.SqlLibraryOperatorTableFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.compiler.SQLStatementCompiler;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationSchema;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.MySQLOperatorTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(createOrderFederationTableMetaData());
        tables.add(createUserInfoTableMetaData());
        tables.add(createTOrderTableMetaData());
        tables.add(createTOrderItemTableMetaData());
        tables.add(createTSingleTableMetaData());
        tables.add(createTOrderFederateShardingMetaData());
        tables.add(createTOrderItemFederateShardingMetaData());
        tables.add(createTMerchantMetaData());
        tables.add(createTProductMetaData());
        tables.add(createTProductDetailMetaData());
        tables.add(createMultiTypesFirstTableMetaData());
        tables.add(createMultiTypesSecondTableMetaData());
        CalciteSchema calciteSchema = CalciteSchema.createRootSchema(true);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
        calciteSchema.add(SCHEMA_NAME, new SQLFederationSchema(SCHEMA_NAME, new ShardingSphereSchema("foo_db", tables, Collections.emptyList()), databaseType));
        sqlStatementCompiler = new SQLStatementCompiler(
                new SQLFederationRelConverter(new CompilerContext(mock(SQLParserRule.class), calciteSchema, new CalciteConnectionConfigImpl(new Properties()), getOperatorTables()),
                        Collections.singletonList("federate_jdbc"), databaseType, EnumerableConvention.INSTANCE),
                EnumerableConvention.INSTANCE);
    }
    
    private Collection<SqlOperatorTable> getOperatorTables() {
        SqlOperatorTable operatorTable =
                SqlLibraryOperatorTableFactory.INSTANCE.getOperatorTable(Arrays.asList(SqlLibrary.STANDARD, SqlLibrary.MYSQL));
        return Arrays.asList(new MySQLOperatorTable(), operatorTable);
    }
    
    private ShardingSphereTable createOrderFederationTableMetaData() {
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.VARCHAR, true, false,"varchar", false, true, false, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        return new ShardingSphereTable("t_order_federate", Arrays.asList(orderIdColumn, userIdColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createUserInfoTableMetaData() {
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, true, false,"varchar", false, true, false, false);
        ShardingSphereColumn informationColumn = new ShardingSphereColumn("information", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        return new ShardingSphereTable("t_user_info", Arrays.asList(userIdColumn, informationColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderTableMetaData() {
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.BIGINT, true, false,"bigint", false, true, false, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn merchantIdColumn = new ShardingSphereColumn("merchant_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn remarkColumn = new ShardingSphereColumn("remark", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false,"date", false, true, false, false);
        return new ShardingSphereTable("t_order", Arrays.asList(orderIdColumn, userIdColumn, statusColumn, merchantIdColumn, remarkColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderItemTableMetaData() {
        ShardingSphereColumn itemIdColumn = new ShardingSphereColumn("item_id", Types.BIGINT, true, false,"bigint", false, true, false, false);
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.BIGINT, false, false,"bigint", false, true, false, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn productIdColumn = new ShardingSphereColumn("product_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn quantityColumn = new ShardingSphereColumn("quantity", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false,"date", false, true, false, false);
        return new ShardingSphereTable("t_order_item", Arrays.asList(itemIdColumn, orderIdColumn, userIdColumn, productIdColumn, quantityColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTSingleTableMetaData() {
        ShardingSphereColumn singleIdColumn = new ShardingSphereColumn("single_id", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        return new ShardingSphereTable("t_single_table", Arrays.asList(singleIdColumn, idColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderFederateShardingMetaData() {
        ShardingSphereColumn orderIdShardingColumn = new ShardingSphereColumn("order_id_sharding", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        return new ShardingSphereTable("t_order_federate_sharding", Arrays.asList(orderIdShardingColumn, userIdColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTOrderItemFederateShardingMetaData() {
        ShardingSphereColumn itemIdColumn = new ShardingSphereColumn("item_id", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn remarksColumn = new ShardingSphereColumn("remarks", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        return new ShardingSphereTable("t_order_item_federate_sharding", Arrays.asList(itemIdColumn, orderIdColumn, userIdColumn, statusColumn, remarksColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTMerchantMetaData() {
        ShardingSphereColumn merchantIdColumn = new ShardingSphereColumn("merchant_id", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn countryIdColumn = new ShardingSphereColumn("country_id", Types.SMALLINT, false, false,"smallint", false, true, false, false);
        ShardingSphereColumn merchantNameColumn = new ShardingSphereColumn("merchant_name", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn businessCodeColumn = new ShardingSphereColumn("business_code", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn telephoneColumn = new ShardingSphereColumn("telephone", Types.CHAR, false, false,"char", false, true, false, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false,"date", false, true, false, false);
        return new ShardingSphereTable("t_merchant", Arrays.asList(merchantIdColumn, countryIdColumn, merchantNameColumn, businessCodeColumn, telephoneColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTProductDetailMetaData() {
        ShardingSphereColumn detailIdColumn = new ShardingSphereColumn("detail_id", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn productIdColumn = new ShardingSphereColumn("product_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn descriptionColumn = new ShardingSphereColumn("description", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false,"date", false, true, false, false);
        return new ShardingSphereTable("t_product_detail", Arrays.asList(detailIdColumn, productIdColumn, descriptionColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createTProductMetaData() {
        ShardingSphereColumn productIdColumn = new ShardingSphereColumn("product_id", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn productNameColumn = new ShardingSphereColumn("product_name", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn categoryIdColumn = new ShardingSphereColumn("category_id", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn priceColumn = new ShardingSphereColumn("price", Types.DECIMAL, false, false,"decimal", false, true, false, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn creationDateColumn = new ShardingSphereColumn("creation_date", Types.DATE, false, false,"date", false, true, false, false);
        return new ShardingSphereTable("t_product", Arrays.asList(productIdColumn, productNameColumn, categoryIdColumn, priceColumn, statusColumn, creationDateColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createMultiTypesFirstTableMetaData() {
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn bitColumn = new ShardingSphereColumn("bit_column", Types.BIT, false, false,"bit", false, true, false, false);
        ShardingSphereColumn tinyIntColumn = new ShardingSphereColumn("tiny_int_column", Types.TINYINT, false, false,"tinyint", false, true, false, false);
        ShardingSphereColumn smallIntColumn = new ShardingSphereColumn("small_int_column", Types.SMALLINT, false, false,"smallint", false, true, false, false);
        ShardingSphereColumn integerColumn = new ShardingSphereColumn("integer_column", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn bigIntColumn = new ShardingSphereColumn("big_int_column", Types.BIGINT, false, false,"bigint", false, true, false, false);
        ShardingSphereColumn floatColumn = new ShardingSphereColumn("float_column", Types.FLOAT, false, false,"float", false, true, false, false);
        ShardingSphereColumn realColumn = new ShardingSphereColumn("real_column", Types.REAL, false, false,"real", false, true, false, false);
        ShardingSphereColumn doubleColumn = new ShardingSphereColumn("double_column", Types.DOUBLE, false, false,"double", false, true, false, false);
        ShardingSphereColumn numericColumn = new ShardingSphereColumn("numeric_column", Types.NUMERIC, false, false,"numeric", false, true, false, false);
        ShardingSphereColumn decimalColumn = new ShardingSphereColumn("decimal_column", Types.DECIMAL, false, false,"decimal", false, true, false, false);
        ShardingSphereColumn charColumn = new ShardingSphereColumn("char_column", Types.CHAR, false, false,"char", false, true, false, false);
        ShardingSphereColumn varcharColumn = new ShardingSphereColumn("varchar_column", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn longVarcharColumn = new ShardingSphereColumn("long_varchar_column", Types.LONGVARCHAR, false, false,"text", false, true, false, false);
        ShardingSphereColumn dateColumn = new ShardingSphereColumn("date_column", Types.DATE, false, false,"date", false, true, false, false);
        ShardingSphereColumn timeColumn = new ShardingSphereColumn("time_column", Types.TIME, false, false,"time", false, true, false, false);
        ShardingSphereColumn timeStampColumn = new ShardingSphereColumn("time_stamp_column", Types.TIMESTAMP, false, false,"time_stamp", false, true, false, false);
        ShardingSphereColumn binaryColumn = new ShardingSphereColumn("binary_column", Types.BINARY, false, false,"binary", false, true, false, false);
        ShardingSphereColumn varBinaryColumn = new ShardingSphereColumn("varbinary_column", Types.VARBINARY, false, false,"varbit", false, true, false, false);
        ShardingSphereColumn longVarbinaryColumn = new ShardingSphereColumn("long_varbinary_column", Types.LONGVARBINARY, false, false,"long_varbinary", false, true, false, false);
        ShardingSphereColumn nullColumn = new ShardingSphereColumn("null_column", Types.NULL, false, false,"null", false, true, false, false);
        ShardingSphereColumn otherColumn = new ShardingSphereColumn("other_column", Types.OTHER, false, false,"other", false, true, false, false);
        return new ShardingSphereTable("multi_types_first", Arrays.asList(idColumn, bitColumn, tinyIntColumn, smallIntColumn, integerColumn, bigIntColumn, floatColumn,
                realColumn, doubleColumn, numericColumn, decimalColumn, charColumn, varcharColumn, longVarcharColumn, dateColumn, timeColumn, timeStampColumn, binaryColumn,
                varBinaryColumn, longVarbinaryColumn, nullColumn, otherColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createMultiTypesSecondTableMetaData() {
        ShardingSphereColumn idColumn = new ShardingSphereColumn("id", Types.INTEGER, true, false,"int", false, true, false, false);
        ShardingSphereColumn bitColumn = new ShardingSphereColumn("bit_column", Types.BIT, false, false,"bit", false, true, false, false);
        ShardingSphereColumn tinyIntColumn = new ShardingSphereColumn("tiny_int_column", Types.TINYINT, false, false,"tinyint", false, true, false, false);
        ShardingSphereColumn smallIntColumn = new ShardingSphereColumn("small_int_column", Types.SMALLINT, false, false,"smallint", false, true, false, false);
        ShardingSphereColumn integerColumn = new ShardingSphereColumn("integer_column", Types.INTEGER, false, false,"int", false, true, false, false);
        ShardingSphereColumn bigIntColumn = new ShardingSphereColumn("big_int_column", Types.BIGINT, false, false,"bigint", false, true, false, false);
        ShardingSphereColumn floatColumn = new ShardingSphereColumn("float_column", Types.FLOAT, false, false,"float", false, true, false, false);
        ShardingSphereColumn realColumn = new ShardingSphereColumn("real_column", Types.REAL, false, false,"real", false, true, false, false);
        ShardingSphereColumn doubleColumn = new ShardingSphereColumn("double_column", Types.DOUBLE, false, false,"double", false, true, false, false);
        ShardingSphereColumn numericColumn = new ShardingSphereColumn("numeric_column", Types.NUMERIC, false, false,"numeric", false, true, false, false);
        ShardingSphereColumn decimalColumn = new ShardingSphereColumn("decimal_column", Types.DECIMAL, false, false,"decimal", false, true, false, false);
        ShardingSphereColumn charColumn = new ShardingSphereColumn("char_column", Types.CHAR, false, false,"char", false, true, false, false);
        ShardingSphereColumn varcharColumn = new ShardingSphereColumn("varchar_column", Types.VARCHAR, false, false,"varchar", false, true, false, false);
        ShardingSphereColumn longVarcharColumn = new ShardingSphereColumn("long_varchar_column", Types.LONGVARCHAR, false, false,"text", false, true, false, false);
        ShardingSphereColumn dateColumn = new ShardingSphereColumn("date_column", Types.DATE, false, false,"date", false, true, false, false);
        ShardingSphereColumn timeColumn = new ShardingSphereColumn("time_column", Types.TIME, false, false,"time", false, true, false, false);
        ShardingSphereColumn timeStampColumn = new ShardingSphereColumn("time_stamp_column", Types.TIMESTAMP, false, false,"time_stamp", false, true, false, false);
        ShardingSphereColumn binaryColumn = new ShardingSphereColumn("binary_column", Types.BINARY, false, false,"binary", false, true, false, false);
        ShardingSphereColumn varBinaryColumn = new ShardingSphereColumn("varbinary_column", Types.VARBINARY, false, false,"varbit", false, true, false, false);
        ShardingSphereColumn longVarbinaryColumn = new ShardingSphereColumn("long_varbinary_column", Types.LONGVARBINARY, false, false,"long_varbinary", false, true, false, false);
        return new ShardingSphereTable("multi_types_second", Arrays.asList(idColumn, bitColumn, tinyIntColumn, smallIntColumn, integerColumn, bigIntColumn, floatColumn,
                realColumn, doubleColumn, numericColumn, decimalColumn, charColumn, varcharColumn, longVarcharColumn, dateColumn, timeColumn, timeStampColumn, binaryColumn,
                varBinaryColumn, longVarbinaryColumn),
                Collections.emptyList(), Collections.emptyList());
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertCompile(final TestCase testcase) {
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL")).parse(testcase.getSql(), false);
        String actual = sqlStatementCompiler.compile(sqlStatement, "MySQL").getPhysicalPlan().explain().replaceAll(System.lineSeparator(), " ");
        assertThat(actual, is(testcase.getAssertion().iterator().next().getExpectedResult()));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) throws IOException {
            return TestCasesLoader.getInstance().generate().stream().map(Arguments::of);
        }
    }
}
