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

package org.apache.shardingsphere.encrypt.rewrite.token;

import org.apache.shardingsphere.encrypt.rewrite.token.generator.impl.EncryptPredicateColumnTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EncryptTokenGenerateBuilderTest {

    private EncryptTokenGenerateBuilder builder;
    
    @Test
    public void getSQLTokenGeneratorsTest() {
        final EncryptRule encryptRule = mock(EncryptRule.class);
        builder = new EncryptTokenGenerateBuilder(encryptRule, true);
        final Collection<SQLTokenGenerator> sqlTokenGenerators = builder.getSQLTokenGenerators();

        assertEquals(11, sqlTokenGenerators.size());
    }
}
