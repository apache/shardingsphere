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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.definition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedConstraintDefinition;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
        if (null != expected.getIndexName()) {
            assertNotNull(assertContext.getText("Actual index name should exist."), actual.getIndexName());
            assertThat(assertContext.getText("Actual index name assertion error."), actual.getIndexName().getIdentifier().getValue(), is(expected.getIndexName()));
        } else {
            assertNull(assertContext.getText("Actual index name should not exist."), actual.getIndexName());
        }
        
        if (null != expected.getReferencedTable()) {
            assertTrue(assertContext.getText("Actual referenced table should exist."), actual.getReferencedTable().isPresent());
            TableAssert.assertIs(assertContext, actual.getReferencedTable().get(), expected.getReferencedTable());
        } else {
            assertFalse(assertContext.getText("Actual referenced table should not exist."), actual.getReferencedTable().isPresent());
        }
        assertThat(assertContext.getText("Constraint definition start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertContext.getText("Constraint definition stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
}
