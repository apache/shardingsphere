package org.apache.shardingsphere.data.pipeline.core.job;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineJobCenterTest {

    @Test
    void addJob(){
        PipelineJob pipelineJob=mock(PipelineJob.class);
        PipelineJobCenter.addJob("12",pipelineJob);
        Assertions.assertEquals(pipelineJob,PipelineJobCenter.getJob("12"));
        Assertions.assertTrue(PipelineJobCenter.isJobExisting("12"));
    }

    @Test
    void isJobExisting() {
        PipelineJob pipelineJob=mock(PipelineJob.class);
        PipelineJobCenter.addJob("12",pipelineJob);
        Assertions.assertTrue(PipelineJobCenter.isJobExisting("12"));
        assertNotNull(PipelineJobCenter.getJob("12"));
    }

    @Test
    void getJob() {
        PipelineJob pipelineJob=mock(PipelineJob.class);
        PipelineJobCenter.addJob("13",pipelineJob);
        assertTrue(PipelineJobCenter.isJobExisting("13"));
        Assertions.assertEquals(pipelineJob,PipelineJobCenter.getJob("13"));
    }

    @Test
    void getJobItemContext() {
        PipelineJob pipelineJob=mock(PipelineJob.class);
        PipelineTasksRunner pipelineTasksRunner=mock(PipelineTasksRunner.class);
        PipelineJobItemContext pipelineJobItemContext=mock(PipelineJobItemContext.class);
        when(pipelineJob.getTasksRunner(anyInt())).thenReturn(Optional.of(pipelineTasksRunner));
        when(pipelineTasksRunner.getJobItemContext()).thenReturn(pipelineJobItemContext);
        PipelineJobCenter.addJob("111",pipelineJob);
        Optional<PipelineJobItemContext> result=PipelineJobCenter.getJobItemContext("111",1);
        assertTrue(result.isPresent());
        assertEquals(pipelineJobItemContext,result);
    }

    @Test
    void getShardingItems() {
        PipelineJob pipelineJob=new PipelineJob() {
            @Override
            public Optional<PipelineTasksRunner> getTasksRunner(int shardingItem) {
                return Optional.empty();
            }

            @Override
            public Collection<Integer> getShardingItems() {
                Collection<Integer> collection=new ArrayList<>();
                collection.add(2);
                collection.add(3);
                return collection;
            }

            @Override
            public void stop() {

            }
        };
        PipelineJobCenter.addJob("11",pipelineJob);
        Collection<Integer> testCollection=new ArrayList<>();
        testCollection.add(2);
        testCollection.add(3);
        Assertions.assertFalse(pipelineJob.getShardingItems().isEmpty());
        Assertions.assertEquals(testCollection,PipelineJobCenter.getShardingItems("11"));
    }
}