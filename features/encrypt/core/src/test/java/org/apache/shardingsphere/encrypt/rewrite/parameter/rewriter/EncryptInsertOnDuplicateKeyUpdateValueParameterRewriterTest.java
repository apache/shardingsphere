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

package org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptInsertOnDuplicateKeyUpdateValueParameterRewriterTest {
    
    @Test
    void assertRewriteWithDefaultSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        CipherColumnItem cipherColumnItem = mock(CipherColumnItem.class);
        when(cipherColumnItem.encrypt("foo_db", "foo_default_schema", "foo_table", "foo_column", "foo_value")).thenReturn("foo_cipher_value");
        EncryptRule rule = mockEncryptRule(cipherColumnItem);
        StandardParameterBuilder paramBuilder = new StandardParameterBuilder(Collections.singletonList("foo_value"));
        new EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter(rule, database)
                .rewrite(paramBuilder, createSQLStatementContext(), Collections.singletonList("foo_value"));
        assertThat(paramBuilder.getParameters(), is(Collections.singletonList("foo_cipher_value")));
    }
    
    private EncryptRule mockEncryptRule(final CipherColumnItem cipherColumnItem) {
        EncryptColumn encryptColumn = new EncryptColumn("foo_column", cipherColumnItem);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.isEncryptColumn("foo_column")).thenReturn(true);
        when(encryptTable.getEncryptColumn("foo_column")).thenReturn(encryptColumn);
        EncryptRule result = mock(EncryptRule.class);
        when(result.findEncryptTable("foo_table")).thenReturn(Optional.of(encryptTable));
        when(result.getEncryptTable("foo_table")).thenReturn(encryptTable);
        return result;
    }
    
    private InsertStatementContext createSQLStatementContext() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_table")))).build();
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(insertStatement);
        when(result.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        OnDuplicateUpdateContext onDuplicateUpdateContext = mock(OnDuplicateUpdateContext.class);
        when(onDuplicateUpdateContext.getValueExpressions()).thenReturn(Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0)));
        when(onDuplicateUpdateContext.getColumn(0)).thenReturn(new ColumnSegment(0, 0, new IdentifierValue("foo_column")));
        when(onDuplicateUpdateContext.getValue(0)).thenReturn("foo_value");
        when(result.getOnDuplicateKeyUpdateValueContext()).thenReturn(onDuplicateUpdateContext);
        return result;
    }
}
