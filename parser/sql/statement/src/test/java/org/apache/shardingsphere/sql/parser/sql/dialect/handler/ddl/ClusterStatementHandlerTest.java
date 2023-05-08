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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.ClusterStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLClusterStatement;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ClusterStatementHandlerTest {
    
    @Test
    void assertGetSimpleTableSegmentWithTableSegment() {
        PostgreSQLClusterStatement statement = new PostgreSQLClusterStatement();
        statement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        Optional<SimpleTableSegment> actual = ClusterStatementHandler.getSimpleTableSegment(statement);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertGetSimpleTableSegmentWithoutTableSegment() {
        ClusterStatement statement = mock(ClusterStatement.class);
        Optional<SimpleTableSegment> actual = ClusterStatementHandler.getSimpleTableSegment(statement);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetIndexSegmentWithIndexSegment() {
        PostgreSQLClusterStatement statement = new PostgreSQLClusterStatement();
        statement.setIndex(new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue(""))));
        Optional<IndexSegment> actual = ClusterStatementHandler.getIndexSegment(statement);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertGetIndexSegmentWithoutIndexSegment() {
        ClusterStatement statement = mock(ClusterStatement.class);
        Optional<IndexSegment> actual = ClusterStatementHandler.getIndexSegment(statement);
        assertFalse(actual.isPresent());
    }
}
