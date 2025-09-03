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

package org.apache.shardingsphere.mask.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.CreateMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateMaskRuleExecutorTest {
    
    @Test
    void assertExecuteUpdateWithDuplicateMaskRule() {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(getCurrentRuleConfiguration());
        assertThrows(DuplicateRuleException.class, () -> new DistSQLUpdateExecuteEngine(createDuplicatedSQLStatement(false, "MD5"), "foo_db", mockContextManager(rule)).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithoutIfNotExists() throws SQLException {
        MaskRuleConfiguration currentRuleConfig = getCurrentRuleConfiguration();
        CreateMaskRuleStatement sqlStatement = createSQLStatement(false, "MD5");
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertRuleConfiguration));
    }
    
    @Test
    void assertExecuteUpdateWithIfNotExists() throws SQLException {
        MaskRuleConfiguration currentRuleConfig = getCurrentRuleConfiguration();
        MaskRule rule = mock(MaskRule.class);
        CreateMaskRuleStatement sqlStatement = createSQLStatement(true, "MD5");
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertRuleConfiguration));
    }
    
    private CreateMaskRuleStatement createSQLStatement(final boolean ifNotExists, final String algorithmType) {
        MaskColumnSegment tMaskColumnSegment = new MaskColumnSegment("user_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskColumnSegment tOrderColumnSegment = new MaskColumnSegment("order_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskRuleSegment tMaskRuleSegment = new MaskRuleSegment("t_mask_1", Collections.singleton(tMaskColumnSegment));
        MaskRuleSegment tOrderRuleSegment = new MaskRuleSegment("t_order_1", Collections.singleton(tOrderColumnSegment));
        Collection<MaskRuleSegment> rules = new LinkedList<>();
        rules.add(tMaskRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateMaskRuleStatement(ifNotExists, rules);
    }
    
    private CreateMaskRuleStatement createDuplicatedSQLStatement(final boolean ifNotExists, final String algorithmType) {
        MaskColumnSegment tMaskColumnSegment = new MaskColumnSegment("user_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskColumnSegment tOrderColumnSegment = new MaskColumnSegment("order_id", new AlgorithmSegment(algorithmType, new Properties()));
        MaskRuleSegment tMaskRuleSegment = new MaskRuleSegment("t_mask", Collections.singleton(tMaskColumnSegment));
        MaskRuleSegment tOrderRuleSegment = new MaskRuleSegment("t_order", Collections.singleton(tOrderColumnSegment));
        Collection<MaskRuleSegment> rules = new LinkedList<>();
        rules.add(tMaskRuleSegment);
        rules.add(tOrderRuleSegment);
        return new CreateMaskRuleStatement(ifNotExists, rules);
    }
    
    private MaskRuleConfiguration getCurrentRuleConfiguration() {
        Collection<MaskTableRuleConfiguration> rules = new LinkedList<>();
        rules.add(new MaskTableRuleConfiguration("t_mask", Collections.emptyList()));
        rules.add(new MaskTableRuleConfiguration("t_order", Collections.emptyList()));
        return new MaskRuleConfiguration(rules, Collections.emptyMap());
    }
    
    private boolean assertRuleConfiguration(final MaskRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(2));
        assertTrue(actual.getMaskAlgorithms().containsKey("t_mask_1_user_id_md5"));
        assertTrue(actual.getMaskAlgorithms().containsKey("t_order_1_order_id_md5"));
        return true;
    }
    
    private ContextManager mockContextManager(final MaskRule rule) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
