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

package org.apache.shardingsphere.infra.rewrite.mysql;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.generic.DialectToBeRemovedSegmentsProvider;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.index.MySQLShowIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class MySQLToBeRemovedSegmentsProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectToBeRemovedSegmentsProvider provider = DatabaseTypedSPILoader.getService(DialectToBeRemovedSegmentsProvider.class, databaseType);
    
    @Test
    void assertGetToBeRemovedSQLSegmentsFromShowTables() {
        FromDatabaseSegment fromDatabase = createFromDatabaseSegment();
        MySQLShowTablesStatement statement = new MySQLShowTablesStatement(mock(DatabaseType.class), fromDatabase, null, false);
        assertThat(provider.getToBeRemovedSQLSegments(statement), contains(fromDatabase));
    }
    
    @Test
    void assertGetToBeRemovedSQLSegmentsWhenShowColumnsWithoutFromDatabase() {
        MySQLShowColumnsStatement statement = new MySQLShowColumnsStatement(mock(DatabaseType.class), createSimpleTableSegment(), null, null);
        assertThat(provider.getToBeRemovedSQLSegments(statement), is(empty()));
    }
    
    @Test
    void assertGetToBeRemovedSQLSegmentsFromShowIndex() {
        FromDatabaseSegment fromDatabase = createFromDatabaseSegment();
        MySQLShowIndexStatement statement = new MySQLShowIndexStatement(mock(DatabaseType.class), createSimpleTableSegment(), fromDatabase);
        assertThat(provider.getToBeRemovedSQLSegments(statement), contains(fromDatabase));
    }
    
    @Test
    void assertGetToBeRemovedSQLSegmentsFromShowTableStatus() {
        FromDatabaseSegment fromDatabase = createFromDatabaseSegment();
        MySQLShowTableStatusStatement statement = new MySQLShowTableStatusStatement(mock(DatabaseType.class), fromDatabase, null);
        assertThat(provider.getToBeRemovedSQLSegments(statement), contains(fromDatabase));
    }
    
    private FromDatabaseSegment createFromDatabaseSegment() {
        return new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue("db")));
    }
    
    private SimpleTableSegment createSimpleTableSegment() {
        return new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl")));
    }
}
