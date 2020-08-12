package org.apache.shardingsphere.cluster.facade;

import lombok.SneakyThrows;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.ClusterHeartbeatInstance;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResponse;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResult;
import org.apache.shardingsphere.cluster.state.ClusterStateInstance;
import org.apache.shardingsphere.cluster.state.InstanceState;
import org.apache.shardingsphere.control.panel.spi.ControlPanelConfiguration;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterFacadeTest {
    
    private static final ClusterFacade INSTANCE = ClusterFacade.getInstance();
    
    @Mock
    private ClusterHeartbeatInstance clusterHeartbeatInstance;
    
    @Mock
    private ClusterStateInstance clusterStateInstance;
    
    @Test
    public void assertDetectHeartbeatBeforeInit() {
        INSTANCE.detectHeartbeat(new HashMap<>());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    public void assertDetectHeartbeat() {
        init();
        FieldSetter.setField(INSTANCE, ClusterFacade.class.getDeclaredField("clusterHeartbeatInstance"), clusterHeartbeatInstance);
        FieldSetter.setField(INSTANCE, ClusterFacade.class.getDeclaredField("clusterStateInstance"), clusterStateInstance);
        when(clusterHeartbeatInstance.detect(anyMap())).thenReturn(buildHeartbeatResponse());
        when(clusterStateInstance.loadInstanceState()).thenReturn(new InstanceState());
        INSTANCE.detectHeartbeat(new HashMap<>());
    }
    
    private void init() {
        HeartbeatConfiguration heartBeatConfig = new HeartbeatConfiguration();
        heartBeatConfig.setSql("select 1");
        heartBeatConfig.setInterval(60);
        heartBeatConfig.setRetryEnable(true);
        heartBeatConfig.setRetryMaximum(3);
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setHeartbeat(heartBeatConfig);
        List<ControlPanelConfiguration> controlPanelConfigs = new LinkedList<>();
        controlPanelConfigs.add(clusterConfiguration);
        new ControlPanelFacadeEngine().init(controlPanelConfigs);
    }
    
    private HeartbeatResponse buildHeartbeatResponse() {
         return new HeartbeatResponse(buildHeartbeatResult());
    }
    
    private Map<String, Collection<HeartbeatResult>> buildHeartbeatResult() {
        Map<String, Collection<HeartbeatResult>> result = new HashMap<>();
        Collection<HeartbeatResult> heartbeatResults = new ArrayList<>();
        heartbeatResults.add(new HeartbeatResult("ds_0", true, System.currentTimeMillis(), false));
        heartbeatResults.add(new HeartbeatResult("ds_1", true, System.currentTimeMillis(), false));
        result.put("logic_db", heartbeatResults);
        return result;
    }
}
