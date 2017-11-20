package io.shardingjdbc.orchestration.reg;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.orchestration.api.Orchestrator;
import io.shardingjdbc.orchestration.internal.OrchestratorImpl;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.GsonFactory;
import io.shardingjdbc.orchestration.internal.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.reg.base.*;
import io.shardingjdbc.orchestration.reg.etcd.*;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.stub.EtcdClientStub;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import static io.shardingjdbc.orchestration.reg.etcd.internal.LocalInstance.getID;
import static java.lang.String.format;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

public class EtcdOrchestratorTest {

    EtcdClient etcdClient;

    @Before
    public void before() {
        etcdClient = new EtcdClientStub();
    }

    @Test
    public void testOrchestrateShardingDatasource() throws Exception {
        ShardingRuleConfiguration shardingRuleConfiguration = createShardingRuleConfiguration();
        ShardingDataSource shardingDataSource = mock(ShardingDataSource.class);

        Orchestrator orchestrator = createOrchestrator();
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        orchestrator.orchestrateShardingDatasource(dataSourceMap, shardingRuleConfiguration, shardingDataSource, new Properties());

        Optional<Map<String, DataSource>> actualDataSourceMap = etcdClient.get("/test/pms/config/datasource")
                .transform(new Function<String, Map<String, DataSource>>() {
            @Override
            public Map<String, DataSource> apply(String input) { return DataSourceJsonConverter.fromJson(input);
            }
        });

        Optional<ShardingRuleConfiguration> acutalShardingRuleConfig = etcdClient.get("/test/pms/config/sharding")
                .transform(new Function<String, ShardingRuleConfiguration>() {
                    @Override
                    public ShardingRuleConfiguration apply(String input) { return ShardingRuleConfigurationConverter.fromJson(input);
                    }
                });


        if (actualDataSourceMap.isPresent() && acutalShardingRuleConfig.isPresent()) {
            assertThat(actualDataSourceMap.get().size(), is(5));
            assertThat(acutalShardingRuleConfig.get().getTableRuleConfigs().size(), is(2));
        } else {
            fail();
        }
    }

    @Test
    public void testOrchestrateShardingDatasourceThenChangeConfig() throws Exception {
        ShardingRuleConfiguration shardingRuleConfiguration = createShardingRuleConfiguration();
        ShardingDataSource shardingDataSource = mock(ShardingDataSource.class);

        Orchestrator orchestrator = createOrchestrator();
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        orchestrator.orchestrateShardingDatasource(dataSourceMap, shardingRuleConfiguration, shardingDataSource, new Properties());

        ((BasicDataSource) dataSourceMap.get("demo_ds_1")).setMaxActive(10);

        etcdClient.put("/test/pms/config/datasource", DataSourceJsonConverter.toJson(dataSourceMap));
        etcdClient.put("/test/pms/config/sharding", ShardingRuleConfigurationConverter.toJson(shardingRuleConfiguration));
        etcdClient.put("/test/pms/config/props", GsonFactory.getGson().toJson(new Properties()));

        verify(shardingDataSource, times(3)).renew(isA(ShardingRule.class), isA(Properties.class));
    }

    @Test
    public void testOrchestrateShardingDatasourceThenDisableInstance() throws Exception {
        ShardingRuleConfiguration shardingRuleConfiguration = createShardingRuleConfiguration();
        ShardingDataSource shardingDataSource = mock(ShardingDataSource.class);

        Orchestrator orchestrator = createOrchestrator();
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        orchestrator.orchestrateShardingDatasource(dataSourceMap, shardingRuleConfiguration, shardingDataSource, new Properties());

        ((BasicDataSource) dataSourceMap.get("demo_ds_1")).setMaxActive(10);

        etcdClient.put(format("/test/pms/state/instances/%s", getID()), StateNodeStatus.DISABLED.name());

        verify(shardingDataSource, times(1)).renew(isA(ShardingRule.class), isA(Properties.class));
    }

