package org.apache.shardingsphere.agent.plugin.core.util;

import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TimeRecorderTest {

    @Test
    public void recordedElapsedTimeIsCorrectWhenCurrentRecorderIsPresent() throws InterruptedException {
        TimeRecorder.INSTANCE.record();
        Thread.sleep(5);
        assertTrue(TimeRecorder.INSTANCE.getElapsedTime() >= 5);
    }

    @Test
    public void elapsedTimeThrowsNullPointerExceptionWhenCurrentRecorderIsNotPresent() {
        TimeRecorder.INSTANCE.clean();
        assertThrows(NullPointerException.class, TimeRecorder.INSTANCE::getElapsedTime);
    }
}
