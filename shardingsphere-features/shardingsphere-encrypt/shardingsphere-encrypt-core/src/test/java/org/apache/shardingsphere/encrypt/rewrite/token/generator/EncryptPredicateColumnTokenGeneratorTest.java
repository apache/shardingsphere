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

import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptPredicateColumnTokenGeneratorTest extends EncryptGeneratorBaseTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        EncryptPredicateColumnTokenGenerator tokenGenerator = new EncryptPredicateColumnTokenGenerator();
        tokenGenerator.setDatabaseName(DefaultDatabase.LOGIC_NAME);
        tokenGenerator.setEncryptRule(createEncryptRule());
        tokenGenerator.setSchemas(Collections.emptyMap());
        assertTrue(tokenGenerator.isGenerateSQLToken(createUpdatesStatementContext()));
    }
    
    @Test
    public void assertGenerateSQLTokenFromGenerateNewSQLToken() {
        EncryptPredicateColumnTokenGenerator tokenGenerator = new EncryptPredicateColumnTokenGenerator();
        tokenGenerator.setDatabaseName(DefaultDatabase.LOGIC_NAME);
        tokenGenerator.setEncryptRule(createEncryptRule());
        tokenGenerator.setSchemas(Collections.emptyMap());
        Collection<SubstitutableColumnNameToken> substitutableColumnNameTokens = tokenGenerator.generateSQLTokens(createUpdatesStatementContext());
        assertThat(substitutableColumnNameTokens.size(), is(1));
        assertThat(substitutableColumnNameTokens.iterator().next().toString(null), is("pwd_plain"));
    }
}
