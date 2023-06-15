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

import org.apache.shardingsphere.encrypt.rewrite.token.generator.fixture.EncryptGeneratorFixtureBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptInsertValuesTokenGeneratorTest {
    
    private final EncryptInsertValuesTokenGenerator generator = new EncryptInsertValuesTokenGenerator();
    
    @BeforeEach
    void setup() {
        generator.setEncryptRule(EncryptGeneratorFixtureBuilder.createEncryptRule());
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        assertTrue(generator.isGenerateSQLToken(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Collections.emptyList())));
    }
    
    @Test
    void assertGenerateSQLTokenFromGenerateNewSQLToken() {
        generator.setPreviousSQLTokens(Collections.emptyList());
        generator.setDatabaseName("db_schema");
        assertThat(generator.generateSQLToken(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Arrays.asList(1, "Tom", 0, "123456"))).toString(), is("(?, ?, ?, ?, ?, ?)"));
    }
    
    @Test
    void assertGenerateSQLTokenFromPreviousSQLTokens() {
        generator.setDatabaseName("db-001");
        generator.setPreviousSQLTokens(EncryptGeneratorFixtureBuilder.getPreviousSQLTokens());
        generator.setDatabaseName("db_schema");
        assertThat(generator.generateSQLToken(EncryptGeneratorFixtureBuilder.createInsertStatementContext(Arrays.asList(1, "Tom", 0, "123456"))).toString(), is("(?, ?, ?, ?, ?, ?)"));
    }
}
