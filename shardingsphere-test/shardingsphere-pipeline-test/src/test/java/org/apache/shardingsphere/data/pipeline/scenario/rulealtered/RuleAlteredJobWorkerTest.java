package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RuleAlteredJobWorkerTest {
    
    private static JobConfiguration jobConfig;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        jobConfig = JobConfigurationBuilder.createJobConfiguration();
        RuleAlteredJobWorker.initWorkerIfNecessary();
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertCreateRuleAlteredContextNoAlteredRule() {
        jobConfig.setWorkflowConfig(new WorkflowConfiguration("logic_db", ImmutableMap.of(), 0, 1));
        RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
    }
    
    @Test
    public void assertCreateRuleAlteredContextSuccess() {
        final RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        Assert.assertNotNull(ruleAlteredContext.getOnRuleAlteredActionConfig());
    }
}
