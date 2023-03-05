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

package org.apache.shardingsphere.encrypt.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public final class CreateEncryptRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final CreateEncryptRuleStatementUpdater updater = new CreateEncryptRuleStatementUpdater();
    
    @Test
    public void assertCheckSQLStatementWithDuplicateEncryptRule() {
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, "MD5"), getCurrentRuleConfig()));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutToBeCreatedEncryptors() {
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, "INVALID_TYPE"), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithIncompleteDataType() {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id", "user_cipher", "user_plain", "assisted_column", "like_column",
                "int varchar(10)", null, null, null, null, new AlgorithmSegment("test", new Properties()),
                new AlgorithmSegment("test", new Properties()),
                new AlgorithmSegment("CHAR_DIGEST_LIKE", new Properties()), null);
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment), null);
        CreateEncryptRuleStatement statement = new CreateEncryptRuleStatement(false, Collections.singleton(ruleSegment));
        assertThrows(InvalidRuleConfigurationException.class, () -> updater.checkSQLStatement(database, statement, null));
    }
    
    @Test
    public void assertCreateEncryptRuleWithIfNotExists() {
        EncryptRuleConfiguration currentRuleConfig = getCurrentRuleConfig();
        CreateEncryptRuleStatement sqlStatement = createSQLStatement(true, "AES");
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        EncryptRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, sqlStatement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        assertThat(currentRuleConfig.getTables().size(), is(2));
        assertTrue(currentRuleConfig.getEncryptors().isEmpty());
    }
    
    private CreateEncryptRuleStatement createSQLStatement(final boolean ifNotExists, final String encryptorName) {
        EncryptColumnSegment tEncryptColumnSegment = new EncryptColumnSegment("user_id", "user_cipher", "user_plain", "assisted_column", "like_column",
                new AlgorithmSegment(encryptorName, new Properties()),
                new AlgorithmSegment(encryptorName, new Properties()),
                new AlgorithmSegment(encryptorName, new Properties()), null);
        EncryptColumnSegment tOrderColumnSegment = new EncryptColumnSegment("order_id", "order_cipher", "order_plain", "assisted_column", "like_column",
                new AlgorithmSegment(encryptorName, new Properties()),
                new AlgorithmSegment(encryptorName, new Properties()),
                new AlgorithmSegment(encryptorName, new Properties()), null);
        EncryptRuleSegment tEncryptRuleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(tEncryptColumnSegment), null);
        EncryptRuleSegment tOrderRuleSegment = new EncryptRuleSegment("t_order", Collections.singleton(tOrderColumnSegment), null);
        Collection<EncryptRuleSegment> rules = new LinkedList<>();
        rules.add(tEncryptRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateEncryptRuleStatement(ifNotExists, rules);
    }
    
    private EncryptRuleConfiguration getCurrentRuleConfig() {
        Collection<EncryptTableRuleConfiguration> rules = new LinkedList<>();
        rules.add(new EncryptTableRuleConfiguration("t_encrypt", Collections.emptyList(), null));
        rules.add(new EncryptTableRuleConfiguration("t_order", Collections.emptyList(), null));
        return new EncryptRuleConfiguration(rules, new HashMap<>());
    }
}
