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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Encrypt generator fixture builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptGeneratorFixtureBuilder {
    
    /**
     * Create encrypt rule.
     * 
     * @return created encrypt rule
     */
    public static EncryptRule createEncryptRule() {
        Map<String, AlgorithmConfiguration> encryptors = new LinkedHashMap<>(2, 1F);
        encryptors.put("standard_encryptor", new AlgorithmConfiguration("CORE.FIXTURE", new Properties()));
        encryptors.put("assisted_encryptor", new AlgorithmConfiguration("CORE.QUERY_ASSISTED.FIXTURE", new Properties()));
        encryptors.put("like_encryptor", new AlgorithmConfiguration("CORE.QUERY_LIKE.FIXTURE", new Properties()));
        EncryptColumnRuleConfiguration pwdColumnConfig =
                new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", "standard_encryptor"));
        pwdColumnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("pwd_assist", "assisted_encryptor"));
        pwdColumnConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("pwd_like", "like_encryptor"));
        return new EncryptRule(
                new EncryptRuleConfiguration(Collections.singleton(new EncryptTableRuleConfiguration("t_user", Collections.singletonList(pwdColumnConfig))), encryptors));
    }
    
    /**
     * Create insert statement context.
     * 
     * @param params parameters
     * @return created insert statement context
     */
    public static InsertStatementContext createInsertStatementContext(final List<Object> params) {
        InsertStatement insertStatement = createInsertStatement();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        when(schema.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status", "pwd"));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
        return new InsertStatementContext(metaData, params, insertStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private static InsertStatement createInsertStatement() {
        InsertStatement result = new MySQLInsertStatement();
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Arrays.asList(
                new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")),
                new ColumnSegment(0, 0, new IdentifierValue("status")), new ColumnSegment(0, 0, new IdentifierValue("pwd"))));
        result.setInsertColumns(insertColumnsSegment);
        result.getValues().add(new InsertValuesSegment(0, 0, createValueExpressions()));
        return result;
    }
    
    /**
     * Create update statement context.
     * 
     * @return created update statement context
     */
    public static UpdateStatementContext createUpdateStatementContext() {
        MySQLUpdateStatement updateStatement = new MySQLUpdateStatement();
        updateStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        updateStatement.setWhere(createWhereSegment());
        updateStatement.setSetAssignment(createSetAssignmentSegment());
        return new UpdateStatementContext(updateStatement);
    }
    
    private static WhereSegment createWhereSegment() {
        BinaryOperationExpression nameExpression = new BinaryOperationExpression(10, 24,
                new ColumnSegment(10, 13, new IdentifierValue("name")), new LiteralExpressionSegment(18, 22, "LiLei"), "=", "name = 'LiLei'");
        BinaryOperationExpression pwdExpression = new BinaryOperationExpression(30, 44,
                new ColumnSegment(30, 32, new IdentifierValue("pwd")), new LiteralExpressionSegment(40, 45, "123456"), "=", "pwd = '123456'");
        return new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, nameExpression, pwdExpression, "AND", "name = 'LiLei' AND pwd = '123456'"));
    }
    
    private static SetAssignmentSegment createSetAssignmentSegment() {
        List<ColumnSegment> columnSegment = Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("pwd")));
        return new SetAssignmentSegment(0, 0, Collections.singletonList(new ColumnAssignmentSegment(0, 0, columnSegment, new LiteralExpressionSegment(0, 0, "654321"))));
    }
    
    /**
     * Get previous SQL tokens.
     * 
     * @return previous SQL tokens
     */
    public static List<SQLToken> getPreviousSQLTokens() {
        EncryptInsertValuesToken encryptInsertValuesToken = new EncryptInsertValuesToken(0, 0);
        encryptInsertValuesToken.getInsertValues().add(new InsertValue(createValueExpressions()));
        return Collections.singletonList(encryptInsertValuesToken);
    }
    
    private static List<ExpressionSegment> createValueExpressions() {
        List<ExpressionSegment> result = new ArrayList<>(4);
        result.add(new ParameterMarkerExpressionSegment(0, 0, 1));
        result.add(new ParameterMarkerExpressionSegment(0, 0, 2));
        result.add(new ParameterMarkerExpressionSegment(0, 0, 3));
        result.add(new ParameterMarkerExpressionSegment(0, 0, 4));
        return result;
    }
}
