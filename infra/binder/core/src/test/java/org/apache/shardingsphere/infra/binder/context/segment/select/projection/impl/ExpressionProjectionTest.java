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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionProjectionTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetColumnNameWithAlias() {
        assertThat(new ExpressionProjection(new ExpressionProjectionSegment(0, 0, "text"), new IdentifierValue("alias"), databaseType).getColumnName(), is("alias"));
    }
    
    @Test
    void assertGetColumnNameWithoutAlias() {
        assertThat(new ExpressionProjection(new ExpressionProjectionSegment(0, 0, "text"), null, databaseType).getColumnName(), is("text"));
    }
    
    @Test
    void assertGetExpression() {
        assertThat(new ExpressionProjection(new ExpressionProjectionSegment(0, 0, "text"), null, databaseType).getExpression(), is("text"));
    }
}
