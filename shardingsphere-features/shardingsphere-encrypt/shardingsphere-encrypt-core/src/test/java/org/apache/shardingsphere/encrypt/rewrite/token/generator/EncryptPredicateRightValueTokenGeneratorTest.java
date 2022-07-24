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

import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptConditionEngine;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class EncryptPredicateRightValueTokenGeneratorTest extends EncryptGeneratorBaseTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        EncryptPredicateRightValueTokenGenerator tokenGenerator = new EncryptPredicateRightValueTokenGenerator();
        tokenGenerator.setDatabaseName("db-001");
        tokenGenerator.setEncryptRule(new EncryptRule(createEncryptRuleConfiguration()));
        assertTrue(tokenGenerator.isGenerateSQLToken(createUpdatesStatementContext()));
    }
    
    @Test
    public void assertGenerateSQLTokenFromGenerateNewSQLToken() {
        UpdateStatementContext updatesStatementContext = createUpdatesStatementContext();
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mock(ShardingSphereSchema.class)), updatesStatementContext,
                "UPDATE t_user SET pwd = '654321' WHERE name = 'LiLei' AND pwd = '123456'",
                Collections.emptyList());
        Collection<EncryptCondition> encryptConditions = new EncryptConditionEngine(new EncryptRule(createEncryptRuleConfiguration()), sqlRewriteContext.getSchemas())
                .createEncryptConditions(updatesStatementContext.getWhereSegments(), updatesStatementContext.getColumnSegments(), updatesStatementContext, sqlRewriteContext.getDatabaseName());
        EncryptPredicateRightValueTokenGenerator tokenGenerator = new EncryptPredicateRightValueTokenGenerator();
        tokenGenerator.setDatabaseName(DefaultDatabase.LOGIC_NAME);
        tokenGenerator.setEncryptRule(new EncryptRule(createEncryptRuleConfiguration()));
        tokenGenerator.setEncryptConditions(encryptConditions);
        Collection<SQLToken> sqlTokens = tokenGenerator.generateSQLTokens(updatesStatementContext);
        assertTrue(sqlTokens.stream().findFirst().isPresent());
        assertThat(sqlTokens.stream().findFirst().get().toString(), is("'123456'"));
    }
}
