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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateDatabaseStatementContext;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.IndexTokenGenerator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndexTokenGeneratorTest {
    
    @Test
    void assertIsGenerateSQLToken() {
        CreateDatabaseStatementContext createDatabaseStatementContext = mock(CreateDatabaseStatementContext.class);
        IndexTokenGenerator generator = new IndexTokenGenerator();
        assertFalse(generator.isGenerateSQLToken(createDatabaseStatementContext));
        AlterIndexStatementContext alterIndexStatementContext = mock(AlterIndexStatementContext.class);
        Collection<IndexSegment> indexSegments = new LinkedList<>();
        when(alterIndexStatementContext.getIndexes()).thenReturn(indexSegments);
        assertFalse(generator.isGenerateSQLToken(alterIndexStatementContext));
        indexSegments.add(mock(IndexSegment.class));
        assertTrue(generator.isGenerateSQLToken(alterIndexStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokens() {
        IndexSegment indexSegment = mock(IndexSegment.class, RETURNS_DEEP_STUBS);
        when(indexSegment.getStartIndex()).thenReturn(1);
        when(indexSegment.getStopIndex()).thenReturn(3);
        when(indexSegment.getIndexName()).thenReturn(new IndexNameSegment(1, 3, mock(IdentifierValue.class)));
        AlterIndexStatementContext alterIndexStatementContext = mock(AlterIndexStatementContext.class, RETURNS_DEEP_STUBS);
        when(alterIndexStatementContext.getIndexes()).thenReturn(Collections.singleton(indexSegment));
        when(alterIndexStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(alterIndexStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        IndexTokenGenerator generator = new IndexTokenGenerator();
        generator.setShardingRule(mock(ShardingRule.class));
        generator.setSchemas(Collections.singletonMap("test", mock(ShardingSphereSchema.class)));
        generator.setDatabaseName("test");
        Collection<SQLToken> actual = generator.generateSQLTokens(alterIndexStatementContext);
        assertThat(actual.size(), is(1));
        assertThat((new LinkedList<>(actual)).get(0).getStartIndex(), is(1));
    }
}
