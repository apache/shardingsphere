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

package org.apache.shardingsphere.governance.core.registry.state.watcher;

import org.apache.shardingsphere.governance.core.schema.GovernanceSchema;
import org.apache.shardingsphere.governance.core.registry.state.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.GovernanceEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataSourceStateChangedWatcherTest {
    
    @Test
    public void assertCreateEvent() {
        Optional<GovernanceEvent> actual = new DataSourceStateChangedWatcher().createGovernanceEvent(
                new DataChangedEvent("/states/datanodes/replica_query_db/replica_ds_0", "disabled", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((DisabledStateChangedEvent) actual.get()).getGovernanceSchema().getSchemaName(), is(new GovernanceSchema("replica_query_db", "replica_ds_0").getSchemaName()));
    }
}
