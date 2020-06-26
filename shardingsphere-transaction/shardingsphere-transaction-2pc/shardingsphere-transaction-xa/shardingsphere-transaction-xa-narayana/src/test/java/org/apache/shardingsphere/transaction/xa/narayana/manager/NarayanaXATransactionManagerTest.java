package org.apache.shardingsphere.transaction.xa.narayana.manager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import javax.sql.XADataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.narayana.manager.fixture.ReflectiveUtil;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NarayanaXATransactionManagerTest {
    
    private final NarayanaXATransactionManager narayanaXATransactionManager = new NarayanaXATransactionManager();
    
    @Mock
    private TransactionManager transactionManager;
    
    @Mock
    private XARecoveryModule xaRecoveryModule;
    
    @Mock
    private RecoveryManagerService recoveryManagerService;
    
    @Mock
    private XADataSource xaDataSource;
    
    @SneakyThrows
    @Before
    public void setUp() {
        ReflectiveUtil.setProperty(narayanaXATransactionManager, "xaRecoveryModule", xaRecoveryModule);
        ReflectiveUtil.setProperty(narayanaXATransactionManager, "transactionManager", transactionManager);
        ReflectiveUtil.setProperty(narayanaXATransactionManager, "recoveryManagerService", recoveryManagerService);
    }
    
    @SneakyThrows
    @Test
    public void assertInit() {
        narayanaXATransactionManager.init();
        verify(recoveryManagerService).create();
        verify(recoveryManagerService).start();
    }
    
    @Test
    public void assertRegisterRecoveryResource() {
        narayanaXATransactionManager.registerRecoveryResource("ds1", xaDataSource);
        verify(xaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
    }
    
    @Test
    public void assertRemoveRecoveryResource() {
        narayanaXATransactionManager.removeRecoveryResource("ds1", xaDataSource);
        verify(xaRecoveryModule).removeXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
    }
    
    @SneakyThrows
    @Test
    public void assertEnlistResource() {
        SingleXAResource singleXAResource = mock(SingleXAResource.class);
        Transaction transaction = mock(Transaction.class);
        when(transactionManager.getTransaction()).thenReturn(transaction);
        narayanaXATransactionManager.enlistResource(singleXAResource);
        verify(transaction).enlistResource(singleXAResource.getDelegate());
    }
    
    @Test
    public void assertGetTransactionManager() {
        assertThat(narayanaXATransactionManager.getTransactionManager(), is(transactionManager));
    }
    
    @SneakyThrows
    @Test
    public void assertClose() {
        narayanaXATransactionManager.close();
        verify(recoveryManagerService).stop();
        verify(recoveryManagerService).destroy();
    }
}
