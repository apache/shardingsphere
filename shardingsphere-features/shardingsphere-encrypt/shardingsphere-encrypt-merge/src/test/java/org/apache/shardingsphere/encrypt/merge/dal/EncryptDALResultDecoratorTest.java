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

package org.apache.shardingsphere.encrypt.merge.dal;

import org.apache.shardingsphere.encrypt.merge.dal.impl.DecoratedEncryptColumnsMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.impl.MergedEncryptColumnsMergedResult;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.DescribeStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.executor.sql.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptDALResultDecoratorTest {
    
    @Mock
    private ShardingSphereSchema schema;

    @Mock
    private EncryptRule rule;
    
    @Mock
    private SQLStatementContext<?> sqlStatementContext;
    
    @Test
    public void assertMergedResultWithDescribeStatement() {
        schema = mock(ShardingSphereSchema.class);
        sqlStatementContext = getDescribeStatementContext();
        EncryptDALResultDecorator encryptDALResultDecorator = new EncryptDALResultDecorator();
        assertThat(encryptDALResultDecorator.decorate(mock(QueryResult.class), sqlStatementContext, rule), instanceOf(MergedEncryptColumnsMergedResult.class));
        assertThat(encryptDALResultDecorator.decorate(mock(MergedResult.class), sqlStatementContext, rule), instanceOf(DecoratedEncryptColumnsMergedResult.class));
    }
    
    @Test
    public void assertMergedResultWithShowColumnsStatement() {
        schema = mock(ShardingSphereSchema.class);
        sqlStatementContext = getShowColumnsStatementContext();
        EncryptDALResultDecorator encryptDALResultDecorator = new EncryptDALResultDecorator();
        assertThat(encryptDALResultDecorator.decorate(mock(QueryResult.class), sqlStatementContext, rule), instanceOf(MergedEncryptColumnsMergedResult.class));
        assertThat(encryptDALResultDecorator.decorate(mock(MergedResult.class), sqlStatementContext, rule), instanceOf(DecoratedEncryptColumnsMergedResult.class));
    }
    
    @Test
    public void assertMergedResultWithOtherStatement() {
        schema = mock(ShardingSphereSchema.class);
        sqlStatementContext = mock(SQLStatementContext.class);
        EncryptDALResultDecorator encryptDALResultDecorator = new EncryptDALResultDecorator();
        assertThat(encryptDALResultDecorator.decorate(mock(QueryResult.class), sqlStatementContext, rule), instanceOf(TransparentMergedResult.class));
        assertThat(encryptDALResultDecorator.decorate(mock(MergedResult.class), sqlStatementContext, rule), instanceOf(MergedResult.class));
    }
    
    private SQLStatementContext<?> getDescribeStatementContext() {
        DescribeStatementContext result = mock(DescribeStatementContext.class);
        SimpleTableSegment simpleTableSegment = getSimpleTableSegment();
        when(result.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(result.getSqlStatement()).thenReturn(mock(MySQLDescribeStatement.class));
        return result;
    }
    
    private SQLStatementContext<?> getShowColumnsStatementContext() {
        ShowColumnsStatementContext result = mock(ShowColumnsStatementContext.class);
        SimpleTableSegment simpleTableSegment = getSimpleTableSegment();
        when(result.getAllTables()).thenReturn(Collections.singletonList(simpleTableSegment));
        when(result.getSqlStatement()).thenReturn(mock(MySQLShowColumnsStatement.class));
        return result;
    }
    
    private SimpleTableSegment getSimpleTableSegment() {
        IdentifierValue identifierValue = new IdentifierValue("test");
        TableNameSegment tableNameSegment = new TableNameSegment(1, 4, identifierValue);
        return new SimpleTableSegment(tableNameSegment);
    }
}
