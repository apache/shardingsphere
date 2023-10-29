package org.apache.shardingsphere.data.pipeline.core.job;

import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PipelineJobCenterTest {

    private static final String JOB_ID = "testJobId";
    private static final String NOT_JOB_ID = "nontestJobId";
    private static final int SHARDING_ITEM = 0;
    private static final int NOT_SHARDING_ITEM = 1;

    private PipelineJob mockJob;
    private PipelineTasksRunner mockTasksRunner;
    private PipelineJobItemContext mockJobItemContext;

    @BeforeEach
    void setUp() {
        mockJob = mock(PipelineJob.class);
        mockTasksRunner = mock(PipelineTasksRunner.class);
        mockJobItemContext = mock(PipelineJobItemContext.class);

        when(mockJob.getTasksRunner(SHARDING_ITEM)).thenReturn(Optional.of(mockTasksRunner));
        when(mockTasksRunner.getJobItemContext()).thenReturn(mockJobItemContext);
        when(mockJob.getShardingItems()).thenReturn(Collections.singleton(SHARDING_ITEM));

        PipelineJobCenter.addJob(JOB_ID, mockJob);
    }

    @AfterEach
    void tearDown() {
        PipelineJobCenter.stop(JOB_ID);
    }

    @Test
    void testAddJobAndIsJobExisting() {
        assertTrue(PipelineJobCenter.isJobExisting(JOB_ID));
    }

    @Test
    void testIsJobNonExisting() {
        assertFalse(PipelineJobCenter.isJobExisting(NOT_JOB_ID));
    }

    @Test
    void testStop() {
//        Already stopped by AfterEach
//        PipelineJobCenter.stop(JOB_ID);
        assertFalse(PipelineJobCenter.isJobExisting(JOB_ID));
        verify(mockJob).stop();
    }

    @Test
    void testStopWithNonExistingJob() {
        PipelineJobCenter.stop(NOT_JOB_ID);
        assertFalse(PipelineJobCenter.isJobExisting(NOT_JOB_ID));
    }

    @Test
    void testGetJobItemContext() {
        Optional<PipelineJobItemContext> jobItemContext = PipelineJobCenter.getJobItemContext(JOB_ID, SHARDING_ITEM);
        assertTrue(jobItemContext.isPresent());
        assertEquals(mockJobItemContext, jobItemContext.get());
    }

    @Test
    void testGetJobItemContextWithNonExistingJob() {
        Optional<PipelineJobItemContext> jobItemContext = PipelineJobCenter.getJobItemContext(NOT_JOB_ID, SHARDING_ITEM);
        assertFalse(jobItemContext.isPresent());
    }

    @Test
    void testGetJobItemContextWithNonExistingShardingItem() {
        Optional<PipelineJobItemContext> jobItemContext = PipelineJobCenter.getJobItemContext(JOB_ID, NOT_SHARDING_ITEM);
        assertFalse(jobItemContext.isPresent());
    }

    @Test
    void testGetShardingItems() {
        Collection<Integer> shardingItems = PipelineJobCenter.getShardingItems(JOB_ID);
        assertNotNull(shardingItems);
        assertEquals(NOT_SHARDING_ITEM, shardingItems.size());
        assertTrue(shardingItems.contains(SHARDING_ITEM));
    }

    @Test
    void testGetShardingItemsWithNonExistingJob() {
        Collection<Integer> shardingItems = PipelineJobCenter.getShardingItems(NOT_JOB_ID);
        assertNotNull(shardingItems);
        assertTrue(shardingItems.isEmpty());
    }
}
