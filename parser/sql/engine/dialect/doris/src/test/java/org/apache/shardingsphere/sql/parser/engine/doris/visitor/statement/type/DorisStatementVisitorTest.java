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

package org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DorisStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitCreateTemporaryTable() {
        CreateTableStatement statement = (CreateTableStatement) parse("CREATE TEMPORARY TABLE t_order (order_id INT)");
        assertTrue(statement.isTemporary());
    }
    
    @Test
    void assertVisitCreateTableWithoutTemporary() {
        CreateTableStatement statement = (CreateTableStatement) parse("CREATE TABLE t_order (order_id INT)");
        assertFalse(statement.isTemporary());
    }
    
    @Test
    void assertVisitDropTemporaryTable() {
        DropTableStatement statement = (DropTableStatement) parse("DROP TEMPORARY TABLE t_order");
        assertTrue(statement.isTemporary());
    }
    
    @Test
    void assertVisitDropTableWithoutTemporary() {
        DropTableStatement statement = (DropTableStatement) parse("DROP TABLE t_order");
        assertFalse(statement.isTemporary());
    }
    
    @Test
    void assertVisitAlterDatabaseSetDataQuotaWithUnit() {
        assertThat(getDataQuotaValue("ALTER DATABASE db SET DATA QUOTA 1GB"), is(1073741824L));
        assertThat(getDataQuotaValue("ALTER DATABASE db SET DATA QUOTA 1gb"), is(1073741824L));
        assertThat(getDataQuotaValue("ALTER DATABASE db SET DATA QUOTA 100G"), is(107374182400L));
        assertThat(getDataQuotaValue("ALTER DATABASE db SET DATA QUOTA 1P"), is(1125899906842624L));
        assertThat(getDataQuotaValue("ALTER DATABASE db SET DATA QUOTA 4k"), is(4096L));
        assertThat(getDataQuotaValue("ALTER DATABASE db SET DATA QUOTA 500B"), is(500L));
    }
    
    @Test
    void assertVisitAlterDatabaseSetDataQuotaWithoutUnit() {
        assertThat(getDataQuotaValue("ALTER DATABASE db SET DATA QUOTA 10995116277760"), is(10995116277760L));
    }
    
    @Test
    void assertVisitAlterDatabaseSetDataQuotaWithValueOutOfRange() {
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET DATA QUOTA 99999999999P"));
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET DATA QUOTA 99999999999999999999999"));
    }
    
    @Test
    void assertVisitAlterDatabaseSetDataQuotaWithInvalidValue() {
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET DATA QUOTA foo"));
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET DATA QUOTA 1XB"));
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET DATA QUOTA 1.5"));
    }
    
    @Test
    void assertVisitAlterDatabaseSetReplicaAndTransactionQuota() {
        AlterDatabaseStatement replicaStatement = (AlterDatabaseStatement) parse("ALTER DATABASE db SET REPLICA QUOTA 1024");
        assertThat(replicaStatement.getQuotaType().orElse(null), is("REPLICA"));
        assertThat(replicaStatement.getQuotaValue().orElse(null), is(1024L));
        AlterDatabaseStatement transactionStatement = (AlterDatabaseStatement) parse("ALTER DATABASE db SET TRANSACTION QUOTA 1000");
        assertThat(transactionStatement.getQuotaType().orElse(null), is("TRANSACTION"));
        assertThat(transactionStatement.getQuotaValue().orElse(null), is(1000L));
    }
    
    @Test
    void assertVisitAlterDatabaseSetReplicaAndTransactionQuotaWithUnit() {
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET REPLICA QUOTA 1G"));
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET TRANSACTION QUOTA 1G"));
        assertThrows(SQLParsingException.class, () -> parse("ALTER DATABASE db SET REPLICA QUOTA 1.5"));
    }
    
    @Test
    void assertVisitWorkloadKeywordUsedAsIdentifier() {
        assertDoesNotThrow(() -> parse("SELECT workload FROM t"));
        assertDoesNotThrow(() -> parse("SELECT t.workload FROM t"));
        assertDoesNotThrow(() -> parse("CREATE TABLE t (workload INT)"));
        assertDoesNotThrow(() -> parse("SELECT * FROM workload"));
    }
    
    private long getDataQuotaValue(final String sql) {
        AlterDatabaseStatement result = (AlterDatabaseStatement) parse(sql);
        assertThat(result.getQuotaType().orElse(null), is("DATA"));
        return result.getQuotaValue().orElse(0L);
    }
    
    private Object parse(final String sql) {
        return new SQLStatementVisitorEngine("Doris").visit(new SQLParserEngine("Doris", CACHE_OPTION).parse(sql, false));
    }
}
