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

package org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler;

import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.impl.BarrierMetaDataChangedEventHandler;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.impl.ChangedJobConfigurationDispatcher;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertTrue;

public final class PipelineMetaDataChangedEventHandlerFactoryTest {
    
    @Test
    public void assertFindInstance() {
        Collection<PipelineMetaDataChangedEventHandler> actual = PipelineMetaDataChangedEventHandlerFactory.findAllInstances();
        boolean isContainMigration = false;
        boolean isContainBarrier = false;
        for (PipelineMetaDataChangedEventHandler each : actual) {
            if (each instanceof ChangedJobConfigurationDispatcher) {
                isContainMigration = true;
                continue;
            }
            if (each instanceof BarrierMetaDataChangedEventHandler) {
                isContainBarrier = true;
            }
        }
        assertTrue(isContainMigration);
        assertTrue(isContainBarrier);
    }
}
