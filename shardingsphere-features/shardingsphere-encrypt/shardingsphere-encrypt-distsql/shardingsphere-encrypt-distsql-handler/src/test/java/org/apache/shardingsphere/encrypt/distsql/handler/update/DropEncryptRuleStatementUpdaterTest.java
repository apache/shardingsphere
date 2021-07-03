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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.handler.exception.EncryptRuleNotExistedException;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public final class DropEncryptRuleStatementUpdaterTest {
    
    private final DropEncryptRuleStatementUpdater updater = new DropEncryptRuleStatementUpdater();
    
    @Test(expected = EncryptRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement(), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = EncryptRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement(), new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap()), mock(ShardingSphereResource.class));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        updater.updateCurrentRuleConfiguration("foo", createSQLStatement(), createCurrentRuleConfiguration());
        // TODO assert current rule configuration
    }
    
    private DropEncryptRuleStatement createSQLStatement() {
        return new DropEncryptRuleStatement(Collections.singleton("t_encrypt"));
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfiguration() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", "user_cipher", "", "user_plain", "t_encrypt_user_id_MD5");
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig));
        Map<String, ShardingSphereAlgorithmConfiguration> encryptors = new HashMap<>(1, 1);
        encryptors.put("t_encrypt_user_id_MD5", new ShardingSphereAlgorithmConfiguration("TEST", new Properties()));
        return new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), encryptors, true);
    }
}
