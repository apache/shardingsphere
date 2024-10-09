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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.qualified.QualifiedDataSourceState;
import org.apache.shardingsphere.mode.event.dispatch.state.storage.QualifiedDataSourceStateEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualifiedDataSourceSubscriberTest {
    
    private QualifiedDataSourceSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        subscriber = new QualifiedDataSourceSubscriber(contextManager);
    }
    
    @Test
    void assertRenew() {
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        StaticDataSourceRuleAttribute staticDataSourceRuleAttribute = mock(StaticDataSourceRuleAttribute.class);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class))
                .thenReturn(Collections.singleton(staticDataSourceRuleAttribute));
        QualifiedDataSource qualifiedDataSource = new QualifiedDataSource("foo_db.foo_group.foo_ds");
        subscriber.renew(new QualifiedDataSourceStateEvent(qualifiedDataSource, new QualifiedDataSourceState(DataSourceState.DISABLED)));
        verify(staticDataSourceRuleAttribute).updateStatus(qualifiedDataSource, DataSourceState.DISABLED);
    }
}
