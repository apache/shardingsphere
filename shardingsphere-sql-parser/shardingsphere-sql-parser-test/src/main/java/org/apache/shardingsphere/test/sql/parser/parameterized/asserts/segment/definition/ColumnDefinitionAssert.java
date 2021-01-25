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
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedColumnDefinition;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Column definition assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnDefinitionAssert {
    
    /**
     * Assert actual column definition segment is correct with expected column definition.
     * 
     * @param assertContext assert context
     * @param actual actual column definition segment
     * @param expected expected column definition
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ColumnDefinitionSegment actual, final ExpectedColumnDefinition expected) {
        assertThat(assertContext.getText("Column definition name assertion error: "), actual.getColumnName().getIdentifier().getValue(), is(expected.getColumn().getName()));
        assertThat(assertContext.getText("Column definition data type assertion error: "), actual.getDataType().getDataTypeName(), is(expected.getType()));
        assertThat(assertContext.getText("Column definition primary key assertion error: "), actual.isPrimaryKey(), is(expected.isPrimaryKey()));
        TableAssert.assertIs(assertContext, actual.getReferencedTables(), expected.getReferencedTables());
        assertThat(assertContext.getText("Column definition start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertContext.getText("Column definition stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
}
