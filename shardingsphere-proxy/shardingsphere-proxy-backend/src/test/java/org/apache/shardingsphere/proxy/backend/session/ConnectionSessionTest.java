package org.apache.shardingsphere.proxy.backend.session;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConnectionSessionTest {
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    private ConnectionSession connectionSession;
    
    @Before
    public void setup() {
        connectionSession = new ConnectionSession(TransactionType.LOCAL, null);
        connectionSession.setBackendConnection(backendConnection);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
    }
    
    @Test
    public void assertSetCurrentSchema() {
        connectionSession.setCurrentSchema("currentSchema");
        assertThat(connectionSession.getSchemaName(), is("currentSchema"));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchTransactionTypeWhileBegin() throws SQLException {
        connectionSession.setCurrentSchema("schema");
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.begin();
        connectionSession.getTransactionStatus().setTransactionType(TransactionType.XA);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchSchemaWhileBegin() throws SQLException {
        connectionSession.setCurrentSchema("schema");
        BackendTransactionManager transactionManager = new BackendTransactionManager(backendConnection);
        transactionManager.begin();
        connectionSession.setCurrentSchema("newSchema");
    }
    
    @Test
    public void assertDefaultAutocommit() {
        assertTrue(connectionSession.isAutoCommit());
    }
    
    @Test
    public void assertSetAutocommit() {
        connectionSession.setAutoCommit(false);
        assertFalse(connectionSession.isAutoCommit());
    }
}
