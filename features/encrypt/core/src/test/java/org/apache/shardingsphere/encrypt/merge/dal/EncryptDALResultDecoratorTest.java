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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowColumnsMergedResult;
import org.apache.shardingsphere.encrypt.merge.dal.show.EncryptShowCreateTableMergedResult;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ColumnInResultSetSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableInResultSetSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptDALResultDecoratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    public void setup() {
        when(database.getRuleMetaData().findSingleRule(EncryptRule.class)).thenReturn(Optional.ofNullable(mock(EncryptRule.class)));
    }
    
    @Test
    void assertMergedResultWithShowColumnsStatement() {
        sqlStatementContext = mockColumnInResultSetSQLStatementAttributeContext();
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        EncryptDALResultDecorator decorator = new EncryptDALResultDecorator(database, mock(ShardingSphereMetaData.class));
        assertThat(decorator.decorate(mock(MergedResult.class), queryContext), isA(EncryptShowColumnsMergedResult.class));
    }
    
    @Test
    void assertMergedResultWithShowCreateTableStatement() {
        sqlStatementContext = mockTableInfoInResultSetAvailableStatementContext();
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getSingleRule(SQLParserRule.class)).thenReturn(mock(SQLParserRule.class));
        when(metaData.getGlobalRuleMetaData()).thenReturn(ruleMetaData);
        EncryptDALResultDecorator decorator = new EncryptDALResultDecorator(database, metaData);
        assertThat(decorator.decorate(mock(MergedResult.class), queryContext), isA(EncryptShowCreateTableMergedResult.class));
    }
    
    @Test
    void assertMergedResultWithOtherStatement() {
        sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        EncryptDALResultDecorator decorator = new EncryptDALResultDecorator(database, mock(ShardingSphereMetaData.class));
        assertThat(decorator.decorate(mock(MergedResult.class), queryContext), isA(MergedResult.class));
    }
    
    private SQLStatementContext mockColumnInResultSetSQLStatementAttributeContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(1, 7, new IdentifierValue("foo_tbl")));
        when(result.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        when(result.getSqlStatement().getAttributes()).thenReturn(new SQLStatementAttributes(new ColumnInResultSetSQLStatementAttribute(1)));
        return result;
    }
    
    private SQLStatementContext mockTableInfoInResultSetAvailableStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(1, 7, new IdentifierValue("foo_tbl")));
        when(result.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(simpleTableSegment));
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableInResultSetSQLStatementAttribute(2)));
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
}
