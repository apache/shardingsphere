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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class PipelinePrepareSQLBuilderTest {
    
    private final PipelinePrepareSQLBuilder sqlBuilder = new PipelinePrepareSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"),
            new DatabaseIdentifierContext(IdentifierCasePolicyResolver.resolveProtocol(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"))));
    
    @Test
    void assertBuildCreateSchemaSQL() {
        assertFalse(sqlBuilder.buildCreateSchemaSQL("foo_schema").isPresent());
    }
    
    @Test
    void assertBuildDropSQL() {
        assertThat(sqlBuilder.buildDropSQL("foo_schema", "foo_tbl"), is("DROP TABLE IF EXISTS foo_tbl"));
    }
    
    @Test
    void assertBuildDropSQLWithActualIdentifiers() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        when((Object) DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType))
                .thenReturn(TypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType));
        when((Object) DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType)).thenReturn(mock(DialectPipelineSQLBuilder.class));
        PipelinePrepareSQLBuilder builder = new PipelinePrepareSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        assertThat(builder.buildDropSQL("Foo_SCHEMA", "T_Order"), is("DROP TABLE IF EXISTS \"Foo_SCHEMA\".\"T_Order\""));
    }
    
    @Test
    void assertBuildCountSQL() {
        assertThat(sqlBuilder.buildCountSQL("foo_schema", "foo_tbl"), is("SELECT COUNT(*) FROM foo_tbl"));
    }
    
    @Test
    void assertBuildEstimatedCountSQL() {
        assertFalse(sqlBuilder.buildEstimatedCountSQL("foo_catalog", "foo_schema", "foo_tbl").isPresent());
    }
    
    @Test
    void assertBuildUniqueKeyMinMaxValuesSQL() {
        assertThat(sqlBuilder.buildUniqueKeyMinMaxValuesSQL("foo_schema", "foo_tbl", "foo_key"), is("SELECT MIN(foo_key), MAX(foo_key) FROM foo_tbl"));
    }
    
    @Test
    void assertBuildCheckEmptyTableSQL() {
        assertThat(sqlBuilder.buildCheckEmptyTableSQL("foo_schema", "foo_tbl"), is("SELECT * FROM foo_tbl LIMIT 1"));
    }
}
