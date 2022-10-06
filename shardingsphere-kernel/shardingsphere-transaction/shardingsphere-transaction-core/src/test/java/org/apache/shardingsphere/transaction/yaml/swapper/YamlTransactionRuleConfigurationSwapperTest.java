package org.apache.shardingsphere.transaction.yaml.swapper;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.yaml.config.YamlTransactionRuleConfiguration;
import org.junit.Test;

public class YamlTransactionRuleConfigurationSwapperTest {

    private final YamlTransactionRuleConfigurationSwapper swapper = new YamlTransactionRuleConfigurationSwapper();

    @Test
    public void assertSwapToYamlConfiguration() {
        Properties props = new Properties();
        TransactionRuleConfiguration yamlTransactionRuleConfig = new TransactionRuleConfiguration("default", "provider", props);
        YamlTransactionRuleConfiguration actual = swapper.swapToYamlConfiguration(yamlTransactionRuleConfig);
        assertEquals(props, actual.getProps());
        assertEquals("provider", actual.getProviderType());
        assertEquals("default", actual.getDefaultType());
    }

}
