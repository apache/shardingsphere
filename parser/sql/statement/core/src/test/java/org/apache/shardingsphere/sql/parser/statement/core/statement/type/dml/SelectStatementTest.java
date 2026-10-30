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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SelectStatementTest {
    
    @ParameterizedTest(name = "VALUES rows={0}")
    @ValueSource(ints = {0, 1, 2})
    void assertWithSubqueryTypePreservesValues(final int rowCount) {
        Collection<InsertValuesSegment> values = new ArrayList<>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            values.add(new InsertValuesSegment(i * 3, i * 3 + 2, Collections.singletonList(new LiteralExpressionSegment(i * 3 + 1, i * 3 + 1, i))));
        }
        SelectStatement.SelectStatementBuilder builder = SelectStatement.builder().databaseType(mock(DatabaseType.class)).projections(new ProjectionsSegment(0, 0));
        if (0 < rowCount) {
            builder.values(values);
        }
        SelectStatement actual = builder.build().withSubqueryType(SubqueryType.PROJECTION);
        assertThat(actual.getValues(), is(values));
        assertThat(actual.getSubqueryType().orElse(null), is(SubqueryType.PROJECTION));
    }
    
    @Test
    void assertWithSubqueryType() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(mock(DatabaseType.class)).projections(new ProjectionsSegment(0, 0)).intoVariables(true).build();
        FunctionSegment transformSegment = new FunctionSegment(0, 0, "TRANSFORM", "TRANSFORM(user_id)");
        selectStatement.getTransformSegments().add(transformSegment);
        SelectStatement actual = selectStatement.withSubqueryType(SubqueryType.TABLE);
        assertThat(actual.getSubqueryType().orElse(null), is(SubqueryType.TABLE));
        assertTrue(actual.isIntoVariables());
        assertThat(actual.getTransformSegments(), contains(transformSegment));
    }
}
