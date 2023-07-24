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

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
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
class CreateEncryptRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final CreateEncryptRuleStatementUpdater updater = new CreateEncryptRuleStatementUpdater();
    
    @Test
    void assertCheckSQLStatementWithDuplicateEncryptRule() {
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, "MD5"), getCurrentRuleConfig()));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeCreatedEncryptors() {
        assertThrows(ServiceProviderNotFoundServerException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, "INVALID_TYPE"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithConflictColumnNames() {
        assertThrows(InvalidRuleConfigurationException.class, () -> updater.checkSQLStatement(database, createConflictColumnNameSQLStatement(), getCurrentRuleConfig()));
    }
    
    @Test
    void assertCreateEncryptRuleWithIfNotExists() {
        EncryptRuleConfiguration currentRuleConfig = getCurrentRuleConfig();
        CreateEncryptRuleStatement sqlStatement = createAESEncryptRuleSQLStatement(true);
        updater.checkSQLStatement(database, sqlStatement, currentRuleConfig);
        EncryptRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, sqlStatement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        assertThat(currentRuleConfig.getTables().size(), is(2));
        assertTrue(currentRuleConfig.getEncryptors().isEmpty());
    }
    
    private CreateEncryptRuleStatement createAESEncryptRuleSQLStatement(final boolean ifNotExists) {
        EncryptColumnSegment encryptColumnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("AES", PropertiesBuilder.build(new Property("aes-key-value", "abc")))),
                new EncryptColumnItemSegment("assisted_column", null),
                new EncryptColumnItemSegment("like_column", null));
        Collection<EncryptRuleSegment> rules = new LinkedList<>();
        rules.add(new EncryptRuleSegment("t_user", Collections.singleton(encryptColumnSegment)));
        return new CreateEncryptRuleStatement(ifNotExists, rules);
    }
    
    private CreateEncryptRuleStatement createSQLStatement(final boolean ifNotExists, final String encryptorName) {
        EncryptColumnSegment tUserColumnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment(encryptorName, new Properties())));
        EncryptColumnSegment tOrderColumnSegment = new EncryptColumnSegment("order_id",
                new EncryptColumnItemSegment("order_cipher", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment(encryptorName, new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment(encryptorName, new Properties())));
        EncryptRuleSegment tUserRuleSegment = new EncryptRuleSegment("t_user", Collections.singleton(tUserColumnSegment));
        EncryptRuleSegment tOrderRuleSegment = new EncryptRuleSegment("t_order", Collections.singleton(tOrderColumnSegment));
        Collection<EncryptRuleSegment> rules = new LinkedList<>();
        rules.add(tUserRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateEncryptRuleStatement(ifNotExists, rules);
    }
    
    private CreateEncryptRuleStatement createConflictColumnNameSQLStatement() {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("MD5", new Properties())),
                new EncryptColumnItemSegment("user_id", new AlgorithmSegment("test", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("test", new Properties())));
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment));
        return new CreateEncryptRuleStatement(false, Collections.singleton(ruleSegment));
    }
    
    private EncryptRuleConfiguration getCurrentRuleConfig() {
        Collection<EncryptTableRuleConfiguration> rules = new LinkedList<>();
        rules.add(new EncryptTableRuleConfiguration("t_user", Collections.emptyList()));
        rules.add(new EncryptTableRuleConfiguration("t_order", Collections.emptyList()));
        return new EncryptRuleConfiguration(rules, new HashMap<>());
    }
    
    @Test
    void assertCreateAESEncryptRuleWithPropertiesNotExists() {
        EncryptRuleConfiguration currentRuleConfig = getCurrentRuleConfig();
        CreateEncryptRuleStatement sqlStatement = createWrongAESEncryptorSQLStatement();
        assertThrows(EncryptAlgorithmInitializationException.class, () -> updater.checkSQLStatement(database, sqlStatement, currentRuleConfig));
    }
    
    private CreateEncryptRuleStatement createWrongAESEncryptorSQLStatement() {
        EncryptColumnSegment tUserColumnSegment = new EncryptColumnSegment("user_id",
                new EncryptColumnItemSegment("user_cipher", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("CHAR_DIGEST_LIKE", new Properties())));
        EncryptColumnSegment tOrderColumnSegment = new EncryptColumnSegment("order_id",
                new EncryptColumnItemSegment("order_cipher", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("assisted_column", new AlgorithmSegment("AES", new Properties())),
                new EncryptColumnItemSegment("like_column", new AlgorithmSegment("CHAR_DIGEST_LIKE", new Properties())));
        EncryptRuleSegment tUserRuleSegment = new EncryptRuleSegment("t_user", Collections.singleton(tUserColumnSegment));
        EncryptRuleSegment tOrderRuleSegment = new EncryptRuleSegment("t_order", Collections.singleton(tOrderColumnSegment));
        Collection<EncryptRuleSegment> rules = new LinkedList<>();
        rules.add(tUserRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateEncryptRuleStatement(true, rules);
    }
}
