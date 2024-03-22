package org.apache.shardingsphere.mask.rule.changed;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class MaskAlgorithmChangedProcessorTest {

    @Test
    public void assertSwapRuleItemConfiguration() {
        MaskAlgorithmChangedProcessor processor = new MaskAlgorithmChangedProcessor();
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        AlgorithmConfiguration algorithmConfiguration = processor.swapRuleItemConfiguration(event, "type: TEST");
        assertThat(algorithmConfiguration.getType(), is("TEST"));
    }

    @Test
    public void assertFindRuleConfiguration() {
        MaskAlgorithmChangedProcessor processor = new MaskAlgorithmChangedProcessor();
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap());
        MaskRuleConfiguration maskRuleConfiguration = processor.findRuleConfiguration(database);
        assertThat(maskRuleConfiguration.getMaskAlgorithms().size(), is(0) );

    }

    // TODO Unsupported Operation
    /*@Test
    public void assertChangeRuleItemConfiguration() {
        MaskAlgorithmChangedProcessor processor = new MaskAlgorithmChangedProcessor();
        AlterNamedRuleItemEvent event = mock(AlterNamedRuleItemEvent.class);
        MaskRuleConfiguration currentRuleConfig = new MaskRuleConfiguration(Collections.emptyList(), Collections.singletonMap("type: TEST1", mock(AlgorithmConfiguration.class)));
        AlgorithmConfiguration toBeChangedItemConfig = processor.swapRuleItemConfiguration(event, "type: TEST2");
        processor.changeRuleItemConfiguration(event, currentRuleConfig, toBeChangedItemConfig);
    }*/

    @Test
    public void assertDropRuleItemConfiguration() {
        MaskAlgorithmChangedProcessor processor = new MaskAlgorithmChangedProcessor();
        DropNamedRuleItemEvent event = mock(DropNamedRuleItemEvent.class);
        MaskRuleConfiguration currentRuleConfig = new MaskRuleConfiguration(Collections.emptyList(), Collections.singletonMap("type: TEST", mock(AlgorithmConfiguration.class)));
        processor.dropRuleItemConfiguration(event, currentRuleConfig);
    }

    @Test
    public void assertGetType() {
        MaskAlgorithmChangedProcessor processor = new MaskAlgorithmChangedProcessor();
        String type = processor.getType();
        assertEquals("mask.mask_algorithms", type);
    }
}
