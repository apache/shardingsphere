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
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ColumnProjectionTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetOwner() {
        assertThat(new ColumnProjection("owner", "name", "alias", databaseType).getOwner(), is(Optional.of(new IdentifierValue("owner"))));
    }
    
    @Test
    void assertGetOriginalTableWithNullOriginalTableAndWithoutOwner() {
        assertThat(new ColumnProjection(null, "name", "alias", databaseType).getOriginalTable(), is(new IdentifierValue("")));
    }
    
    @Test
    void assertGetOriginalTableWithEmptyOriginalTableAndWithOwner() {
        ColumnProjection projection = new ColumnProjection(new IdentifierValue("owner"), new IdentifierValue("name"), new IdentifierValue("alias"), databaseType,
                null, null, new ColumnSegmentBoundInfo(new IdentifierValue("")));
        assertThat(projection.getOriginalTable(), is(new IdentifierValue("owner")));
    }
    
    @Test
    void assertGetOriginalTable() {
        ColumnProjection projection = new ColumnProjection(new IdentifierValue("owner"), new IdentifierValue("name"), new IdentifierValue("alias"), databaseType,
                null, null, new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(null, null), new IdentifierValue("tbl"), new IdentifierValue(""), TableSourceType.PHYSICAL_TABLE));
        assertThat(projection.getOriginalTable(), is(new IdentifierValue("tbl")));
    }
    
    @Test
    void assertGetOriginalColumnWithNullOriginalColumn() {
        assertThat(new ColumnProjection(null, "name", "alias", databaseType).getOriginalColumn(), is(new IdentifierValue("name")));
    }
    
    @Test
    void assertGetOriginalColumnWithEmptyOriginalColumn() {
        ColumnProjection projection = new ColumnProjection(null, new IdentifierValue("name"), new IdentifierValue("alias"), databaseType,
                null, null, new ColumnSegmentBoundInfo(new IdentifierValue("")));
        assertThat(projection.getOriginalColumn(), is(new IdentifierValue("name")));
    }
    
    @Test
    void assertGetOriginalColumn() {
        ColumnProjection projection = new ColumnProjection(null, new IdentifierValue("name"), new IdentifierValue("alias"),
                databaseType, null, null, new ColumnSegmentBoundInfo(new IdentifierValue("col")));
        assertThat(projection.getOriginalColumn(), is(new IdentifierValue("col")));
    }
    
    @Test
    void assertGetLeftParentheses() {
        assertFalse(new ColumnProjection(new IdentifierValue("owner"), new IdentifierValue("name"), new IdentifierValue("alias"), databaseType).getLeftParentheses().isPresent());
    }
    
    @Test
    void assertGetRightParentheses() {
        assertFalse(new ColumnProjection(new IdentifierValue("owner"), new IdentifierValue("name"), new IdentifierValue("alias"), databaseType).getRightParentheses().isPresent());
    }
    
    @Test
    void assertGetColumnNameWithAlias() {
        assertThat(new ColumnProjection(null, "name", "alias", databaseType).getColumnName(), is("alias"));
    }
    
    @Test
    void assertGetColumnNameWithoutAlias() {
        assertThat(new ColumnProjection(null, "name", null, databaseType).getColumnName(), is("name"));
    }
    
    @Test
    void assertGetExpressionWithoutOwner() {
        assertThat(new ColumnProjection(null, "name", null, databaseType).getExpression(), is("name"));
    }
    
    @Test
    void assertGetExpressionWithOwner() {
        assertThat(new ColumnProjection("owner", "name", null, databaseType).getExpression(), is("owner.name"));
    }
}
