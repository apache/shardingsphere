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

package org.apache.shardingsphere.orchestration.internal.registry.state;

import org.apache.shardingsphere.orchestration.internal.registry.state.listener.DataSourceStateChangedListenerTest;
import org.apache.shardingsphere.orchestration.internal.registry.state.listener.InstanceStateChangedListenerTest;
import org.apache.shardingsphere.orchestration.internal.registry.state.listener.StateChangedListenerManagerTest;
import org.apache.shardingsphere.orchestration.internal.registry.state.node.StateNodeTest;
import org.apache.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchemaGroupTest;
import org.apache.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchemaTest;
import org.apache.shardingsphere.orchestration.internal.registry.state.service.StateServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        StateNodeTest.class, 
        OrchestrationShardingSchemaTest.class, 
        OrchestrationShardingSchemaGroupTest.class, 
        StateServiceTest.class,
        StateChangedListenerManagerTest.class,
        DataSourceStateChangedListenerTest.class, 
        InstanceStateChangedListenerTest.class
})
public final class AllStateTests {
}
