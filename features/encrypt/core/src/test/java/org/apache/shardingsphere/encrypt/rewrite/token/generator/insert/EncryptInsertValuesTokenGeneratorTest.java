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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.insert;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.fixture.EncryptGeneratorFixtureBuilder;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class EncryptInsertValuesTokenGeneratorTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private EncryptInsertValuesTokenGenerator generator;
    
    @BeforeEach
    void setup() {
        generator = new EncryptInsertValuesTokenGenerator(EncryptGeneratorFixtureBuilder.createEncryptRule(), new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        assertTrue(generator.isGenerateSQLToken(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Collections.emptyList())));
    }
    
    @Test
    void assertGenerateSQLTokenFromGenerateNewSQLToken() {
        generator.setPreviousSQLTokens(Collections.emptyList());
        assertThat(generator.generateSQLToken(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Arrays.asList(1, "Tom", 0, "123456"))).toString(), is("(?, ?, ?, ?, ?, ?)"));
    }
    
    @Test
    void assertGenerateSQLTokenFromPreviousSQLTokens() {
        generator.setPreviousSQLTokens(EncryptGeneratorFixtureBuilder.getPreviousSQLTokens());
        assertThat(generator.generateSQLToken(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Arrays.asList(1, "Tom", 0, "123456"))).toString(), is("(?, ?, ?, ?, ?, ?)"));
    }
    
    @Test
    void assertGenerateSQLTokenWithLiteralValues() {
        generator.setPreviousSQLTokens(Collections.emptyList());
        assertThat(generator.generateSQLToken(createLiteralInsertStatementContext()).toString(), is("(1, 'Tom', 0, 'encryptValue', 'assistedEncryptValue', 'likeEncryptValue')"));
    }
    
    private InsertStatementContext createLiteralInsertStatementContext() {
        ShardingSphereDatabase database = EncryptGeneratorFixtureBuilder.createDatabase();
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Arrays.asList(
                new ColumnSegment(0, 0, new IdentifierValue("id")),
                new ColumnSegment(0, 0, new IdentifierValue("name")),
                new ColumnSegment(0, 0, new IdentifierValue("status")),
                new ColumnSegment(0, 0, new IdentifierValue("pwd"))));
        List<ExpressionSegment> valueExpressions = new ArrayList<>(4);
        valueExpressions.add(new LiteralExpressionSegment(0, 0, 1));
        valueExpressions.add(new LiteralExpressionSegment(0, 0, "Tom"));
        valueExpressions.add(new LiteralExpressionSegment(0, 0, 0));
        valueExpressions.add(new LiteralExpressionSegment(0, 0, "123456"));
        InsertStatement insertStatement = InsertStatement.builder().databaseType(DATABASE_TYPE)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user"))))
                .insertColumns(insertColumnsSegment).values(Collections.singletonList(new InsertValuesSegment(0, 0, valueExpressions))).build();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        return new InsertStatementContext(insertStatement, metaData, "foo_db");
    }
}
