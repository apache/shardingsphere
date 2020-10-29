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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceStateChangedListenerTest {
    
    private DataSourceStateChangedListener dataSourceStateChangedListener;
    
    @Mock
    private RegistryRepository registryRepository;
    
    @Before
    public void setUp() {
        dataSourceStateChangedListener = new DataSourceStateChangedListener(registryRepository, Arrays.asList("sharding_db", "replica_query_db", "encrypt_db"));
    }
    
    @Test
    public void assertCreateGovernanceEvent() {
        Optional<GovernanceEvent> actual = dataSourceStateChangedListener.createGovernanceEvent(
                new DataChangedEvent("/states/datanodes/replica_query_db/replica_ds_0", "disabled", ChangedType.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((DisabledStateChangedEvent) actual.get()).getGovernanceSchema().getSchemaName(), is(new GovernanceSchema("replica_query_db", "replica_ds_0").getSchemaName()));
    }
}
