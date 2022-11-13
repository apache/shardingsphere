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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.definition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.definition.ExpectedConstraintDefinition;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Constraint definition assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstraintDefinitionAssert {
    
    /**
     * Assert actual constraint definition segment is correct with expected constraint definition.
     * 
     * @param assertContext assert context
     * @param actual actual constraint definition segment
     * @param expected expected constraint definition
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ConstraintDefinitionSegment actual, final ExpectedConstraintDefinition expected) {
        int count = 0;
        for (ColumnSegment each : actual.getPrimaryKeyColumns()) {
            ColumnAssert.assertIs(assertContext, each, expected.getPrimaryKeyColumns().get(count));
            count++;
        }
        assertThat(assertContext.getText("Constraint definition index column size assertion error: "), actual.getIndexColumns().size(), is(expected.getIndexColumns().size()));
        int indexCount = 0;
        for (ColumnSegment each : actual.getIndexColumns()) {
            ColumnAssert.assertIs(assertContext, each, expected.getIndexColumns().get(indexCount));
            indexCount++;
        }
        if (null == expected.getConstraintName()) {
            assertFalse(assertContext.getText("Actual constraint name should not exist."), actual.getConstraintName().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual constraint name should exist."), actual.getConstraintName().isPresent());
            assertThat(assertContext.getText("Actual constraint name assertion error."), actual.getConstraintName().get().getIdentifier().getValue(), is(expected.getConstraintName()));
        }
        if (null == expected.getIndexName()) {
            assertFalse(assertContext.getText("Actual index name should not exist."), actual.getIndexName().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual index name should exist."), actual.getIndexName().isPresent());
            assertThat(assertContext.getText("Actual index name assertion error."), actual.getIndexName().get().getIndexName().getIdentifier().getValue(), is(expected.getIndexName()));
        }
        if (null == expected.getReferencedTable()) {
            assertFalse(assertContext.getText("Actual referenced table should not exist."), actual.getReferencedTable().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual referenced table should exist."), actual.getReferencedTable().isPresent());
            TableAssert.assertIs(assertContext, actual.getReferencedTable().get(), expected.getReferencedTable());
        }
        assertThat(assertContext.getText("Constraint definition start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertContext.getText("Constraint definition stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
}
