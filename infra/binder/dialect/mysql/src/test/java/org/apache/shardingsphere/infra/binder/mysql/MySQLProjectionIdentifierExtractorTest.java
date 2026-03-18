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

package org.apache.shardingsphere.infra.binder.mysql;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.DialectProjectionIdentifierExtractor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class MySQLProjectionIdentifierExtractorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectProjectionIdentifierExtractor extractor = DatabaseTypedSPILoader.getService(DialectProjectionIdentifierExtractor.class, databaseType);
    
    @Test
    void assertGetIdentifierValue() {
        assertThat(extractor.getIdentifierValue(new IdentifierValue("foo_id")), is("foo_id"));
    }
    
    @Test
    void assertGetColumnNameFromFunction() {
        assertThat(extractor.getColumnNameFromFunction("COUNT", "COUNT(*)"), is("COUNT(*)"));
    }
    
    @Test
    void assertGetColumnNameFromExpression() {
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "expression")), is("expression"));
    }
    
    @Test
    void assertGetColumnNameFromSubqueryWithoutTruncation() {
        String text = "SELECT id FROM subquery";
        assertThat(extractor.getColumnNameFromSubquery(new SubqueryProjectionSegment(mock(SubquerySegment.class), text)), is(text));
    }
    
    @Test
    void assertGetColumnNameFromSubqueryWithTruncation() {
        String text = "SELECT id FROM subquery WHERE column_name > 100 AND another_column LIKE '%some_long_text_value%' AND condition_3 = true"
                + " AND condition_4 = false AND condition_5 IS NOT NULL AND condition_6 BETWEEN 1 AND 1000 AND condition_7 IN "
                + "(SELECT id FROM another_table WHERE description LIKE '%even_more_detailed_description_text%' AND status = 'active' AND created_at > '2023-01-01')";
        assertThat(extractor.getColumnNameFromSubquery(new SubqueryProjectionSegment(mock(SubquerySegment.class), text)), is(text.substring(0, 255)));
    }
}
