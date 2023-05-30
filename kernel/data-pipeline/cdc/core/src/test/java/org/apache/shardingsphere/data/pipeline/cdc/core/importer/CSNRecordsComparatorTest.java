package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.PriorityQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class CSNRecordsComparatorTest {
    
    @Test
    void assertSort() {
        PipelineChannel channel = mock(PipelineChannel.class);
        PriorityQueue<CSNRecords> queue = new PriorityQueue<>(new CSNRecordsComparator());
        queue.add(new CSNRecords(3L, channel, Collections.emptyList()));
        queue.add(new CSNRecords(1L, channel, Collections.emptyList()));
        queue.add(new CSNRecords(2L, channel, Collections.emptyList()));
        assertThat(queue.size(), is(3));
        assertThat(queue.poll().getCsn(), is(1L));
        assertThat(queue.poll().getCsn(), is(2L));
        assertThat(queue.poll().getCsn(), is(3L));
    }
}
