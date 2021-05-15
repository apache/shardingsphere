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

package org.apache.shardingsphere.governance.core.registry.listener.impl;

import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GlobalRuleChangedListenerTest extends GovernanceListenerTest {
    
    private GlobalRuleChangedListener globalRuleChangedListener;
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    @Before
    public void setUp() {
        globalRuleChangedListener = new GlobalRuleChangedListener(registryCenterRepository);
    }
    
    @Test
    public void assertCreateEvent() {
        Optional<GovernanceEvent> event = globalRuleChangedListener.createEvent(new DataChangedEvent("rule", readYAML("yaml/authority-rule.yaml"), Type.UPDATED));
        assertTrue(event.isPresent());
        assertThat(event.get(), instanceOf(GlobalRuleConfigurationsChangedEvent.class));
    }
}
