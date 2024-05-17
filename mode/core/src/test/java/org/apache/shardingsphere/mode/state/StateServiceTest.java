package org.apache.shardingsphere.mode.state;

import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StateServiceTest {

    @Mock
    private PersistRepository repository;

    @Test
    void assertPersistClusterStateWithoutPath() {
        StateService stateService = new StateService(repository);
        stateService.persist(ClusterState.OK);
        verify(repository).persist(ComputeNode.getClusterStatusNodePath(), ClusterState.OK.name());
    }

    @Test
    void assertPersistClusterStateWithPath() {
        StateService stateService = new StateService(repository);
        when(repository.getDirectly("/nodes/compute_nodes/status")).thenReturn(ClusterState.OK.name());
        stateService.persist(ClusterState.OK);
        verify(repository, times(0)).persist(ComputeNode.getClusterStatusNodePath(), ClusterState.OK.name());
    }

    @Test
    void assertLoadClusterStatus() {
        new StateService(repository).load();
        verify(repository).getDirectly(ComputeNode.getClusterStatusNodePath());
    }
}
