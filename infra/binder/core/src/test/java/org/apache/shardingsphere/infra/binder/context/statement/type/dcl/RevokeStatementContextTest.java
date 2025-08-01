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

package org.apache.shardingsphere.infra.binder.context.statement.type.dcl;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevokeStatementContextTest {
    
    @Mock
    private DatabaseType databaseType;
    
    @Test
    void assertNewInstance() {
        RevokeStatement revokeStatement = new RevokeStatement(databaseType);
        RevokeStatementContext actual = new RevokeStatementContext(revokeStatement);
        assertThat(actual, instanceOf(RevokeStatementContext.class));
        assertThat(actual.getSqlStatement(), instanceOf(RevokeStatement.class));
    }
    
    @Test
    void assertGetTablesContextWithoutTable() {
        RevokeStatement revokeStatement = new RevokeStatement(databaseType);
        RevokeStatementContext actual = new RevokeStatementContext(revokeStatement);
        assertTrue(actual.getTablesContext().getSimpleTables().isEmpty());
    }
    
    @Test
    void assertGetTablesContextWithTable() {
        RevokeStatement revokeStatement = new RevokeStatement(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        SimpleTableSegment tableSegment = new SimpleTableSegment(tableNameSegment);
        revokeStatement.getTables().add(tableSegment);
        RevokeStatementContext actual = new RevokeStatementContext(revokeStatement);
        assertFalse(actual.getTablesContext().getSimpleTables().isEmpty());
    }
}
