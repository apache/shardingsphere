package org.apache.shardingsphere.driver.governance.internal.state;

import org.apache.shardingsphere.driver.governance.internal.circuit.connection.CircuitBreakerConnection;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;

public class DriverStateContextTest {
    
    @Test
    public void assertGetConnection() {
        Connection actual1 = DriverStateContext.getConnection(
                Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)), mock(MetaDataContexts.class), mock(TransactionContexts.class, RETURNS_DEEP_STUBS), TransactionType.LOCAL);
        assertThat(actual1, instanceOf(ShardingSphereConnection.class));
        StateContext.switchState(new StateEvent(StateType.CIRCUIT_BREAK, true));
        Connection actual2 = DriverStateContext.getConnection(
                Collections.emptyMap(), mock(MetaDataContexts.class), mock(TransactionContexts.class, RETURNS_DEEP_STUBS), TransactionType.LOCAL);
        assertThat(actual2, instanceOf(CircuitBreakerConnection.class));
        StateContext.switchState(new StateEvent(StateType.CIRCUIT_BREAK, false));
        Connection actual3 = DriverStateContext.getConnection(
                Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)), mock(MetaDataContexts.class), mock(TransactionContexts.class, RETURNS_DEEP_STUBS), TransactionType.LOCAL);
        assertThat(actual3, instanceOf(ShardingSphereConnection.class));
    }
}
