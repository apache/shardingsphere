package org.apache.shardingsphere.sharding.schedule;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.OutputConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShardingRuleAlteredJobConfigurationPreparerTest {
    
    private static ShardingRuleAlteredJobConfigurationPreparer preparer = new ShardingRuleAlteredJobConfigurationPreparer();
    
    private static PipelineConfiguration pipelineConfiguration = new PipelineConfiguration();
    
    @Mock
    private OnRuleAlteredActionConfiguration mockOnRuleAlteredActionConfiguration;
    
    private WorkflowConfiguration workflowConfiguration;
    
    @BeforeClass
    public static void beforeClass() throws IOException {
        URL sourceUrl = ShardingRuleAlteredJobConfigurationPreparerTest.class.getClassLoader().getResource("yaml/alter_rule_source.yaml");
        assertNotNull(sourceUrl);
        YamlPipelineDataSourceConfiguration source = YamlEngine.unmarshal(new File(sourceUrl.getFile()), YamlPipelineDataSourceConfiguration.class);
        
        pipelineConfiguration.setSource(source);
    }
    
    @Before
    public void setUp() {
        workflowConfiguration = new WorkflowConfiguration("logic_db", ImmutableMap.of(YamlShardingRuleConfiguration.class.getName(),
                Collections.singletonList("t_order")), 0, 1);
        when(mockOnRuleAlteredActionConfiguration.getOutput()).thenReturn(new OutputConfiguration(5, 1000, null));
    }
    
    @Test
    public void testAutoTableRuleCreateTaskConfiguration() throws IOException {
        URL targetUrl = ShardingRuleAlteredJobConfigurationPreparerTest.class.getClassLoader().getResource("yaml/auto_table_alter_rule_target.yaml");
        assertNotNull(targetUrl);
        YamlPipelineDataSourceConfiguration target = YamlEngine.unmarshal(new File(targetUrl.getFile()), YamlPipelineDataSourceConfiguration.class);
        pipelineConfiguration.setTarget(target);
        final JobConfiguration jobConfiguration = new JobConfiguration(workflowConfiguration, pipelineConfiguration);
        jobConfiguration.buildHandleConfig();
        final TaskConfiguration taskConfiguration = preparer.createTaskConfiguration(pipelineConfiguration, jobConfiguration.getHandleConfig(), mockOnRuleAlteredActionConfiguration);
        assertEquals(taskConfiguration.getHandleConfig().getLogicTables(), "t_order");
    }
    
    @Test
    public void testTableRuleCreateTaskConfiguration() throws IOException {
        URL targetUrl = ShardingRuleAlteredJobConfigurationPreparerTest.class.getClassLoader().getResource("yaml/table_alter_rule_target.yaml");
        assertNotNull(targetUrl);
        YamlPipelineDataSourceConfiguration target = YamlEngine.unmarshal(new File(targetUrl.getFile()), YamlPipelineDataSourceConfiguration.class);
        pipelineConfiguration.setTarget(target);
        
        final JobConfiguration jobConfiguration = new JobConfiguration(workflowConfiguration, pipelineConfiguration);
        jobConfiguration.buildHandleConfig();
        final TaskConfiguration taskConfiguration = preparer.createTaskConfiguration(pipelineConfiguration, jobConfiguration.getHandleConfig(), mockOnRuleAlteredActionConfiguration);
        assertEquals(taskConfiguration.getHandleConfig().getLogicTables(), "t_order");
    }
}
