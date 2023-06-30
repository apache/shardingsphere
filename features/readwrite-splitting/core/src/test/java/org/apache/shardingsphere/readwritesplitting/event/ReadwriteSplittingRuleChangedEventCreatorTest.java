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

package org.apache.shardingsphere.readwritesplitting.event;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.readwritesplitting.event.datasource.AddReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.datasource.DeleteReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.AlterReadwriteSplittingLoadBalancerEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.DeleteReadwriteSplittingLoadBalancerEvent;
import org.apache.shardingsphere.readwritesplitting.metadata.nodepath.ReadwriteSplittingRuleNodePathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ReadwriteSplittingRuleChangedEventCreatorTest {

    private ReadwriteSplittingRuleChangedEventCreator readwriteSplittingRuleChangedEventCreator;

    @BeforeEach
    void setUp() {
        readwriteSplittingRuleChangedEventCreator = new ReadwriteSplittingRuleChangedEventCreator();
    }

    @Test
    void assertReadWriteSplittingRuleChangeEventForDataSource() {

        assertThat(readwriteSplittingRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                ReadwriteSplittingRuleNodePathProvider.DATA_SOURCES, "test"), instanceOf(DeleteReadwriteSplittingDataSourceEvent.class));

        assertThat(readwriteSplittingRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.ADDED),
                ReadwriteSplittingRuleNodePathProvider.DATA_SOURCES, "test"), instanceOf(AddReadwriteSplittingDataSourceEvent.class));

    }

    @Test
    void assertReadWriteEventsRuleChangeCreatorForLoadBalancers() {
        assertThat(readwriteSplittingRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                ReadwriteSplittingRuleNodePathProvider.LOAD_BALANCERS, "test"), instanceOf(DeleteReadwriteSplittingLoadBalancerEvent.class));

        assertThat(readwriteSplittingRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.ADDED),
                ReadwriteSplittingRuleNodePathProvider.LOAD_BALANCERS, "test"), instanceOf(AlterReadwriteSplittingLoadBalancerEvent.class));
    }

    @Test
    void assertReadWriteEventsRuleChangeCreatorThrowsUnsupportedException() {

        assertThrows(UnsupportedOperationException.class, () -> readwriteSplittingRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                "Invalid Type", "test"));

    }
}
