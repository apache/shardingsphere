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

import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowColumnsMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowCreateTableMergedResult;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dal.ShowCreateTableStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptDALResultDecoratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private EncryptRule rule;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Test
    void assertMergedResultWithDescribeStatement() {
        sqlStatementContext = getDescribeStatementContext();
        EncryptDALResultDecorator encryptDALResultDecorator = new EncryptDALResultDecorator(mock(RuleMetaData.class));
        assertThat(encryptDALResultDecorator.decorate(mock(MergedResult.class), sqlStatementContext, rule), instanceOf(EncryptShowColumnsMergedResult.class));
    }
    
    @Test
    void assertMergedResultWithShowColumnsStatement() {
        sqlStatementContext = getShowColumnsStatementContext();
        EncryptDALResultDecorator encryptDALResultDecorator = new EncryptDALResultDecorator(mock(RuleMetaData.class));
        assertThat(encryptDALResultDecorator.decorate(mock(MergedResult.class), sqlStatementContext, rule), instanceOf(EncryptShowColumnsMergedResult.class));
    }
    
    @Test
    void assertMergedResultWithShowCreateTableStatement() {
        sqlStatementContext = getShowCreateTableStatementContext();
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getSingleRule(SQLParserRule.class)).thenReturn(mock(SQLParserRule.class));
        EncryptDALResultDecorator encryptDALResultDecorator = new EncryptDALResultDecorator(ruleMetaData);
        assertThat(encryptDALResultDecorator.decorate(mock(MergedResult.class), sqlStatementContext, rule), instanceOf(EncryptShowCreateTableMergedResult.class));
    }
    
    @Test
    void assertMergedResultWithOtherStatement() {
        sqlStatementContext = mock(SQLStatementContext.class);
        EncryptDALResultDecorator encryptDALResultDecorator = new EncryptDALResultDecorator(mock(RuleMetaData.class));
        assertThat(encryptDALResultDecorator.decorate(mock(MergedResult.class), sqlStatementContext, rule), instanceOf(MergedResult.class));
    }
    
    private SQLStatementContext getDescribeStatementContext() {
        ExplainStatementContext result = mock(ExplainStatementContext.class, RETURNS_DEEP_STUBS);
        SimpleTableSegment simpleTableSegment = getSimpleTableSegment();
        when(result.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        when(result.getSqlStatement()).thenReturn(mock(MySQLExplainStatement.class));
        return result;
    }
    
    private SQLStatementContext getShowColumnsStatementContext() {
        ShowColumnsStatementContext result = mock(ShowColumnsStatementContext.class, RETURNS_DEEP_STUBS);
        SimpleTableSegment simpleTableSegment = getSimpleTableSegment();
        when(result.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        when(result.getSqlStatement()).thenReturn(mock(MySQLShowColumnsStatement.class));
        return result;
    }
    
    private SQLStatementContext getShowCreateTableStatementContext() {
        ShowCreateTableStatementContext result = mock(ShowCreateTableStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getDatabaseType()).thenReturn(databaseType);
        SimpleTableSegment simpleTableSegment = getSimpleTableSegment();
        when(result.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        when(result.getSqlStatement()).thenReturn(new MySQLShowCreateTableStatement());
        return result;
    }
    
    private SimpleTableSegment getSimpleTableSegment() {
        IdentifierValue identifierValue = new IdentifierValue("test");
        TableNameSegment tableNameSegment = new TableNameSegment(1, 4, identifierValue);
        return new SimpleTableSegment(tableNameSegment);
    }
}
