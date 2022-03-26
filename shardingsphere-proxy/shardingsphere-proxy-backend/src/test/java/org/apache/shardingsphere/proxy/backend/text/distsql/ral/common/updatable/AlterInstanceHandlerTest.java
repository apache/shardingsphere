package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;


import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterInstanceStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.mockito.Mockito.*;

public class AlterInstanceHandlerTest {


    @Test(expected = UnsupportedOperationException.class)
    public void assertUpdateWithNotSupportedKey() throws SQLException {
        String instanceId = "instance_id";
        String key = "key_1";
        String value = "value_1";
        new AlterInstanceHandler().initStatement(getSQLStatement(instanceId , key , value)).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertCheckWithNoPersistenceConfigurationFound() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaDataPersistService()).thenReturn(Optional.empty());
        ProxyContext.getInstance().init(contextManager);
        String instanceId = "instance_id";
        String key = "xa_recovery_nodes";
        String value = "value_1";
        new AlterInstanceHandler().initStatement(getSQLStatement(instanceId , key , value)).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void assertCheckWithNotExistInstanceId() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(mock(PersistRepository.class));
        when(contextManager.getMetaDataContexts().getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));
        ProxyContext.getInstance().init(contextManager);
        String instanceId = "instance_id";
        String key = "xa_recovery_nodes";
        String value = "value_1";
        new AlterInstanceHandler().initStatement(getSQLStatement(instanceId , key , value)).execute();
    }


    private AlterInstanceStatement getSQLStatement(final String instanceId , final String key , final String value) {
        return new AlterInstanceStatement(instanceId , key , value);
    }
}
