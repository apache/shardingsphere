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

package io.shardingjdbc.core.parsing.integrate.asserts.token;

import com.google.common.base.Optional;
import io.shardingjdbc.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedGeneratedKeyToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedIndexToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedItemsToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedMultipleInsertValuesToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedOffsetToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedOrderByToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedRowCountToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedTableToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedTokens;
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
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Token assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TokenAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert tokens.
     * 
     * @param actual actual tokens
     * @param expected expected tokens
     */
    public void assertTokens(final List<SQLToken> actual, final ExpectedTokens expected) {
        assertTableTokens(actual, expected);
        assertIndexToken(actual, expected);
        assertItemsToken(actual, expected);
        assertGeneratedKeyToken(actual, expected);
        assertMultipleInsertValuesToken(actual, expected);
        assertOrderByToken(actual, expected);
        assertOffsetToken(actual, expected);
        assertRowCountToken(actual, expected);
    }
    
    private void assertTableTokens(final List<SQLToken> actual, final ExpectedTokens expected) {
        List<TableToken> tableTokens = getTableTokens(actual);
        assertThat(assertMessage.getFullAssertMessage("Table tokens size error: "), tableTokens.size(), is(expected.getTableTokens().size()));
        int count = 0;
        for (ExpectedTableToken each : expected.getTableTokens()) {
            assertTableToken(tableTokens.get(count), each);
            count++;
        }
    }
    
    private void assertTableToken(final TableToken actual, final ExpectedTableToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Table tokens begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Table tokens original literals assertion error: "), actual.getOriginalLiterals(), is(expected.getOriginalLiterals()));
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
    
    private void assertIndexToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<IndexToken> indexToken = getIndexToken(actual);
        if (indexToken.isPresent()) {
            assertIndexToken(indexToken.get(), expected.getIndexToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Index token should not exist: "), expected.getIndexToken());
        }
    }
    
    private void assertIndexToken(final IndexToken actual, final ExpectedIndexToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Index token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Index token original literals assertion error: "), actual.getOriginalLiterals(), is(expected.getOriginalLiterals()));
        assertThat(assertMessage.getFullAssertMessage("Index token table name assertion error: "), actual.getTableName(), is(expected.getTableName()));
    }
    
    private Optional<IndexToken> getIndexToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof IndexToken) {
                return Optional.of((IndexToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertItemsToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<ItemsToken> itemsToken = getItemsToken(actual);
        if (itemsToken.isPresent()) {
            assertItemsToken(itemsToken.get(), expected.getItemsToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Items token should not exist: "), expected.getItemsToken());
        }
    }
    
    private void assertItemsToken(final ItemsToken actual, final ExpectedItemsToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Items token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Items token items assertion error: "), actual.getItems(), is(expected.getItems()));
    }
    
    private Optional<ItemsToken> getItemsToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof ItemsToken) {
                return Optional.of((ItemsToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertGeneratedKeyToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<GeneratedKeyToken> generatedKeyToken = getGeneratedKeyToken(actual);
        if (generatedKeyToken.isPresent()) {
            assertGeneratedKeyToken(generatedKeyToken.get(), expected.getGeneratedKeyToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Generated key token should not exist: "), expected.getGeneratedKeyToken());
        }
    }
    
    private void assertGeneratedKeyToken(final GeneratedKeyToken actual, final ExpectedGeneratedKeyToken expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Generated key token begin position assertion error: "), actual.getBeginPosition(), is(expected.getPlaceholderBeginPosition()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Generated key token begin position assertion error: "), actual.getBeginPosition(), is(expected.getLiteralBeginPosition()));
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
    
    private void assertMultipleInsertValuesToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<MultipleInsertValuesToken> multipleInsertValuesToken = getMultipleInsertValuesToken(actual);
        if (multipleInsertValuesToken.isPresent()) {
            assertMultipleInsertValuesToken(multipleInsertValuesToken.get(), expected.getMultipleInsertValuesToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Multiple insert values token should not exist: "), expected.getMultipleInsertValuesToken());
        }
    }
    
    private void assertMultipleInsertValuesToken(final MultipleInsertValuesToken actual, final ExpectedMultipleInsertValuesToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Multiple insert values token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Multiple insert values token values assertion error: "), actual.getValues(), is(expected.getValues()));
    }
    
    private Optional<MultipleInsertValuesToken> getMultipleInsertValuesToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof MultipleInsertValuesToken) {
                return Optional.of((MultipleInsertValuesToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertOrderByToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<OrderByToken> orderByToken = getOrderByToken(actual);
        if (orderByToken.isPresent()) {
            assertOrderByToken(orderByToken.get(), expected.getOrderByToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Order by token should not exist: "), expected.getOrderByToken());
        }
    }
    
    private void assertOrderByToken(final OrderByToken actual, final ExpectedOrderByToken expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("Order by token begin position assertion error: "), actual.getBeginPosition(), is(expected.getPlaceholderBeginPosition()));
        } else {
            assertThat(assertMessage.getFullAssertMessage("Order by token begin position assertion error: "), actual.getBeginPosition(), is(expected.getLiteralBeginPosition()));
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
    
    private void assertOffsetToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<OffsetToken> offsetToken = getOffsetToken(actual);
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertFalse(assertMessage.getFullAssertMessage("Offset token should not exist: "), offsetToken.isPresent());
            return;
        }
        if (offsetToken.isPresent()) {
            assertOffsetToken(offsetToken.get(), expected.getOffsetToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Offset token should not exist: "), expected.getOffsetToken());
        }
    }
    
    private void assertOffsetToken(final OffsetToken actual, final ExpectedOffsetToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Offset token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Offset token offset assertion error: "), actual.getOffset(), is(expected.getOffset()));
    }
    
    private Optional<OffsetToken> getOffsetToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof OffsetToken) {
                return Optional.of((OffsetToken) each);
            }
        }
        return Optional.absent();
    }
    
    private void assertRowCountToken(final List<SQLToken> actual, final ExpectedTokens expected) {
        Optional<RowCountToken> rowCountToken = getRowCountToken(actual);
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertFalse(assertMessage.getFullAssertMessage("Row count token should not exist: "), rowCountToken.isPresent());
            return;
        }
        if (rowCountToken.isPresent()) {
            assertRowCountToken(rowCountToken.get(), expected.getRowCountToken());
        } else {
            assertNull(assertMessage.getFullAssertMessage("Row count token should not exist: "), expected.getRowCountToken());
        }
    }
    
    private void assertRowCountToken(final RowCountToken actual, final ExpectedRowCountToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Row count token begin position assertion error: "), actual.getBeginPosition(), is(expected.getBeginPosition()));
        assertThat(assertMessage.getFullAssertMessage("Row count token row count assertion error: "), actual.getRowCount(), is(expected.getRowCount()));
    }
    
    private Optional<RowCountToken> getRowCountToken(final List<SQLToken> actual) {
        for (SQLToken each : actual) {
            if (each instanceof RowCountToken) {
                return Optional.of((RowCountToken) each);
            }
        }
        return Optional.absent();
    }
}
