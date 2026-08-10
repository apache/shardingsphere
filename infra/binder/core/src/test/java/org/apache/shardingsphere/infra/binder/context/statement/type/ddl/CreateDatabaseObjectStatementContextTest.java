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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class CreateDatabaseObjectStatementContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstanceWithTriggerTable() {
        CreateTriggerStatement statement = new CreateTriggerStatement(databaseType);
        statement.setTable(new SimpleTableSegment(new TableNameSegment(28, 36, new IdentifierValue("t_account"))));
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(statement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getSqlStatement(), is(statement));
        assertThat(actual.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue(), is("t_account"));
    }
    
    @Test
    void assertNewInstanceWithProcedure() {
        CreateProcedureStatement sqlStatement = mock(CreateProcedureStatement.class);
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(sqlStatement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
    
    @Test
    void assertNewInstanceWithProcedureInternalSQLTablesContext() {
        CreateProcedureStatement sqlStatement = new CreateProcedureStatement(databaseType);
        sqlStatement.getSqlStatements().add(new SQLStatementSegment(0, 0, new TableAwareSQLStatement("t_encrypt")));
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(sqlStatement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("t_encrypt")));
    }
    
    private static final class TableAwareSQLStatement extends SQLStatement {
        
        private final SQLStatementAttributes attributes;
        
        private TableAwareSQLStatement(final String tableName) {
            super(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
            attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(new SimpleTableSegment(new TableNameSegment(0, tableName.length(), new IdentifierValue(tableName)))));
        }
        
        @Override
        public SQLStatementAttributes getAttributes() {
            return attributes;
        }
    }
}
