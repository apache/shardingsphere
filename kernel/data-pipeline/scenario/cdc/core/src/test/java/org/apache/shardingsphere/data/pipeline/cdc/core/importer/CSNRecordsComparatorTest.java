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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressListener;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;
import java.util.PriorityQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class CSNRecordsComparatorTest {
    
    @Test
    void assertSort() {
        PipelineChannel channel = mock(PipelineChannel.class);
        PriorityQueue<CSNRecords> queue = new PriorityQueue<>(new CSNRecordsComparator());
        CDCChannelProgressPair channelProgressPair = new CDCChannelProgressPair(channel, mock(PipelineJobProgressListener.class));
        queue.add(new CSNRecords(3L, channelProgressPair, Collections.emptyList()));
        queue.add(new CSNRecords(1L, channelProgressPair, Collections.emptyList()));
        queue.add(new CSNRecords(2L, channelProgressPair, Collections.emptyList()));
        assertThat(queue.size(), is(3));
        assertThat(Objects.requireNonNull(queue.poll()).getCsn(), is(1L));
        assertThat(Objects.requireNonNull(queue.poll()).getCsn(), is(2L));
        assertThat(Objects.requireNonNull(queue.poll()).getCsn(), is(3L));
    }
}
