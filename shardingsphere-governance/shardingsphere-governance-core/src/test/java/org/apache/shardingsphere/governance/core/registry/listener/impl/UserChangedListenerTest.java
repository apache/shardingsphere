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
import org.apache.shardingsphere.governance.core.registry.listener.event.authority.AuthorityChangedEvent;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
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
public final class UserChangedListenerTest {
    
    private static final String AUTHENTICATION_YAML = "- root1@:root1\n" + "- root2@:root2\n";
    
    private UserChangedListener userChangedListener;
    
    @Mock
    private RegistryRepository registryRepository;
    
    @Before
    public void setUp() {
        userChangedListener = new UserChangedListener(registryRepository);
    }
    
    @Test
    public void assertCreateEvent() {
        Optional<GovernanceEvent> actual = userChangedListener.createEvent(new DataChangedEvent("test", AUTHENTICATION_YAML, Type.UPDATED));
        assertTrue(actual.isPresent());
        Optional<ShardingSphereUser> user = ((AuthorityChangedEvent) actual.get()).getUsers().stream().filter(each -> each.getGrantee().equals(new Grantee("root1", ""))).findFirst();
        assertTrue(user.isPresent());
        assertThat(user.get().getPassword(), is("root1"));
    }
}
