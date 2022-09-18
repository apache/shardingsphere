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

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public final class EncryptForUseDefaultInsertColumnsTokenGeneratorTest extends EncryptGeneratorBaseTest {
    
    @Test
    public void assertIsGenerateSQLToken() {
        EncryptForUseDefaultInsertColumnsTokenGenerator tokenGenerator = new EncryptForUseDefaultInsertColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(createEncryptRule());
        assertFalse(tokenGenerator.isGenerateSQLToken(createInsertStatementContext(Collections.emptyList())));
    }
    
    @Test
    public void assertGenerateSQLTokenFromGenerateNewSQLToken() {
        EncryptForUseDefaultInsertColumnsTokenGenerator tokenGenerator = new EncryptForUseDefaultInsertColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(createEncryptRule());
        tokenGenerator.setPreviousSQLTokens(Collections.emptyList());
        assertThat(tokenGenerator.generateSQLToken(createInsertStatementContext(Collections.emptyList())).toString(), is("(id, name, status, pwd_cipher, pwd_assist, pwd_plain)"));
    }
    
    @Test
    public void assertGenerateSQLTokenFromPreviousSQLTokens() {
        EncryptForUseDefaultInsertColumnsTokenGenerator tokenGenerator = new EncryptForUseDefaultInsertColumnsTokenGenerator();
        tokenGenerator.setEncryptRule(createEncryptRule());
        tokenGenerator.setPreviousSQLTokens(getPreviousSQLTokens());
        assertThat(tokenGenerator.generateSQLToken(createInsertStatementContext(Collections.emptyList())).toString(), is("(id, name, status, pwd_cipher, pwd_assist, pwd_plain)"));
    }
}
