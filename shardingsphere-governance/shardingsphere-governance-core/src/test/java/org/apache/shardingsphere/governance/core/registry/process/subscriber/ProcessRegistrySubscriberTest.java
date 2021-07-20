package org.apache.shardingsphere.governance.core.registry.process.subscriber;

import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.governance.core.registry.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class ProcessRegistrySubscriberTest {

    @Mock
    private RegistryCenterRepository repository;

    @InjectMocks
    private ProcessRegistrySubscriber processRegistrySubscriber;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoadShowProcessListData() {
        ShowProcessListRequestEvent showProcessListRequestEvent = mock(ShowProcessListRequestEvent.class);
        Mockito.when(repository.getChildrenKeys(any())).thenReturn(Collections.singletonList("abc"));
        Mockito.when(repository.get(any())).thenReturn("abc");

        processRegistrySubscriber.loadShowProcessListData(showProcessListRequestEvent);
        Mockito.verify(repository, times(1)).get(any());

    }

    @Test
    public void testReportExecuteProcessSummary() {
        ExecuteProcessContext executeProcessContext = mock(ExecuteProcessContext.class);
        ExecuteProcessSummaryReportEvent event = mock(ExecuteProcessSummaryReportEvent.class);
        Mockito.when(event.getExecuteProcessContext()).thenReturn(executeProcessContext);
        Mockito.when(executeProcessContext.getExecutionID()).thenReturn("id");

        processRegistrySubscriber.reportExecuteProcessSummary(event);
        Mockito.verify(event, times(1)).getExecuteProcessContext();

    }

    @Test(expected = NullPointerException.class)
    public void testReportExecuteProcessSummaryThrowsException() {
        ExecuteProcessSummaryReportEvent event = mock(ExecuteProcessSummaryReportEvent.class);
        Mockito.when(event.getExecuteProcessContext()).thenThrow(NullPointerException.class);
        processRegistrySubscriber.reportExecuteProcessSummary(event);
        Mockito.verify(event, times(0)).getExecuteProcessContext();

    }

    @Test
    public void testReportExecuteProcessUnit() {

        ExecuteProcessUnitReportEvent event = mock(ExecuteProcessUnitReportEvent.class);
        Mockito.when(event.getExecutionID()).thenReturn("id");

        Mockito.when(repository.get(anyString())).thenReturn(mockYamlExecuteProcessContext());

        Mockito.when(event.getExecuteProcessUnit()).thenReturn(mockExecuteProcessUnit());
        processRegistrySubscriber.reportExecuteProcessUnit(event);

        Mockito.verify(repository, times(1)).persist(any(), any());
    }

    @Test(expected = NullPointerException.class)
    public void testReportExecuteProcessUnitException() {

        ExecuteProcessUnitReportEvent event = mock(ExecuteProcessUnitReportEvent.class);
        Mockito.when(event.getExecutionID()).thenReturn("id");
        Mockito.when(repository.get(anyString())).thenReturn(null);
        Mockito.when(event.getExecuteProcessUnit()).thenReturn(mockExecuteProcessUnit());

        processRegistrySubscriber.reportExecuteProcessUnit(event);
        Mockito.verify(repository, times(0)).persist(any(), any());
    }

    @Test
    public void testReportExecuteProcess() {
        ExecuteProcessReportEvent event = mock(ExecuteProcessReportEvent.class);
        Mockito.when(event.getExecutionID()).thenReturn("id");
        Mockito.when(repository.get(anyString())).thenReturn(mockYamlExecuteProcessContext());

        processRegistrySubscriber.reportExecuteProcess(event);
        Mockito.verify(repository, times(1)).delete(any());
    }

    @Test(expected = NullPointerException.class)
    public void testReportExecuteProcessException() {
        ExecuteProcessReportEvent event = mock(ExecuteProcessReportEvent.class);
        Mockito.when(event.getExecutionID()).thenReturn("id");
        Mockito.when(repository.get(anyString())).thenReturn(null);

        processRegistrySubscriber.reportExecuteProcess(event);
        Mockito.verify(repository, times(0)).delete(any());
    }

    private String mockYamlExecuteProcessContext() {
        YamlExecuteProcessUnit yamlExecuteProcessUnit = new YamlExecuteProcessUnit();
        yamlExecuteProcessUnit.setUnitID("159917166");
        yamlExecuteProcessUnit.setStatus(ExecuteProcessConstants.EXECUTE_STATUS_DONE);
        Collection<YamlExecuteProcessUnit> unitStatuses = Collections.singleton(yamlExecuteProcessUnit);
        YamlExecuteProcessContext yamlExecuteProcessContext = new YamlExecuteProcessContext();
        yamlExecuteProcessContext.setUnitStatuses(unitStatuses);
        return YamlEngine.marshal(yamlExecuteProcessContext);
    }

    private ExecuteProcessUnit mockExecuteProcessUnit() {
        ExecutionUnit executionUnit = mock(ExecutionUnit.class);
        return new ExecuteProcessUnit(executionUnit, ExecuteProcessConstants.EXECUTE_STATUS_DONE);
    }

}
