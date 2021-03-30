package org.apache.shardingsphere.test.integration.junit.compose;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.governance.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

@Slf4j
public final class GovernanceContainerCompose extends ContainerCompose {
    
    @Getter
    private final ShardingSphereStorageContainer storageContainer;
    
    @Getter
    private final ShardingSphereAdapterContainer adapterContainer;
    
    public GovernanceContainerCompose(final String clusterName, final ParameterizedArray parameterizedArray) {
        super(clusterName, parameterizedArray);
        this.storageContainer = createStorageContainer();
        this.adapterContainer = createAdapterContainer();
        ZookeeperContainer zookeeperContainer = createZookeeperContainer();
        if ("proxy".equals(parameterizedArray.getAdapter())) {
            ShardingSphereAdapterContainer proxy = createAdapterContainer();
            proxy.dependsOn(storageContainer, zookeeperContainer);
        }
        adapterContainer.dependsOn(storageContainer, zookeeperContainer);
    }
    
    private ZookeeperContainer createZookeeperContainer() {
        return createContainer(() -> new ZookeeperContainer(getParameterizedArray()), "zk");
    }
    
}
