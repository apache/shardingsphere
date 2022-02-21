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

package org.apache.shardingsphere.shadow.distsql.update;

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.CreateDefaultShadowAlgorithmStatementUpdater;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateDefaultShadowAlgorithmStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDefaultShadowAlgorithmStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfiguration;
    
    private final CreateDefaultShadowAlgorithmStatementUpdater updater = new CreateDefaultShadowAlgorithmStatementUpdater();
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertExecuteWithNotExistAlgorithm() throws DistSQLException {
        when(currentConfiguration.getShadowAlgorithms()).thenReturn(Collections.singletonMap("default_name", null));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("input_default_name"), currentConfiguration);
    }
    
    private CreateDefaultShadowAlgorithmStatement createSQLStatement(final String defaultShadowAlgorithmName) {
        return new CreateDefaultShadowAlgorithmStatement(defaultShadowAlgorithmName);
    }
}
