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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.rule.type;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereArgumentVerifyMatchers.deepEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NamedRuleItemConfigurationChangedHandlerTest {
    
    private NamedRuleItemConfigurationChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        handler = new NamedRuleItemConfigurationChangedHandler(contextManager);
    }
    
    @Test
    void assertHandleWithInvalidPath() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).alter(any());
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).drop(any());
    }
    
    @Test
    void assertHandleWithIgnoreType() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/named/foo_rule_item/active_version", "0", Type.IGNORED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).alter(any());
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager(), never()).drop(any());
    }
    
    @Test
    void assertHandleWithAddItem() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/named/foo_rule_item/active_version", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager())
                .alter(deepEq(new DatabaseRuleNodePath("foo_db", "fixture", new DatabaseRuleItem("named", "foo_rule_item"))));
    }
    
    @Test
    void assertHandleWithAlterItem() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/named/foo_rule_item/active_version", "0", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager())
                .alter(deepEq(new DatabaseRuleNodePath("foo_db", "fixture", new DatabaseRuleItem("named", "foo_rule_item"))));
    }
    
    @Test
    void assertHandleDropItem() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/rules/fixture/named/foo_rule_item", "0", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getDatabaseRuleItemManager()).drop(deepEq(new DatabaseRuleNodePath("foo_db", "fixture", new DatabaseRuleItem("named", "foo_rule_item"))));
    }
}
