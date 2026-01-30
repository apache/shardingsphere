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

package org.apache.shardingsphere.test.it.sql.binder;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.test.it.sql.binder.cases.registry.binder.SQLBinderTestCasesRegistry;
import org.apache.shardingsphere.test.it.sql.binder.cases.registry.sql.SQLBinderCasesRegistry;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.SQLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.SQLParserTestCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.SQLCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

public abstract class SQLBinderIT {
    
    private static final SQLCases SQL_CASES = SQLBinderCasesRegistry.getInstance().getCases();
    
    private static final SQLParserTestCases SQL_BINDER_TEST_CASES = SQLBinderTestCasesRegistry.getInstance().getCases();
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertBind(final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, SQL_BINDER_TEST_CASES.get(sqlCaseId).getParameters());
        SQLParserTestCase expected = SQL_BINDER_TEST_CASES.get(sqlCaseId);
        SQLStatement actual = bindSQLStatement("H2".equals(databaseType) ? "MySQL" : databaseType, sql, new ArrayList<>(expected.getParameters()));
        SQLStatementAssert.assertIs(new SQLCaseAssertContext(sqlCaseId, sql, expected.getParameters(), sqlCaseType), actual, expected);
    }
    
    private SQLStatement bindSQLStatement(final String databaseType, final String sql, final List<Object> parameters) {
        HintValueContext hintValueContext = SQLHintUtils.extractHint(sql);
        SQLStatement sqlStatement = new SQLStatementVisitorEngine(databaseType).visit(new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false));
        return new SQLBindEngine(mockMetaData(TypedSPILoader.getService(DatabaseType.class, databaseType)), "foo_db_1", hintValueContext).bind(sqlStatement).getSqlStatement();
    }
    
    private ShardingSphereMetaData mockMetaData(final DatabaseType databaseType) {
        Collection<ShardingSphereDatabase> databases = new LinkedList<>();
        databases.add(new ShardingSphereDatabase("foo_db_1", databaseType, mock(ResourceMetaData.class), mock(RuleMetaData.class), mockSchemas(databaseType, "foo_db_1")));
        databases.add(new ShardingSphereDatabase("foo_db_2", databaseType, mock(ResourceMetaData.class), mock(RuleMetaData.class), mockSchemas(databaseType, "foo_db_2")));
        return new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
    }
    
    private Collection<ShardingSphereSchema> mockSchemas(final DatabaseType databaseType, final String databaseName) {
        Collection<ShardingSphereSchema> result = new LinkedList<>();
        String defaultSchemaName = new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(databaseName);
        Collection<ShardingSphereTable> tables = "foo_db_1".equalsIgnoreCase(databaseName) ? mockFooDB1Tables() : mockFooDB2Tables();
        result.add(new ShardingSphereSchema(defaultSchemaName, databaseType, tables, Collections.emptyList()));
        return result;
    }
    
    private Collection<ShardingSphereTable> mockFooDB1Tables() {
        Collection<ShardingSphereTable> result = new LinkedList<>();
        result.add(new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("order_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("merchant_id", Types.INTEGER, false, false, false, true, false, true),
                new ShardingSphereColumn("remark", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)),
                Collections.singletonList(new ShardingSphereIndex("idx_user_id", Collections.singletonList("user_id"), false)), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("item_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.BIGINT, false, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("product_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("quantity", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_user", Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("email", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("telephone", Types.CHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_merchant", Arrays.asList(
                new ShardingSphereColumn("merchant_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("country_id", Types.SMALLINT, false, false, false, true, false, false),
                new ShardingSphereColumn("merchant_name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("business_code", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("telephone", Types.CHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_order_view", Arrays.asList(
                new ShardingSphereColumn("order_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("merchant_id", Types.INTEGER, false, false, false, true, false, true),
                new ShardingSphereColumn("remark", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        return result;
    }
    
    private Collection<ShardingSphereTable> mockFooDB2Tables() {
        Collection<ShardingSphereTable> result = new LinkedList<>();
        result.add(new ShardingSphereTable("t_product", Arrays.asList(
                new ShardingSphereColumn("product_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("product_name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("category_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("price", Types.DECIMAL, false, false, false, true, false, true),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_product_detail", Arrays.asList(
                new ShardingSphereColumn("detail_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("product_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("description", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_product_category", Arrays.asList(
                new ShardingSphereColumn("category_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("category_name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("parent_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("level", Types.TINYINT, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        result.add(new ShardingSphereTable("t_country", Arrays.asList(
                new ShardingSphereColumn("country_id", Types.SMALLINT, true, false, false, true, false, false),
                new ShardingSphereColumn("country_name", Types.VARCHAR, false, false, false, true, false, true),
                new ShardingSphereColumn("continent_name", Types.VARCHAR, false, false, false, true, false, true),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        return result;
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            SQLBinderITSettings settings = context.getRequiredTestClass().getAnnotation(SQLBinderITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation SQLBinderITSettings is required.");
            return getTestParameters(settings.value()).stream();
        }
        
        private Collection<Arguments> getTestParameters(final String... databaseTypes) {
            Collection<Arguments> result = new LinkedList<>();
            for (InternalSQLParserTestParameter each : SQL_CASES.generateTestParameters(Arrays.stream(databaseTypes).collect(Collectors.toSet()))) {
                if (!isPlaceholderWithoutParameter(each)) {
                    result.add(Arguments.arguments(each.getSqlCaseId(), each.getSqlCaseType(), each.getDatabaseType()));
                }
            }
            return result;
        }
        
        private boolean isPlaceholderWithoutParameter(final InternalSQLParserTestParameter testParam) {
            return SQLCaseType.PLACEHOLDER == testParam.getSqlCaseType() && SQL_BINDER_TEST_CASES.get(testParam.getSqlCaseId()).getParameters().isEmpty();
        }
    }
}
