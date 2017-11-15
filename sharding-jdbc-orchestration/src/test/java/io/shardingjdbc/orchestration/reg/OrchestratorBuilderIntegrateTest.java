package io.shardingjdbc.orchestration.reg;

import com.google.common.collect.Maps;
import io.shardingjdbc.orchestration.api.Orchestrator;
import io.shardingjdbc.orchestration.api.OrchestratorBuilder;
import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;
import io.shardingjdbc.orchestration.util.EmbedTestingServer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class OrchestratorBuilderIntegrateTest {

    @BeforeClass
    public static void before() {
        EmbedTestingServer.start();
    }

    @Test
    public void testBuildZookeeperOrchestor() {
        Map<String, String> registryCenter = Maps.newHashMap();
        registryCenter.put("type", "zk");
        registryCenter.put("namespace", "test");
        registryCenter.put("server-lists", "localhost:3181");
        OrchestratorConfiguration configuration = new OrchestratorConfiguration("test", false, registryCenter);
        Orchestrator orchestrator = OrchestratorBuilder.newBuilder().with(configuration).build();
        assertNotNull(orchestrator);
    }

    @Test
    public void testBuildEtcdOrchestor() {
        Map<String, String> registryCenter = Maps.newHashMap();
        registryCenter.put("type", "etcd");
        registryCenter.put("namespace", "test");
        registryCenter.put("server-lists", "http://localhost:2379");
        OrchestratorConfiguration configuration = new OrchestratorConfiguration("test", false, registryCenter);
        Orchestrator orchestrator = OrchestratorBuilder.newBuilder().with(configuration).build();
        assertNotNull(orchestrator);
    }
}
