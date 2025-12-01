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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class ProjectionIdentifierExtractEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetIdentifierValueWithQuoteIdentifier() {
        assertThat(new ProjectionIdentifierExtractEngine(databaseType).getIdentifierValue(new IdentifierValue("Data", QuoteCharacter.QUOTE)), is("Data"));
    }
    
    @Test
    void assertGetIdentifierValueWithNoneIdentifier() {
        assertThat(new ProjectionIdentifierExtractEngine(databaseType).getIdentifierValue(new IdentifierValue("Data", QuoteCharacter.NONE)), is("Data"));
    }
    
    @Test
    void assertGetColumnNameFromFunction() {
        assertThat(new ProjectionIdentifierExtractEngine(databaseType).getColumnNameFromFunction("Function", "FunctionExpression"), is("FunctionExpression"));
    }
    
    @Test
    void assertGetColumnNameFromExpression() {
        assertThat(new ProjectionIdentifierExtractEngine(databaseType).getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "expression")), is("expression"));
    }
    
    @Test
    void assertGetColumnNameFromSubquery() {
        assertThat(new ProjectionIdentifierExtractEngine(databaseType).getColumnNameFromSubquery(new SubqueryProjectionSegment(mock(SubquerySegment.class), "text")), is("text"));
    }
}
