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
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.AlterMaskRuleStatement;
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

class AlterMaskRuleExecutorTest {
    
    @Test
    void assertExecuteUpdateWithoutToBeAlteredRules() {
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        assertThrows(MissingRequiredRuleException.class, () -> new DistSQLUpdateExecuteEngine(createSQLStatement(), "foo_db", mockContextManager(rule), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        MaskColumnSegment columnSegment = new MaskColumnSegment("order_id", new AlgorithmSegment("MD5", new Properties()));
        MaskRuleSegment ruleSegment = new MaskRuleSegment("t_order", Collections.singleton(columnSegment));
        AlterMaskRuleStatement sqlStatement = new AlterMaskRuleStatement(Collections.singleton(ruleSegment));
        sqlStatement.buildAttributes();
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, null).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        metaDataManagerPersistService.removeRuleConfigurationItem(any(), ArgumentMatchers.argThat(this::assertToBeDroppedRuleConfiguration));
        metaDataManagerPersistService.alterRuleConfiguration(any(), ArgumentMatchers.argThat(this::assertToBeAlteredRuleConfiguration));
    }
    
    private boolean assertToBeDroppedRuleConfiguration(final MaskRuleConfiguration actual) {
        assertTrue(actual.getTables().isEmpty());
        assertTrue(actual.getMaskAlgorithms().isEmpty());
        return true;
    }
    
    private boolean assertToBeAlteredRuleConfiguration(final MaskRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().iterator().next().getName(), is("t_order"));
        assertThat(actual.getTables().iterator().next().getColumns().iterator().next().getLogicColumn(), is("order_id"));
        assertThat(actual.getMaskAlgorithms().size(), is(1));
        assertTrue(actual.getMaskAlgorithms().containsKey("t_order_order_id_md5"));
        return true;
    }
    
    private AlterMaskRuleStatement createSQLStatement() {
        MaskColumnSegment columnSegment = new MaskColumnSegment("user_id", new AlgorithmSegment("MD5", new Properties()));
        MaskRuleSegment ruleSegment = new MaskRuleSegment("t_mask", Collections.singleton(columnSegment));
        AlterMaskRuleStatement result = new AlterMaskRuleStatement(Collections.singleton(ruleSegment));
        result.buildAttributes();
        return result;
    }
    
    private MaskRuleConfiguration createCurrentRuleConfiguration() {
        Collection<MaskTableRuleConfiguration> tableRuleConfigs = new LinkedList<>();
        tableRuleConfigs.add(new MaskTableRuleConfiguration("t_order", Collections.emptyList()));
        return new MaskRuleConfiguration(tableRuleConfigs, Collections.emptyMap());
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
