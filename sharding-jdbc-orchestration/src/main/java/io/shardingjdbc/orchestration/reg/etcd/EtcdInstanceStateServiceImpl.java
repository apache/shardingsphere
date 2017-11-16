package io.shardingjdbc.orchestration.reg.etcd;

import com.google.common.base.Function;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.orchestration.internal.jdbc.datasource.CircuitBreakerDataSource;
import io.shardingjdbc.orchestration.internal.util.IpUtils;
import io.shardingjdbc.orchestration.reg.base.*;
import io.shardingjdbc.orchestration.reg.etcd.internal.RegistryPath;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import io.shardingjdbc.orchestration.reg.exception.RegExceptionHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author junxiong
 */
@Slf4j
public class EtcdInstanceStateServiceImpl implements InstanceStateService {
    private static final String DELIMITER = "@-@";
    private static final String PID_FLAG = "@";

    private ConfigurationService configurationService;
    private CoordinatorRegistryCenter registryCenter;
    private RegistryPath instanceStatePath;

    EtcdInstanceStateServiceImpl(String name, ConfigurationService configurationService, CoordinatorRegistryCenter registryCenter) {
        this.configurationService = configurationService;
        this.registryCenter = registryCenter;
        this.instanceStatePath = RegistryPath.from(name, "state", "instances");
    }

    private String localInstanceID() {
        return IpUtils.getIp() + DELIMITER + ManagementFactory.getRuntimeMXBean().getName().split(PID_FLAG)[0];
    }

    @Override
    public void persistShardingInstanceOnline(final ShardingDataSource shardingDataSource) {
        String key = instanceStatePath.join(localInstanceID()).asNodeKey();
        registryCenter.persistEphemeral(key, StateNodeStatus.ENABLED.name());
        addInstancesStateChangeListener(new Function<Map<String, DataSource>, Void>() {
            @Override
            public Void apply(Map<String, DataSource> dataSourceMap) {
                try {
                    shardingDataSource.renew(configurationService.loadShardingRuleConfiguration().build(dataSourceMap),
                            configurationService.loadShardingProperties());
                } catch (SQLException e) {
                    log.error("Can not build sharding rule", e);
                    RegExceptionHandler.handleException(e);
                }
                return null;
            }
        });

    }

    @Override
    public void persistMasterSlaveInstanceOnline(final MasterSlaveDataSource masterSlaveDataSource) {
        String key = instanceStatePath.join(localInstanceID()).asNodeKey();
        registryCenter.persistEphemeral(key, StateNodeStatus.ENABLED.name());
        addInstancesStateChangeListener(new Function<Map<String, DataSource>, Void> () {
            @Override
            public Void apply(Map<String, DataSource> dataSourceMap) {
                masterSlaveDataSource.renew(configurationService.loadMasterSlaveRuleConfiguration().build(dataSourceMap));
                return null;
            }
        });
    }

    private void addInstancesStateChangeListener(final Function<Map<String, DataSource>, Void> callback) {
        registryCenter.addRegistryChangeListener(instanceStatePath.join(localInstanceID()).asNodeKey(), new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                if (RegistryChangeType.UPDATED == registryChangeEvent.getType() && registryChangeEvent.getPayload().isPresent()) {
                    String instanceStateKey = registryChangeEvent.getPayload().get().getKey();
                    Map<String, DataSource> dataSourceMap = configurationService.loadDataSourceMap();
                    if (StateNodeStatus.DISABLED.toString().equalsIgnoreCase(registryCenter.get(instanceStateKey))) {
                        for (String each : dataSourceMap.keySet()) {
                            dataSourceMap.put(each, new CircuitBreakerDataSource());
                        }
                    }
                    callback.apply(dataSourceMap);

                }
            }
        });
    }
}
