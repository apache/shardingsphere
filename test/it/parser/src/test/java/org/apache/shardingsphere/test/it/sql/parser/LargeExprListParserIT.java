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

package org.apache.shardingsphere.test.it.sql.parser;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class LargeExprListParserIT {
    
    private static final int LIST_SIZE = 10240;
    
    private static final long SMALL_STACK_SIZE = 512L * 1024L;
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"openGauss", "PostgreSQL"})
    void assertParseLargeInList(final String databaseType) {
        SQLStatement actual = parseWithSmallStack(databaseType, "SELECT COUNT(1) FROM t WHERE col IN (" + createPlaceholders() + ")");
        assertThat(actual, instanceOf(SelectStatement.class));
        InExpression inExpression = (InExpression) ((SelectStatement) actual).getWhere().orElseThrow(IllegalStateException::new).getExpr();
        assertThat(inExpression.getExpressionList().size(), is(LIST_SIZE));
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"openGauss", "PostgreSQL"})
    void assertParseLargeInsertValues(final String databaseType) {
        SQLStatement actual = parseWithSmallStack(databaseType, "INSERT INTO t VALUES (" + createPlaceholders() + ")");
        assertThat(actual, instanceOf(InsertStatement.class));
        InsertValuesSegment insertValues = ((InsertStatement) actual).getValues().iterator().next();
        assertThat(insertValues.getValues().size(), is(LIST_SIZE));
    }
    
    private String createPlaceholders() {
        StringBuilder result = new StringBuilder(LIST_SIZE * 2);
        for (int i = 0; i < LIST_SIZE; i++) {
            result.append(0 == i ? "?" : ",?");
        }
        return result.toString();
    }
    
    private SQLStatement parseWithSmallStack(final String databaseType, final String sql) {
        AtomicReference<SQLStatement> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(null, () -> {
            try {
                DatabaseType type = TypedSPILoader.getService(DatabaseType.class, databaseType);
                result.set(new SQLStatementVisitorEngine(type).visit(new SQLParserEngine(type, new CacheOption(128, 1024L)).parse(sql, false)));
            } catch (final Throwable ex) {
                failure.set(ex);
            }
        }, "large-expr-list-parser", SMALL_STACK_SIZE);
        thread.start();
        try {
            thread.join();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        assertThat(failure.get(), nullValue());
        return result.get();
    }
}
