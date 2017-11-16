package io.shardingjdbc.orchestration.reg;

import com.google.common.base.Optional;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.Orchestrator;
import io.shardingjdbc.orchestration.internal.OrchestratorImpl;
import io.shardingjdbc.orchestration.internal.json.DataSourceJsonConverter;
import io.shardingjdbc.orchestration.internal.json.ShardingRuleConfigurationConverter;
import io.shardingjdbc.orchestration.reg.base.ConfigurationService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.DataSourceService;
import io.shardingjdbc.orchestration.reg.base.InstanceStateService;
import io.shardingjdbc.orchestration.reg.etcd.*;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdClient;
import io.shardingjdbc.orchestration.reg.stub.EtcdClientStub;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class EtcdOrchestratorTest {

    EtcdClient etcdClient;

    @Before
    public void before() {
        etcdClient = new EtcdClientStub();
    }

    @Test
    public void testOrchestrateShardingDatasource() throws Exception {
        ShardingRuleConfiguration shardingRuleConfiguration = createShardingRuleConfiguration();
        Properties props = new Properties();
        ShardingDataSource shardingDataSource = mock(ShardingDataSource.class);

        Orchestrator orchestrator = createOrchestrator();
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        orchestrator.orchestrateShardingDatasource(dataSourceMap, shardingRuleConfiguration, shardingDataSource, new Properties());

        Optional<String> datasourcemapJson = etcdClient.get("/test/pms/config/datasource");
        Map<String, DataSource> actualDataSourceMap = DataSourceJsonConverter.fromJson(datasourcemapJson.get());

        assertThat(actualDataSourceMap.size(), is(2));
    }

    @Test
    public void testOrchestrateMasterSlaveDatasource() {
        // TODO
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


}