    @Test
    public void testOrchestrateMasterSlaveDatasourceThenChangeConfig() throws Exception {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = createMasterSlaveRuleConfiguration();
        MasterSlaveDataSource masterSlaveDataSource = mock(MasterSlaveDataSource.class);

        Orchestrator orchestrator = createOrchestrator();
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        orchestrator.orchestrateMasterSlaveDatasource(dataSourceMap, masterSlaveRuleConfiguration, masterSlaveDataSource);

        etcdClient.put("/test/pms/config/datasource", DataSourceJsonConverter.toJson(dataSourceMap));
        etcdClient.put("/test/pms/config/masterslave", GsonFactory.getGson().toJson(masterSlaveRuleConfiguration));

        verify(masterSlaveDataSource, times(3)).renew(isA(MasterSlaveRule.class));
    }

    @Test
    public void testOrchestrateMasterSlaveDatasourceThenDisableInstance() throws Exception {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = createMasterSlaveRuleConfiguration();
        MasterSlaveDataSource masterSlaveDataSource = mock(MasterSlaveDataSource.class);

        Orchestrator orchestrator = createOrchestrator();
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        orchestrator.orchestrateMasterSlaveDatasource(dataSourceMap, masterSlaveRuleConfiguration, masterSlaveDataSource);

        etcdClient.put(format("/test/pms/state/instances/%s", getID()), StateNodeStatus.DISABLED.name());

        // orchestrator explicitly invoke the renew method when orchestrate master slave data source, so plus the event trigger, it will be two times.
        verify(masterSlaveDataSource, times(2)).renew(isA(MasterSlaveRule.class));
    }

    @Test
    public void testOrchestrateMasterSlaveDatasource() throws Exception {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = createMasterSlaveRuleConfiguration();
        MasterSlaveDataSource masterSlaveDataSource = mock(MasterSlaveDataSource.class);

        Orchestrator orchestrator = createOrchestrator();
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        orchestrator.orchestrateMasterSlaveDatasource(dataSourceMap, masterSlaveRuleConfiguration, masterSlaveDataSource);

        Optional<Map<String, DataSource>> actualDataSourceMap = etcdClient.get("/test/pms/config/datasource")
                .transform(new Function<String, Map<String, DataSource>>() {
                    @Override
                    public Map<String, DataSource> apply(String input) { return DataSourceJsonConverter.fromJson(input);
                    }
                });

        Optional<MasterSlaveRuleConfiguration> acutalMasterSlaveRuleConfig = etcdClient.get("/test/pms/config/masterslave")
                .transform(new Function<String, MasterSlaveRuleConfiguration>() {
                    @Override
                    public MasterSlaveRuleConfiguration apply(String input) { return GsonFactory.getGson().fromJson(input, MasterSlaveRuleConfiguration.class); }
                });


        if (actualDataSourceMap.isPresent() && acutalMasterSlaveRuleConfig.isPresent()) {
            assertThat(actualDataSourceMap.get().size(), is(5));
            assertThat(acutalMasterSlaveRuleConfig.get().getName(), is("demo_master_slave"));
        } else {
            fail();
        }
    }

    private Orchestrator createOrchestrator() {
        final String namespace = "test";
        final String name = "pms";
        final boolean overwrite = true;
        final long timeToLive = 100L;
        CoordinatorRegistryCenter registryCenter = new EtcdRegistryCenter(namespace, timeToLive, etcdClient);
        ConfigurationService configurationService = new EtcdConfigurationServiceImpl(name, overwrite, registryCenter);
        DataSourceService dataSourceService = new EtcdDataSourceServiceImpl(name, configurationService, registryCenter);
        InstanceStateService instanceStateService = new EtcdInstanceStateServiceImpl(name, configurationService, registryCenter);
        return new OrchestratorImpl(configurationService, instanceStateService, dataSourceService);
    }

    private Map<String, DataSource> createDataSourceMap() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/json/datasources.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return DataSourceJsonConverter.fromJson(sb.toString());
    }

    private ShardingRuleConfiguration createShardingRuleConfiguration() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/json/sharding-rule-configuration.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return ShardingRuleConfigurationConverter.fromJson(sb.toString());
    }

    private MasterSlaveRuleConfiguration createMasterSlaveRuleConfiguration() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/json/masterslave-rule-configuration.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return GsonFactory.getGson().fromJson(sb.toString(), MasterSlaveRuleConfiguration.class);
    }


}
