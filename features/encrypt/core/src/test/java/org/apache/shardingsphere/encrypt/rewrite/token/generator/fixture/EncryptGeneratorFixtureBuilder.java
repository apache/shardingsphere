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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

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
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
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
        EncryptColumnRuleConfiguration userNameColumnConfig = new EncryptColumnRuleConfiguration("user_name", new EncryptColumnItemRuleConfiguration("user_name_cipher", "standard_encryptor"));
        EncryptColumnRuleConfiguration userIdColumnConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_id_cipher", "standard_encryptor"));
        userIdColumnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_id_assist", "assisted_encryptor"));
        return new EncryptRule("foo_db",
                new EncryptRuleConfiguration(Collections.singleton(new EncryptTableRuleConfiguration("t_user", Arrays.asList(pwdColumnConfig, userNameColumnConfig, userIdColumnConfig))), encryptors));
    }
    
    /**
     * Create insert statement context.
     *
     * @param params parameters
     * @return created insert statement context
     */
    public static InsertStatementContext createInsertStatementContext(final List<Object> params) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("foo_db")).thenReturn(schema);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        InsertStatementContext result = new InsertStatementContext(createInsertStatement(), metaData, "foo_db");
        result.bindParameters(params);
        return result;
    }
    
    private static InsertStatement createInsertStatement() {
        InsertStatement result = new InsertStatement(DATABASE_TYPE);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Arrays.asList(
                new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")),
                new ColumnSegment(0, 0, new IdentifierValue("status")), new ColumnSegment(0, 0, new IdentifierValue("pwd"))));
        result.setInsertColumns(insertColumnsSegment);
        result.getValues().add(new InsertValuesSegment(0, 0, createValueExpressions()));
        return result;
    }
    
    private static InsertStatement createInsertSelectStatement(final boolean containsInsertColumns) {
        InsertStatement result = new InsertStatement(DATABASE_TYPE);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        ColumnSegment userIdColumn = new ColumnSegment(0, 0, new IdentifierValue("user_id"));
        userIdColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")), new IdentifierValue("t_user"),
                new IdentifierValue("user_id"), TableSourceType.PHYSICAL_TABLE));
        ColumnSegment userNameColumn = new ColumnSegment(0, 0, new IdentifierValue("user_name"));
        userNameColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")),
                new IdentifierValue("t_user"), new IdentifierValue("user_name"), TableSourceType.PHYSICAL_TABLE));
        List<ColumnSegment> insertColumns = Arrays.asList(userIdColumn, userNameColumn);
        if (containsInsertColumns) {
            result.setInsertColumns(new InsertColumnsSegment(0, 0, insertColumns));
        } else {
            result.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.emptyList()));
            result.getDerivedInsertColumns().addAll(insertColumns);
        }
        SelectStatement selectStatement = new SelectStatement(DATABASE_TYPE);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(userIdColumn));
        ColumnSegment statusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        statusColumn.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")), new IdentifierValue("t_user"),
                new IdentifierValue("status"), TableSourceType.PHYSICAL_TABLE));
        projections.getProjections().add(new ColumnProjectionSegment(statusColumn));
        selectStatement.setProjections(projections);
        result.setInsertSelect(new SubquerySegment(0, 0, selectStatement, ""));
        return result;
    }
    
    /**
     * Create update statement context.
     *
     * @return created update statement context
     */
    public static UpdateStatementContext createUpdateStatementContext() {
        UpdateStatement updateStatement = new UpdateStatement(DATABASE_TYPE);
        updateStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))));
        updateStatement.setWhere(createWhereSegment());
        updateStatement.setSetAssignment(createSetAssignmentSegment());
        return new UpdateStatementContext(updateStatement);
    }
    
    private static WhereSegment createWhereSegment() {
        ColumnSegment nameColumnSegment = new ColumnSegment(10, 13, new IdentifierValue("name"));
        TableSegmentBoundInfo tableSegmentBoundInfo = new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db"));
        nameColumnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(tableSegmentBoundInfo, new IdentifierValue("t_user"), new IdentifierValue("name"), TableSourceType.TEMPORARY_TABLE));
        BinaryOperationExpression nameExpression = new BinaryOperationExpression(10, 24, nameColumnSegment, new LiteralExpressionSegment(18, 22, "LiLei"), "=", "name = 'LiLei'");
        ColumnSegment pwdColumnSegment = new ColumnSegment(30, 32, new IdentifierValue("pwd"));
        pwdColumnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(tableSegmentBoundInfo, new IdentifierValue("t_user"), new IdentifierValue("pwd"), TableSourceType.TEMPORARY_TABLE));
        BinaryOperationExpression pwdExpression = new BinaryOperationExpression(30, 44, pwdColumnSegment, new LiteralExpressionSegment(40, 45, "123456"), "=", "pwd = '123456'");
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
    
    /**
     * Create insert select statement context.
     *
     * @param containsInsertColumns contains insert columns
     * @return created insert select statement context
     */
    public static InsertStatementContext createInsertSelectStatementContext(final boolean containsInsertColumns) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("foo_db")).thenReturn(schema);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        return new InsertStatementContext(createInsertSelectStatement(containsInsertColumns), metaData, "foo_db");
    }
}
