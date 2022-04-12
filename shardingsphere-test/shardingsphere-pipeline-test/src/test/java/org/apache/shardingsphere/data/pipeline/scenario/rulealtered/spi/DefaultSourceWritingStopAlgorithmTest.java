package org.apache.shardingsphere.data.pipeline.scenario.rulealtered.spi;

import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.junit.Before;
import org.junit.Test;

public class DefaultSourceWritingStopAlgorithmTest {

    private final  DefaultSourceWritingStopAlgorithm defaultSourceWritingStopAlgorithm = new DefaultSourceWritingStopAlgorithm();

    private final String lockName = "lock1";
    private final String jobId = "jobId1";

    @Before
    public void setup() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }

    @Test
    public void assertSuccessLockReleaseLock() {
        defaultSourceWritingStopAlgorithm.lock(lockName,jobId);
        defaultSourceWritingStopAlgorithm.releaseLock(lockName,jobId);
    }


    @Test
    public void assertSuccessLockTwiceReleaseLock() {
        defaultSourceWritingStopAlgorithm.lock(lockName,jobId);
        defaultSourceWritingStopAlgorithm.lock(lockName,jobId);
        defaultSourceWritingStopAlgorithm.releaseLock(lockName,jobId);
    }


    @Test
    public void assertSuccessLockReleaseLockTwice() {
        defaultSourceWritingStopAlgorithm.lock(lockName,jobId);
        defaultSourceWritingStopAlgorithm.releaseLock(lockName,jobId);
        defaultSourceWritingStopAlgorithm.releaseLock(lockName,jobId);
    }

    @Test
    public void assertSuccessReleaseNullLock() {
        defaultSourceWritingStopAlgorithm.releaseLock(lockName,jobId);
    }

}
