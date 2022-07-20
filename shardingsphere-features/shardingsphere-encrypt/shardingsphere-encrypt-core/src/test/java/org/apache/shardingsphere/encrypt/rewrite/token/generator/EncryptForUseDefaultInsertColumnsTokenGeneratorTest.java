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

package org.apache.shardingsphere.encrypt.rewrite.token.generator;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptForUseDefaultInsertColumnsTokenGeneratorTest {

    @Test
    public void assertIsGenerateSQLToken() {
        EncryptForUseDefaultInsertColumnsTokenGenerator encryptForUseDefaultInsertColumnsTokenGenerator = new EncryptForUseDefaultInsertColumnsTokenGenerator();
        encryptForUseDefaultInsertColumnsTokenGenerator.setEncryptRule(new EncryptRule(createEncryptRuleConfiguration()));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList());
        assertFalse(encryptForUseDefaultInsertColumnsTokenGenerator.isGenerateSQLToken(insertStatementContext));
    }

    @Test
    public void assertGenerateSQLToken() {
        EncryptForUseDefaultInsertColumnsTokenGenerator encryptForUseDefaultInsertColumnsTokenGenerator = new EncryptForUseDefaultInsertColumnsTokenGenerator();
        encryptForUseDefaultInsertColumnsTokenGenerator.setEncryptRule(new EncryptRule(createEncryptRuleConfiguration()));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList());
        UseDefaultInsertColumnsToken useDefaultInsertColumnsToken = encryptForUseDefaultInsertColumnsTokenGenerator.generateSQLToken(insertStatementContext);
        assertThat(useDefaultInsertColumnsToken.toString(), is("(id, name, status, pwd_cipher, pwd_assist, pwd_plain)"));
    }

    private InsertStatementContext createInsertStatementContext(final List<Object> parameters) {
        InsertStatement insertStatement = createInsertStatement();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        String defaultSchemaName = DefaultDatabase.LOGIC_NAME;
        when(database.getSchemas().get(defaultSchemaName)).thenReturn(schema);
        when(schema.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status", "pwd"));
        return new InsertStatementContext(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), parameters, insertStatement, DefaultDatabase.LOGIC_NAME);
    }

    private InsertStatement createInsertStatement() {
        InsertStatement result = new MySQLInsertStatement();
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Arrays.asList(
                new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")),
                new ColumnSegment(0, 0, new IdentifierValue("status")), new ColumnSegment(0, 0, new IdentifierValue("pwd"))));
        result.setInsertColumns(insertColumnsSegment);
        result.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(
                new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2), new LiteralExpressionSegment(0, 0, "init"))));
        result.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(
                new ParameterMarkerExpressionSegment(0, 0, 3), new ParameterMarkerExpressionSegment(0, 0, 4), new LiteralExpressionSegment(0, 0, "init"))));
        return result;
    }

    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration pwdColumnConfig = new EncryptColumnRuleConfiguration("pwd", "pwd_cipher", "pwd_assist", "pwd_plain", "test_encryptor", "test_encryptor", false);
        return new EncryptRuleConfiguration(Collections.singleton(new EncryptTableRuleConfiguration("tbl", Collections.singletonList(pwdColumnConfig), null)),
                ImmutableMap.of("test_encryptor", new ShardingSphereAlgorithmConfiguration("CORE.QUERY_ASSISTED.FIXTURE", new Properties())));
    }
}
