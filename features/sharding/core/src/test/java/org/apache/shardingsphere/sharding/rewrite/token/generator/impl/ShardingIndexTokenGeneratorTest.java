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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.IndexToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingIndexTokenGeneratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType postgresqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingIndexTokenGenerator generator = new ShardingIndexTokenGenerator(mock(ShardingRule.class));
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotIndexContextAvailable() {
        AlterIndexStatement sqlStatement = new AlterIndexStatement(databaseType);
        sqlStatement.buildAttributes();
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyIndex() {
        AlterIndexStatement sqlStatement = new AlterIndexStatement(postgresqlDatabaseType);
        sqlStatement.buildAttributes();
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        AlterIndexStatement sqlStatement = new AlterIndexStatement(postgresqlDatabaseType);
        sqlStatement.setIndex(mock(IndexSegment.class));
        sqlStatement.buildAttributes();
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokensWithNotIndexContextAvailable() {
        AlterIndexStatement sqlStatement = new AlterIndexStatement(databaseType);
        sqlStatement.buildAttributes();
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensWithSchemaOwner() throws ReflectiveOperationException {
        IndexSegment indexSegment = new IndexSegment(1, 3, new IndexNameSegment(1, 3, mock(IdentifierValue.class)));
        indexSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_schema")));
        CommonSQLStatementContext sqlStatementContext = mockAlterIndexStatementContext(indexSegment);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        generator.setSchemas(Collections.singletonMap("foo_schema", schema));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertTokens(actual, schema);
    }
    
    @Test
    void assertGenerateSQLTokensWithoutSchemaOwner() throws ReflectiveOperationException {
        IndexSegment indexSegment = new IndexSegment(1, 3, new IndexNameSegment(1, 3, mock(IdentifierValue.class)));
        CommonSQLStatementContext sqlStatementContext = mockAlterIndexStatementContext(indexSegment);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        generator.setDefaultSchema(schema);
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertTokens(actual, schema);
    }
    
    private CommonSQLStatementContext mockAlterIndexStatementContext(final IndexSegment indexSegment) {
        AlterIndexStatement sqlStatement = new AlterIndexStatement(databaseType);
        sqlStatement.setIndex(indexSegment);
        sqlStatement.buildAttributes();
        return new CommonSQLStatementContext(sqlStatement);
    }
    
    private void assertTokens(final Collection<SQLToken> actual, final ShardingSphereSchema schema) throws ReflectiveOperationException {
        assertThat(actual.size(), is(1));
        IndexToken actualToken = (IndexToken) new ArrayList<>(actual).get(0);
        assertThat(actualToken.getStartIndex(), is(1));
        assertThat(actualToken.getStopIndex(), is(3));
        assertThat(schema, is((ShardingSphereSchema) Plugins.getMemberAccessor().get(IndexToken.class.getDeclaredField("schema"), actualToken)));
    }
}
