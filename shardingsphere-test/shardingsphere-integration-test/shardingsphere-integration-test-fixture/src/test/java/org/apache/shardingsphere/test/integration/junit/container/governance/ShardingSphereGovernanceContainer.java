package org.apache.shardingsphere.test.integration.junit.container.governance;

import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

public abstract class ShardingSphereGovernanceContainer extends ShardingSphereContainer {
    
    public ShardingSphereGovernanceContainer(String dockerName, String dockerImageName, boolean isFakeContainer, ParameterizedArray parameterizedArray) {
        super(dockerName, dockerImageName, isFakeContainer, parameterizedArray);
    }

    /**
     * Get governance configuration.
     *
     * @return governance configuration
     */
    public YamlGovernanceConfiguration getGovernanceConfiguration() {
        YamlGovernanceConfiguration result = new YamlGovernanceConfiguration();
        result.setName("governance_ds");
        result.setOverwrite(true);
        result.setRegistryCenter(createGovernanceCenterConfiguration());
        return result;
    }
    
    /**
     * Get server lists.
     *
     * @return server lists
     */
    public abstract String getServerLists();
    
    protected abstract YamlGovernanceCenterConfiguration createGovernanceCenterConfiguration();
}
