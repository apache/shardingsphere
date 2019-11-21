/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.state;

import io.shardingsphere.orchestration.internal.registry.state.listener.DataSourceStateChangedListenerTest;
import io.shardingsphere.orchestration.internal.registry.state.listener.InstanceStateChangedListenerTest;
import io.shardingsphere.orchestration.internal.registry.state.listener.StateChangedListenerManagerTest;
import io.shardingsphere.orchestration.internal.registry.state.node.StateNodeTest;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchemaGroupTest;
import io.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchemaTest;
import io.shardingsphere.orchestration.internal.registry.state.service.StateServiceTest;
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
