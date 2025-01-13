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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.node.spi.RuleNodePathProvider;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterUniqueRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropUniqueRuleItem;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereServiceLoader.class)
class RuleConfigurationChangedHandlerTest {
    
    private RuleConfigurationChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        handler = new RuleConfigurationChangedHandler(contextManager);
        RuleNodePathProvider ruleNodePathProvider = mock(RuleNodePathProvider.class, RETURNS_DEEP_STUBS);
        when(ruleNodePathProvider.getRuleNodePath()).thenReturn(new RuleNodePath("fixture", Collections.singleton("named"), Collections.singleton("unique")));
        when(ShardingSphereServiceLoader.getServiceInstances(RuleNodePathProvider.class)).thenReturn(Collections.singleton(ruleNodePathProvider));
    }
    
    @Test
    void assertHandleWithInvalidPath() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/invalid/rules/fixture", "foo", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).alterRuleItem(any());
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).dropRuleItem(any());
    }
    
    @Test
    void assertHandleWithEmptyValue() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/versions/0", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).alterRuleItem(any());
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).dropRuleItem(any());
    }
    
    @Test
    void assertHandleWithPathNotFound() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/versions/0", "foo", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).alterRuleItem(any());
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).dropRuleItem(any());
    }
    
    @Test
    void assertHandleWithIgnoreType() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/named/xxx/active_version", "foo", Type.IGNORED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).alterRuleItem(any());
        verify(contextManager.getMetaDataContextManager().getRuleItemManager(), times(0)).dropRuleItem(any());
    }
    
    @Test
    void assertHandleWithNamedRuleItemAdded() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/named/xxx/active_version", "foo", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager()).alterRuleItem(any(AlterNamedRuleItem.class));
    }
    
    @Test
    void assertHandleWithNamedRuleItemAltered() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/named/xxx/active_version", "foo", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager()).alterRuleItem(any(AlterNamedRuleItem.class));
    }
    
    @Test
    void assertHandleWithNamedRuleItemDropped() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/named/xxx", "foo", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager()).dropRuleItem(any(DropNamedRuleItem.class));
    }
    
    @Test
    void assertHandleWithUniqueRuleItemAdded() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/unique/active_version", "foo", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager()).alterRuleItem(any(AlterUniqueRuleItem.class));
    }
    
    @Test
    void assertHandleWithUniqueRuleItemAltered() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/unique/active_version", "foo", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager()).alterRuleItem(any(AlterUniqueRuleItem.class));
    }
    
    @Test
    void assertHandleWithUniqueRuleItemDropped() throws SQLException {
        handler.handle("foo_db", new DataChangedEvent("/metadata/fixture/rules/fixture/unique/active_version", "foo", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getRuleItemManager()).dropRuleItem(any(DropUniqueRuleItem.class));
    }
}
