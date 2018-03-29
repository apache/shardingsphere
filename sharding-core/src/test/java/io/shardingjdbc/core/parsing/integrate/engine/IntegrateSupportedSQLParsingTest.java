/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.integrate.engine;

import com.google.common.base.Optional;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.integrate.asserts.ParserAssertHelper;
import io.shardingjdbc.core.parsing.integrate.asserts.ParserJAXBHelper;
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.ConditionAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.Value;
import io.shardingjdbc.core.parsing.integrate.jaxb.root.ParserAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.table.TableAssert;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.context.table.Tables;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RequiredArgsConstructor
public final class IntegrateSupportedSQLParsingTest extends AbstractBaseIntegrateSQLParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static ParserAssertsLoader parserAssertsLoader = ParserAssertsLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    @Parameters(name = "{0}In{1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    @Test
    public void assertLiteralSQL() throws NoSuchFieldException, IllegalAccessException {
        assertSQLStatement(new SQLParsingEngine(
                databaseType, sqlCasesLoader.getSupportedLiteralSQL(sqlCaseId, parserAssertsLoader.getParserAssert(sqlCaseId).getParameters()), getShardingRule()).parse(), false);
    }
    
    @Test
    public void assertPlaceholderSQL() throws NoSuchFieldException, IllegalAccessException {
        assertSQLStatement(new SQLParsingEngine(databaseType, sqlCasesLoader.getSupportedPlaceholderSQL(sqlCaseId), getShardingRule()).parse(), true);
    }
    
    private void assertSQLStatement(final SQLStatement actual, final boolean isPreparedStatement) throws NoSuchFieldException, IllegalAccessException {
        ParserAssert parserAssert = parserAssertsLoader.getParserAssert(sqlCaseId);
        assertTables(actual.getTables(), parserAssert.getTables());
        assertConditions(actual.getConditions(), parserAssert.getConditions());
        
        
        ParserAssertHelper.assertSqlTokens(parserAssert.getTokens().getTokenAsserts(), actual.getSqlTokens(), isPreparedStatement);
        if (actual instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) actual;
            SelectStatement expectedSqlStatement = ParserJAXBHelper.getSelectStatement(parserAssert);
            ParserAssertHelper.assertOrderBy(expectedSqlStatement.getOrderByItems(), selectStatement.getOrderByItems());
            ParserAssertHelper.assertGroupBy(expectedSqlStatement.getGroupByItems(), selectStatement.getGroupByItems());
            ParserAssertHelper.assertAggregationSelectItem(expectedSqlStatement.getAggregationSelectItems(), selectStatement.getAggregationSelectItems());
            ParserAssertHelper.assertLimit(parserAssert.getLimit(), selectStatement.getLimit(), isPreparedStatement);
        }
    }
    
    private void assertTables(final Tables actual, final List<TableAssert> expected) {
        assertThat(actual.getTableNames().size(), is(expected.size()));
        for (TableAssert each : expected) {
            Optional<Table> tableOptional;
            if (null != each.getAlias()) {
                tableOptional = actual.find(each.getAlias());
            } else {
                tableOptional = actual.find(each.getName());
            }
            assertTrue(tableOptional.isPresent());
            assertTable(tableOptional.get(), each);
        }
    }
    
    private void assertTable(final Table actual, final TableAssert expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getAlias().orNull(), is(expected.getAlias()));
    }
    
    private void assertConditions(final Conditions actual, final List<ConditionAssert> expected) throws NoSuchFieldException, IllegalAccessException {
        assertThat(actual.getConditions().size(), is(expected.size()));
        for (ConditionAssert each : expected) {
            Optional<Condition> conditionOptional = actual.find(new Column(each.getColumnName(), each.getTableName()));
            assertTrue(conditionOptional.isPresent());
            assertCondition(conditionOptional.get(), each);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void assertCondition(final Condition actual, final ConditionAssert expected) throws NoSuchFieldException, IllegalAccessException {
        assertThat(actual.getColumn().getName().toUpperCase(), is(expected.getColumnName().toUpperCase()));
        assertThat(actual.getColumn().getTableName().toUpperCase(), is(expected.getTableName().toUpperCase()));
        assertThat(actual.getOperator().name(), is(expected.getOperator()));
        int count = 0;
        for (Value each : expected.getValues()) {
            Map<Integer, Comparable<?>> positionValueMap = (Map<Integer, Comparable<?>>) getField(actual, "positionValueMap");
            Map<Integer, Integer> positionIndexMap = (Map<Integer, Integer>) getField(actual, "positionIndexMap");
            if (!positionValueMap.isEmpty()) {
                assertThat(positionValueMap.get(count), is((Comparable) each.getLiteralForAccurateType()));
            } else if (!positionIndexMap.isEmpty()) {
                assertThat(positionIndexMap.get(count), is(each.getIndex()));
            }
            count++;
        }
    }
    
    private Object getField(final Object actual, final String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = actual.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(actual);
    }
}
