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

package org.apache.shardingsphere.transaction.util;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AutoCommitUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNeedStartTransactionForSelectStatement() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        assertFalse(AutoCommitUtils.needStartTransaction(selectStatement));
        selectStatement.setFrom(mock(SimpleTableSegment.class));
        assertTrue(AutoCommitUtils.needStartTransaction(selectStatement));
    }
    
    @Test
    void assertNeedStartTransactionForDDLOrDMLStatement() {
        CreateTableStatement sqlStatement = new CreateTableStatement(databaseType);
        sqlStatement.setIfNotExists(true);
        assertTrue(AutoCommitUtils.needStartTransaction(sqlStatement));
        assertTrue(AutoCommitUtils.needStartTransaction(new InsertStatement(databaseType)));
    }
    
    @Test
    void assertNeedStartTransactionForOtherStatement() {
        assertFalse(AutoCommitUtils.needStartTransaction(mock(RegisterStorageUnitStatement.class)));
    }
}
