package org.apache.shardingsphere.traffic.yaml.swapper;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficStrategyConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class TrafficStrategyConfigurationYamlSwapperTest {

    private static final String NAME = "testName";

    private static final String TEST_LABEL_ONE = "testLabelOne";

    private static final String TEST_LABEL_TWO = "testLabelTwo";

    private static final String ALGORITHM_NAME = "algorithmName";

    private static final String LOAD_BALANCER_NAME = "testLoadBalancerName";

    private static final List<String> LABELS = Lists.newArrayList(TEST_LABEL_ONE, TEST_LABEL_TWO);

    private final TrafficStrategyConfigurationYamlSwapper yamlSwapper = new TrafficStrategyConfigurationYamlSwapper();

    @Test
    public void swapToYamlConfiguration() {
        YamlTrafficStrategyConfiguration yamlTrafficStrategyConfiguration = yamlSwapper.swapToYamlConfiguration(createTrafficStrategyConfiguration());
        assertThat(yamlTrafficStrategyConfiguration.getName(), is(NAME));
        assertThat(yamlTrafficStrategyConfiguration.getLabels(), is(LABELS));
        assertThat(yamlTrafficStrategyConfiguration.getAlgorithmName(), is(ALGORITHM_NAME));
        assertThat(yamlTrafficStrategyConfiguration.getLoadBalancerName(), is(LOAD_BALANCER_NAME));
    }

    private TrafficStrategyConfiguration createTrafficStrategyConfiguration() {
        return new TrafficStrategyConfiguration(NAME, LABELS, ALGORITHM_NAME, LOAD_BALANCER_NAME);
    }

    @Test
    public void swapToObject() {
        TrafficStrategyConfiguration trafficStrategyConfiguration = yamlSwapper.swapToObject(createYamlTrafficStrategyConfiguration());
        assertThat(trafficStrategyConfiguration.getName(), is(NAME));
        assertThat(trafficStrategyConfiguration.getLabels(), is(LABELS));
        assertThat(trafficStrategyConfiguration.getAlgorithmName(), is(ALGORITHM_NAME));
        assertThat(trafficStrategyConfiguration.getLoadBalancerName(), is(LOAD_BALANCER_NAME));
    }

    private YamlTrafficStrategyConfiguration createYamlTrafficStrategyConfiguration() {
        YamlTrafficStrategyConfiguration yamlTrafficStrategyConfiguration = new YamlTrafficStrategyConfiguration();
        yamlTrafficStrategyConfiguration.setName(NAME);
        yamlTrafficStrategyConfiguration.setLabels(LABELS);
        yamlTrafficStrategyConfiguration.setAlgorithmName(ALGORITHM_NAME);
        yamlTrafficStrategyConfiguration.setLoadBalancerName(LOAD_BALANCER_NAME);
        return yamlTrafficStrategyConfiguration;
    }

}
