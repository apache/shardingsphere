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

package org.apache.shardingsphere.orchestration.core.config.listener;

import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class PropertiesChangedListenerTest {
    
    private static final String PROPERTIES_YAML = "executor.size: 16\nsql.show: true";
    
    private PropertiesChangedListener propertiesChangedListener;
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Before
    public void setUp() {
        propertiesChangedListener = new PropertiesChangedListener("test", configurationRepository);
    }
    
    @Test
    public void assertCreateOrchestrationEvent() {
        assertThat(propertiesChangedListener.createOrchestrationEvent(new DataChangedEvent("test", PROPERTIES_YAML, ChangedType.UPDATED)).getProps().get("sql.show"), is(true));
    }
}
