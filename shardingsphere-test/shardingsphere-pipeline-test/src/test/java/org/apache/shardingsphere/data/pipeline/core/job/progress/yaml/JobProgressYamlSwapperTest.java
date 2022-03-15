package org.apache.shardingsphere.data.pipeline.core.job.progress.yaml;

import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.util.ConfigurationFileUtil;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;      


public final class JobProgressYamlSwapperTest {

    private static final JobProgressYamlSwapper JOB_PROGRESS_YAML_SWAPPER = new JobProgressYamlSwapper();

    private JobProgress getJobProgress(final String data) {
        return JOB_PROGRESS_YAML_SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlJobProgress.class));
    }

    @Test
    public void assertFullSwapToYaml() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress.yaml"));
        YamlJobProgress result = JOB_PROGRESS_YAML_SWAPPER.swapToYaml(jobProgress);
        assertThat(result.getStatus(), is("RUNNING"));
        assertThat(result.getSourceDatabaseType(), is("H2"));
        assertThat(result.getInventory().getFinished().length, is(2));
        assertArrayEquals(result.getInventory().getFinished(), new String[]{"ds0.t_2", "ds0.t_1"});
        assertThat(result.getInventory().getUnfinished().size(), is(2));
        assertThat(result.getInventory().getUnfinished().get("ds1.t_2"), is("1,2"));
        assertThat(result.getInventory().getUnfinished().get("ds1.t_1"), is(""));
        assertThat(result.getIncremental().size(), is(1));
        assertTrue(result.getIncremental().containsKey("ds0"));
        assertNull(result.getIncremental().get("position"));
    }

    @Test
    public void assertNullIncremental() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress-no-finished.yaml"));
        YamlJobProgress result = JOB_PROGRESS_YAML_SWAPPER.swapToYaml(jobProgress);
        assertTrue(result.getIncremental().isEmpty());
    }

    @Test
    public void assertNullInventory() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress-no-inventory.yaml"));
        YamlJobProgress result = JOB_PROGRESS_YAML_SWAPPER.swapToYaml(jobProgress);
        assertThat(result.getInventory().getFinished().length, is(0));
    }
}
