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

package org.apache.shardingsphere.data.pipeline.core.task.progress;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class IncrementalTaskProgressTest {
    
    @Test
    void assertGetPosition() {
        IngestPosition position1 = mock(IngestPosition.class);
        IncrementalTaskProgress taskProgress = new IncrementalTaskProgress(position1);
        assertThat(taskProgress.getPosition(), is(position1));
        IngestPosition position2 = mock(IngestPosition.class);
        taskProgress.setPosition(position2);
        assertThat(taskProgress.getPosition(), is(position2));
    }
    
    @Test
    void assertGetIncrementalTaskDelay() {
        IncrementalTaskProgress taskProgress = new IncrementalTaskProgress(mock(IngestPosition.class));
        assertThat(taskProgress.getIncrementalTaskDelay().getLastEventTimestamps(), is(0L));
        IncrementalTaskDelay taskDelay = new IncrementalTaskDelay();
        taskDelay.setLastEventTimestamps(1L);
        taskProgress.setIncrementalTaskDelay(taskDelay);
        assertThat(taskProgress.getIncrementalTaskDelay().getLastEventTimestamps(), is(1L));
    }
}
