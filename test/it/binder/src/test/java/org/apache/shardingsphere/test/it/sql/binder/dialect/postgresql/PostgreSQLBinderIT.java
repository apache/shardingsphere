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

package org.apache.shardingsphere.test.it.sql.binder.dialect.postgresql;

import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderIT;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderITSettings;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SQLBinderITSettings("PostgreSQL")
class PostgreSQLBinderIT extends SQLBinderIT {
    
    @Test
    void assertBindDoesNotShadowSystemCatalogTableWithCurrentSchemaTable() {
        String sql = "SELECT relname FROM pg_class";
        SelectStatement actual = (SelectStatement) bindSQLStatement("PostgreSQL", sql);
        ProjectionSegment actualProjection = actual.getProjections().getProjections().get(0);
        ColumnSegmentBoundInfo actualColumnBoundInfo = ((ColumnProjectionSegment) actualProjection).getColumn().getColumnBoundInfo();
        assertThat(actualColumnBoundInfo.getOriginalTable().getValue(), is("pg_class"));
        assertThat(actualColumnBoundInfo.getOriginalSchema().getValue(), is("pg_catalog"));
    }
    
    @Test
    void assertBindCreateTableNotShadowedBySystemCatalogTableName() {
        String sql = "CREATE TABLE pg_extension (foo_col text)";
        CreateTableStatement actual = (CreateTableStatement) bindSQLStatement("PostgreSQL", sql);
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("public"));
    }
    
    @Test
    void assertBindQuotedSystemCatalogTableNameStillMatchesDictionary() {
        String sql = "SELECT relname FROM \"pg_class\"";
        SelectStatement actual = (SelectStatement) bindSQLStatement("PostgreSQL", sql);
        ProjectionSegment actualProjection = actual.getProjections().getProjections().get(0);
        ColumnSegmentBoundInfo actualColumnBoundInfo = ((ColumnProjectionSegment) actualProjection).getColumn().getColumnBoundInfo();
        assertThat(actualColumnBoundInfo.getOriginalTable().getValue(), is("pg_class"));
        assertThat(actualColumnBoundInfo.getOriginalSchema().getValue(), is("pg_catalog"));
    }
    
    @Test
    void assertBindCreateIndexOnExistingSystemCatalogTableStillResolvesToDictionary() {
        String sql = "CREATE INDEX idx_pg_extension_extname ON pg_extension (extname)";
        CreateIndexStatement actual = (CreateIndexStatement) bindSQLStatement("PostgreSQL", sql);
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("pg_catalog"));
    }
    
    @Test
    void assertBindOwnerQualifiedQuotedIdentifierDoesNotMatchDictionaryView() {
        String sql = "SELECT relname FROM pg_catalog.\"PG_CLASS\"";
        assertThrows(TableNotFoundException.class, () -> bindSQLStatement("PostgreSQL", sql));
    }
    
    @Test
    void assertBindOwnerQualifiedQuotedIdentifierMatchesDictionaryViewWithExactCase() {
        String sql = "SELECT relname FROM pg_catalog.\"pg_class\"";
        SelectStatement actual = (SelectStatement) bindSQLStatement("PostgreSQL", sql);
        ProjectionSegment actualProjection = actual.getProjections().getProjections().get(0);
        ColumnSegmentBoundInfo actualColumnBoundInfo = ((ColumnProjectionSegment) actualProjection).getColumn().getColumnBoundInfo();
        assertThat(actualColumnBoundInfo.getOriginalTable().getValue(), is("pg_class"));
        assertThat(actualColumnBoundInfo.getOriginalSchema().getValue(), is("pg_catalog"));
    }
    
    @Test
    void assertBindRenameTargetNotShadowedBySystemCatalogTableName() {
        String sql = "ALTER TABLE t_order RENAME TO pg_extension";
        AlterTableStatement actual = (AlterTableStatement) bindSQLStatement("PostgreSQL", sql);
        assertThat(actual.getRenameTable().get().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("public"));
    }
}
