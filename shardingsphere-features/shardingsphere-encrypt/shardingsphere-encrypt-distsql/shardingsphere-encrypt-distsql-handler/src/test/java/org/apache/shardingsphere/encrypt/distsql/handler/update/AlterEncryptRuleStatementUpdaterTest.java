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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.handler.exception.EncryptRuleNotExistedException;
import org.apache.shardingsphere.encrypt.distsql.handler.exception.InvalidEncryptorsException;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public final class AlterEncryptRuleStatementUpdaterTest {
    
    private final AlterEncryptRuleStatementUpdater updater = new AlterEncryptRuleStatementUpdater();
    
    @Test(expected = EncryptRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement("MD5"), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = EncryptRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredRules() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement("MD5"), new EncryptRuleConfiguration(Collections.emptyList(), new HashMap<>()), mock(ShardingSphereResource.class));
    }
    
    @Test(expected = InvalidEncryptorsException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredEncryptors() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement("INVALID_TYPE"), createCurrentRuleConfiguration(), mock(ShardingSphereResource.class));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        updater.updateCurrentRuleConfiguration("foo", createSQLStatement("MD5"), createCurrentRuleConfiguration());
        // TODO assert current rule configuration
    }
    
    private AlterEncryptRuleStatement createSQLStatement(final String encryptorName) {
        EncryptColumnSegment columnSegment = new EncryptColumnSegment("user_id", "user_cipher", "user_plain", new AlgorithmSegment(encryptorName, new Properties()));
        EncryptRuleSegment ruleSegment = new EncryptRuleSegment("t_encrypt", Collections.singleton(columnSegment));
        return new AlterEncryptRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfiguration() {
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.emptyList());
        return new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(tableRuleConfig)), new HashMap<>());
    }
}
