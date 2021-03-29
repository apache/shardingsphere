package org.apache.shardingsphere.test.integration.junit.compose;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.junit.container.governance.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.impl.H2Container;
import org.apache.shardingsphere.test.integration.junit.container.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.integration.junit.logging.ContainerLogs;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class GovernanceContainerCompose extends ExternalResource implements AutoCloseable {
    
    private final Network network = Network.newNetwork();
    
    private final String clusterName;
    
    private final ParameterizedArray parameterizedArray;
    
    private final ImmutableList<ShardingSphereContainer> containers;
    
    @Getter
    private final ShardingSphereStorageContainer storageContainer;
    
    private final ShardingSphereAdapterContainer proxy1;
    
    private final ShardingSphereAdapterContainer proxy2;
    
    private final ZookeeperContainer zookeeperContainer;
    
    private volatile boolean started;
    
    public GovernanceContainerCompose(final String clusterName, final ParameterizedArray parameterizedArray) {
        this.clusterName = clusterName;
        this.parameterizedArray = parameterizedArray;
        this.storageContainer = createStorageContainer();
        this.proxy1 = createAdapterContainer();
        this.proxy2 = createAdapterContainer();
        this.zookeeperContainer = createZookeeperContainer();
        zookeeperContainer.setNetworkAliases(Lists.newArrayList("zk"));
        
        proxy1.dependsOn(storageContainer, zookeeperContainer);
        proxy2.dependsOn(storageContainer, zookeeperContainer);
        
        this.containers = ImmutableList.of(storageContainer, proxy1, proxy2, zookeeperContainer);
    }
    
    private ZookeeperContainer createZookeeperContainer() {
        return new ZookeeperContainer(parameterizedArray);
    }
    
    private ShardingSphereAdapterContainer createAdapterContainer() {
        Supplier<ShardingSphereAdapterContainer> supplier = () -> {
            switch (parameterizedArray.getAdapter()) {
                case "proxy":
                    return new ShardingSphereProxyContainer(parameterizedArray);
                case "jdbc":
                    return new ShardingSphereJDBCContainer(parameterizedArray);
                default:
                    throw new RuntimeException("Adapter[" + parameterizedArray.getAdapter() + "] is unknown.");
                
            }
        };
        ShardingSphereAdapterContainer adapterContainer = supplier.get();
        adapterContainer.setNetwork(network);
        adapterContainer.withLogConsumer(ContainerLogs.newConsumer(this.clusterName + "-adapter"));
        return adapterContainer;
    }
    
    private ShardingSphereStorageContainer createStorageContainer() {
        Supplier<ShardingSphereStorageContainer> supplier = () -> {
            switch (parameterizedArray.getDatabaseType().getName()) {
                case "MySQL":
                    return new MySQLContainer(parameterizedArray);
                case "H2":
                    return new H2Container(parameterizedArray);
                default:
                    throw new RuntimeException("Unknown storage type " + parameterizedArray.getDatabaseType());
            }
        };
        ShardingSphereStorageContainer storageContainer = supplier.get();
        storageContainer.setNetwork(network);
        storageContainer.withLogConsumer(ContainerLogs.newConsumer(this.clusterName + "-storage"));
        storageContainer.setNetworkAliases(Lists.newArrayList("mysql.sharding-governance.host"));
        return storageContainer;
    }
    
    /**
     * Startup.
     */
    public void start() {
        containers.stream().filter(each -> !each.isCreated()).forEach(GenericContainer::start);
    }
    
    /**
     * Wait until all containers ready.
     */
    public void waitUntilReady() {
        containers.stream()
                .filter(each -> {
                    try {
                        return !each.isHealthy();
                        // CHECKSTYLE:OFF
                    } catch (final RuntimeException ex) {
                        // CHECKSTYLE:ON
                        return false;
                    }
                })
                .forEach(each -> {
                    while (!(each.isRunning() && each.isHealthy())) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (final InterruptedException ignored) {
                        }
                    }
                });
        started = true;
        log.info("Any container is startup.");
    }
    
    @Override
    protected void before() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    start();
                    waitUntilReady();
                }
            }
        }
    }
    
    @Override
    public void close() {
        containers.forEach(Startable::close);
    }
}
