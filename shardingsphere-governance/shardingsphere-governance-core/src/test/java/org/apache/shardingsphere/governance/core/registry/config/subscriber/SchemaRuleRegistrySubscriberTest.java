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

package org.apache.shardingsphere.governance.core.registry.config.subscriber;

import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.SchemaRulePersistService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaRuleRegistrySubscriberTest {
    
    @Mock
    private SchemaRulePersistService persistService;
    
    @Test
    public void assertUpdate() {
        RuleConfigurationsAlteredSQLNotificationEvent event = new RuleConfigurationsAlteredSQLNotificationEvent("foo_db", Collections.emptyList());
        new SchemaRuleRegistrySubscriber(persistService).update(event);
        verify(persistService).persist(event.getSchemaName(), event.getRuleConfigurations());
    }
}
