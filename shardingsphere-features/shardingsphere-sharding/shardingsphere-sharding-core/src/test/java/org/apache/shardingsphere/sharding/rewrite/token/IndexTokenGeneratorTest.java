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
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.IndexTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.IndexToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class IndexTokenGeneratorTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        CreateDatabaseStatementContext createDatabaseStatementContext = mock(CreateDatabaseStatementContext.class);
        IndexTokenGenerator indexTokenGenerator = new IndexTokenGenerator();
        assertFalse(indexTokenGenerator.isGenerateSQLToken(createDatabaseStatementContext));
        AlterIndexStatementContext alterIndexStatementContext = mock(AlterIndexStatementContext.class);
        Collection<IndexSegment> indexSegmentCollection = new LinkedList<>();
        when(alterIndexStatementContext.getIndexes()).thenReturn(indexSegmentCollection);
        assertFalse(indexTokenGenerator.isGenerateSQLToken(alterIndexStatementContext));
        indexSegmentCollection.add(mock(IndexSegment.class));
        assertTrue(indexTokenGenerator.isGenerateSQLToken(alterIndexStatementContext));
    }
    
    @Test
    public void assertGenerateSQLTokens() {
        IndexSegment indexSegment = mock(IndexSegment.class, RETURNS_DEEP_STUBS);
        int testStartIndex = 1;
        when(indexSegment.getStartIndex()).thenReturn(testStartIndex);
        int testStopIndex = 3;
        when(indexSegment.getStopIndex()).thenReturn(testStopIndex);
        IndexNameSegment indexNameSegment = new IndexNameSegment(testStartIndex, testStopIndex, mock(IdentifierValue.class));
        when(indexSegment.getIndexName()).thenReturn(indexNameSegment);
        Collection<IndexSegment> indexSegments = new LinkedList<>();
        indexSegments.add(indexSegment);
        AlterIndexStatementContext alterIndexStatementContext = mock(AlterIndexStatementContext.class, RETURNS_DEEP_STUBS);
        when(alterIndexStatementContext.getIndexes()).thenReturn(indexSegments);
        when(alterIndexStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(alterIndexStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        IndexTokenGenerator indexTokenGenerator = new IndexTokenGenerator();
        ShardingRule shardingRule = mock(ShardingRule.class);
        indexTokenGenerator.setShardingRule(shardingRule);
        indexTokenGenerator.setSchemas(mockSchemaMap());
        indexTokenGenerator.setDatabaseName("test");
        Collection<IndexToken> result = indexTokenGenerator.generateSQLTokens(alterIndexStatementContext);
        assertThat(result.size(), is(1));
        assertThat((new LinkedList<>(result)).get(0).getStartIndex(), is(testStartIndex));
    }
    
    private Map<String, ShardingSphereSchema> mockSchemaMap() {
        Map<String, ShardingSphereSchema> result = new HashMap<>(1, 1);
        result.put("test", mock(ShardingSphereSchema.class));
        return result;
    }
}
