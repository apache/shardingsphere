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

package org.apache.shardingsphere.governance.core.config.listener;

import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class AuthenticationChangedListenerTest {
    
    private static final String AUTHENTICATION_YAML = "  users:\n" + "    root1:\n      password: root1\n" 
            + "      authorizedSchemas: sharding_db\n" + "    root2:\n" + "      password: root2\n" + "      authorizedSchemas: sharding_db,pr_db";
    
    private AuthenticationChangedListener authenticationChangedListener;
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Before
    public void setUp() {
        authenticationChangedListener = new AuthenticationChangedListener(configurationRepository);
    }
    
    @Test
    public void assertCreateGovernanceEvent() {
        Optional<GovernanceEvent> actual = authenticationChangedListener.createGovernanceEvent(new DataChangedEvent("test", AUTHENTICATION_YAML, Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((AuthenticationChangedEvent) actual.get()).getAuthentication().getUsers().get("root1").getPassword(), is("root1"));
    }
}
