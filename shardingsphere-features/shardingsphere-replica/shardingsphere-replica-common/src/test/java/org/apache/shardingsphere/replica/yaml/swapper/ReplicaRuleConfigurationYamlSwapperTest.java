package org.apache.shardingsphere.replica.yaml.swapper;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replica.api.config.ReplicaDataSourceConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;
import org.apache.shardingsphere.replica.yaml.config.YamlReplicaDataSourceConfiguration;
import org.apache.shardingsphere.replica.yaml.config.YamlReplicaRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public final class ReplicaRuleConfigurationYamlSwapperTest {

    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }

    @Mock
    private ReplicaRuleConfiguration ruleConfig;

    @Test
    public void assertSwapToYamlConfiguration() {
        YamlReplicaRuleConfiguration configuration = getSwapper().swapToYamlConfiguration(createReplicaRuleConfiguration());
        assertThat(configuration.getDataSources().size(), is(1));
        assertTrue(configuration.getDataSources().containsKey("name"));
        assertTrue(configuration.getDataSources().get("name").getReplicaDataSourceNames().contains("replicaSourceNames"));
    }

    private ReplicaRuleConfiguration createReplicaRuleConfiguration() {
        ReplicaDataSourceConfiguration configuration = new ReplicaDataSourceConfiguration("name", Arrays.asList("replicaSourceNames"));
        ReplicaRuleConfiguration replicaRuleConfiguration = new ReplicaRuleConfiguration(Arrays.asList(configuration));
        return replicaRuleConfiguration;
    }

    @Test
    public void assertSwapToObject() {
        ReplicaRuleConfiguration configuration = getSwapper().swapToObject(createYamlReplicaRuleConfiguration());
        assertThat(configuration.getDataSources().size(), is(1));
        Collection<ReplicaDataSourceConfiguration> dataSources = configuration.getDataSources();
        ReplicaDataSourceConfiguration sourceConfiguration = dataSources.stream().findFirst().orElse(null);
        assertNotNull(sourceConfiguration);
        assertThat(sourceConfiguration.getName(), is("dataSources"));
        assertTrue(sourceConfiguration.getReplicaSourceNames().contains("replicaDataSourceNames"));
    }

    private YamlReplicaRuleConfiguration createYamlReplicaRuleConfiguration() {
        YamlReplicaRuleConfiguration result = new YamlReplicaRuleConfiguration();
        YamlReplicaDataSourceConfiguration configuration = new YamlReplicaDataSourceConfiguration();
        configuration.setName("name");
        configuration.setReplicaDataSourceNames(Arrays.asList("replicaDataSourceNames"));
        Map<String, YamlReplicaDataSourceConfiguration> dataSources = new LinkedHashMap<>();
        dataSources.put("dataSources",configuration);
        result.setDataSources(dataSources);
        return result;
    }

    private ReplicaRuleConfigurationYamlSwapper getSwapper() {
        return (ReplicaRuleConfigurationYamlSwapper) OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(ruleConfig), YamlRuleConfigurationSwapper.class).get(ruleConfig);
    }
}
