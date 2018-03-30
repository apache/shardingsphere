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
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.ConditionAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.Value;
import io.shardingjdbc.core.parsing.integrate.jaxb.root.ParserAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.table.TableAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.GeneratedKeyTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.IndexTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ItemsTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.MultipleInsertValuesTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OffsetTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OrderByTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.RowCountTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.SQLTokenAsserts;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.TableTokenAssert;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.context.table.Tables;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.parsing.parser.token.ItemsToken;
import io.shardingjdbc.core.parsing.parser.token.MultipleInsertValuesToken;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.OrderByToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.core.parsing.parser.token.TableToken;
import io.shardingjdbc.test.sql.SQLCaseType;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RequiredArgsConstructor
public final class IntegrateSupportedSQLParsingTest extends AbstractBaseIntegrateSQLParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static ParserAssertsLoader parserAssertsLoader = ParserAssertsLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    private final SQLCaseType sqlCaseType;
    
    @Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    @Test
    public void assertSupportedSQL() throws NoSuchFieldException, IllegalAccessException {
        String sql = sqlCasesLoader.getSupportedSQL(sqlCaseId, sqlCaseType, parserAssertsLoader.getParserAssert(sqlCaseId).getParameters());
        assertSQLStatement(new SQLParsingEngine(databaseType, sql, getShardingRule()).parse());
    }
    
    private void assertSQLStatement(final SQLStatement actual) throws NoSuchFieldException, IllegalAccessException {
        ParserAssert parserAssert = parserAssertsLoader.getParserAssert(sqlCaseId);
        assertTables(actual.getTables(), parserAssert.getTables());
        assertConditions(actual.getConditions(), parserAssert.getConditions());
        assertSQLTokens(actual.getSqlTokens(), parserAssert.getTokens());
        
        
        
//        if (actual instanceof SelectStatement) {
//            SelectStatement selectStatement = (SelectStatement) actual;
//            SelectStatement expectedSqlStatement = ParserJAXBHelper.getSelectStatement(parserAssert);
//            ParserAssertHelper.assertOrderBy(expectedSqlStatement.getOrderByItems(), selectStatement.getOrderByItems());
//            ParserAssertHelper.assertGroupBy(expectedSqlStatement.getGroupByItems(), selectStatement.getGroupByItems());
//            ParserAssertHelper.assertAggregationSelectItem(expectedSqlStatement.getAggregationSelectItems(), selectStatement.getAggregationSelectItems());
//            ParserAssertHelper.assertLimit(parserAssert.getLimit(), selectStatement.getLimit(), withPlaceholder);
//        }
    }
    
    private void assertTables(final Tables actual, final List<TableAssert> expected) {
        assertThat(actual.getTableNames().size(), is(expected.size()));
        for (TableAssert each : expected) {
            Optional<Table> table;
            if (null != each.getAlias()) {
                table = actual.find(each.getAlias());
            } else {
                table = actual.find(each.getName());
            }
            assertTrue(table.isPresent());
            assertTable(table.get(), each);
        }
    }
    
    private void assertTable(final Table actual, final TableAssert expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getAlias().orNull(), is(expected.getAlias()));
    }
    
    private void assertConditions(final Conditions actual, final List<ConditionAssert> expected) throws NoSuchFieldException, IllegalAccessException {
        assertThat(actual.getConditions().size(), is(expected.size()));
        for (ConditionAssert each : expected) {
            Optional<Condition> condition = actual.find(new Column(each.getColumnName(), each.getTableName()));
            assertTrue(condition.isPresent());
            assertCondition(condition.get(), each);
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
    
    private void assertSQLTokens(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        assertTableTokens(actual, expected);
        assertIndexToken(actual, expected);
        assertItemsToken(actual, expected);
        assertGeneratedKeyToken(actual, expected);
        assertMultipleInsertValuesToken(actual, expected);
        assertOrderByToken(actual, expected);
        // TODO fix offset and row count
//        assertOffsetToken(actual, expected);
//        assertRowCountToken(actual, expected);
    }
    
    private void assertTableTokens(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        List<TableToken> tableTokens = getTableTokens(actual);
        assertThat(tableTokens.size(), is(expected.getTableTokens().size()));
        int count = 0;
        for (TableTokenAssert each : expected.getTableTokens()) {
            assertTableToken(tableTokens.get(count), each);
            count++;
        }
    }
    
    private void assertTableToken(final TableToken actual, final TableTokenAssert expected) {
        assertThat(actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(actual.getOriginalLiterals(), is(expected.getOriginalLiterals()));
    }
    
    private List<TableToken> getTableTokens(final List<SQLToken> actual) {
        List<TableToken> result = new ArrayList<>(actual.size());
        for (SQLToken each : actual) {
            if (each instanceof TableToken) {
                result.add((TableToken) each);
            }
        }
        return result;
    }
    
    private void assertIndexToken(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        Optional<IndexToken> indexToken = getIndexToken(actual);
        if (indexToken.isPresent()) {
            assertIndexToken(indexToken.get(), expected.getIndexToken());
        } else {
            assertNull(expected.getIndexToken());
        }
    }
    
    private void assertIndexToken(final IndexToken actual, final IndexTokenAssert expected) {
        assertThat(actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(actual.getOriginalLiterals(), is(expected.getOriginalLiterals()));
        assertThat(actual.getTableName(), is(expected.getTableName()));
    }
    
    private Optional<IndexToken> getIndexToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof IndexToken) {
                return Optional.of((IndexToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertItemsToken(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        Optional<ItemsToken> itemsToken = getItemsToken(actual);
        if (itemsToken.isPresent()) {
            assertItemsToken(itemsToken.get(), expected.getItemsToken());
        } else {
            assertNull(expected.getItemsToken());
        }
    }
    
    private void assertItemsToken(final ItemsToken actual, final ItemsTokenAssert expected) {
        assertThat(actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(actual.getItems(), is(expected.getItems()));
    }
    
    private Optional<ItemsToken> getItemsToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof ItemsToken) {
                return Optional.of((ItemsToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertGeneratedKeyToken(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        Optional<GeneratedKeyToken> generatedKeyToken = getGeneratedKeyToken(actual);
        if (generatedKeyToken.isPresent()) {
            assertGeneratedKeyToken(generatedKeyToken.get(), expected.getGeneratedKeyToken());
        } else {
            assertNull(expected.getGeneratedKeyToken());
        }
    }
    
    private void assertGeneratedKeyToken(final GeneratedKeyToken actual, final GeneratedKeyTokenAssert expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(actual.getBeginPosition(), is(expected.getBeginPositionWithPlaceholder()));
        } else {
            assertThat(actual.getBeginPosition(), is(expected.getBeginPositionWithoutPlaceholder()));
        }
    }
    
    private Optional<GeneratedKeyToken> getGeneratedKeyToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof GeneratedKeyToken) {
                return Optional.of((GeneratedKeyToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertMultipleInsertValuesToken(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        Optional<MultipleInsertValuesToken> multipleInsertValuesToken = getMultipleInsertValuesToken(actual);
        if (multipleInsertValuesToken.isPresent()) {
            assertMultipleInsertValuesToken(multipleInsertValuesToken.get(), expected.getMultipleInsertValuesToken());
        } else {
            assertNull(expected.getMultipleInsertValuesToken());
        }
    }
    
    private void assertMultipleInsertValuesToken(final MultipleInsertValuesToken actual, final MultipleInsertValuesTokenAssert expected) {
        assertThat(actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(actual.getValues(), is(expected.getValues()));
    }
    
    private Optional<MultipleInsertValuesToken> getMultipleInsertValuesToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof MultipleInsertValuesToken) {
                return Optional.of((MultipleInsertValuesToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertOrderByToken(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        Optional<OrderByToken> orderByToken = getOrderByToken(actual);
        if (orderByToken.isPresent()) {
            assertOrderByToken(orderByToken.get(), expected.getOrderByToken());
        } else {
            assertNull(expected.getOrderByToken());
        }
    }
    
    private void assertOrderByToken(final OrderByToken actual, final OrderByTokenAssert expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(actual.getBeginPosition(), is(expected.getBeginPositionWithPlaceholder()));
        } else {
            assertThat(actual.getBeginPosition(), is(expected.getBeginPositionWithoutPlaceholder()));
        }
    }
    
    private Optional<OrderByToken> getOrderByToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof OrderByToken) {
                return Optional.of((OrderByToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertOffsetToken(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        Optional<OffsetToken> offsetToken = getOffsetToken(actual);
        if (offsetToken.isPresent()) {
            assertOffsetToken(offsetToken.get(), expected.getOffsetToken());
        } else {
            assertNull(expected.getOffsetToken());
        }
    }
    
    private void assertOffsetToken(final OffsetToken actual, final OffsetTokenAssert expected) {
        assertThat(actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(actual.getOffset(), is(expected.getOffset()));
    }
    
    private Optional<OffsetToken> getOffsetToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof OffsetToken) {
                return Optional.of((OffsetToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertRowCountToken(final List<SQLToken> actual, final SQLTokenAsserts expected) {
        Optional<RowCountToken> rowCountToken = getRowCountToken(actual);
        if (rowCountToken.isPresent()) {
            assertRowCountToken(rowCountToken.get(), expected.getRowCountToken());
        } else {
            assertNull(expected.getRowCountToken());
        }
    }
    
    private void assertRowCountToken(final RowCountToken actual, final RowCountTokenAssert expected) {
        assertThat(actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(actual.getRowCount(), is(expected.getRowCount()));
    }
    
    private Optional<RowCountToken> getRowCountToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof RowCountToken) {
                return Optional.of((RowCountToken) each);
            }
        }
        return Optional.absent();
    }
    
    private String getFullAssertMessage(final String assertMessage) {
        StringBuilder result = new StringBuilder(System.getProperty("line.separator"));
        result.append("SQL case id: ");
        result.append(sqlCaseId);
        result.append(System.getProperty("line.separator"));
        result.append("SQL: ");
        if (SQLCaseType.Placeholder == sqlCaseType) {
            result.append(SQLCasesLoader.getInstance().getSupportedPlaceholderSQL(sqlCaseId));
        } else {
            result.append(SQLCasesLoader.getInstance().getSupportedLiteralSQL(sqlCaseId, parserAssertsLoader.getParserAssert(sqlCaseId).getParameters()));
        }
        result.append(System.getProperty("line.separator"));
        result.append(assertMessage);
        result.append(System.getProperty("line.separator"));
        return result.toString();
    }
}
