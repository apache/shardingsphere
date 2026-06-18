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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropEncryptKeyStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.DropEncryptKeyStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Drop encrypt key statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropEncryptKeyStatementAssert {
    
    /**
     * Assert drop encrypt key statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual drop encrypt key statement
     * @param expected expected drop encrypt key statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DropEncryptKeyStatement actual, final DropEncryptKeyStatementTestCase expected) {
        assertIfExists(assertContext, actual, expected);
        assertKeyName(assertContext, actual, expected);
    }
    
    private static void assertIfExists(final SQLCaseAssertContext assertContext, final DropEncryptKeyStatement actual, final DropEncryptKeyStatementTestCase expected) {
        assertThat(assertContext.getText("IF EXISTS assertion error: "), actual.isIfExists(), is(expected.isIfExists()));
    }
    
    private static void assertKeyName(final SQLCaseAssertContext assertContext, final DropEncryptKeyStatement actual, final DropEncryptKeyStatementTestCase expected) {
        if (null != expected.getKeyName()) {
            assertNotNull(actual.getKeyName(), assertContext.getText("Key name should not be null"));
            assertThat(assertContext.getText("Key name assertion error: "), actual.getKeyName().getIdentifier().getValue(), is(expected.getKeyName().getName()));
            SQLSegmentAssert.assertIs(assertContext, actual.getKeyName(), expected.getKeyName());
            if (null != expected.getKeyName().getOwner()) {
                assertNotNull(actual.getKeyName().getOwner().orElse(null), assertContext.getText("Owner should not be null"));
                assertThat(assertContext.getText("Owner name assertion error: "), actual.getKeyName().getOwner().get().getIdentifier().getValue(), is(expected.getKeyName().getOwner().getName()));
                SQLSegmentAssert.assertIs(assertContext, actual.getKeyName().getOwner().get(), expected.getKeyName().getOwner());
            }
        }
    }
}
