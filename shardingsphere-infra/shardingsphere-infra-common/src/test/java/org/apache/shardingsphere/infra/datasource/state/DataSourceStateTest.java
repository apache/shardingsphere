package org.apache.shardingsphere.infra.datasource.state;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DataSourceStateTest {

    @Test
    public void testDisabled() {
        assertEquals(DataSourceState.DISABLED, DataSourceState.getDataSourceState("disabled"));
    }

    @Test
    public void testEnabled() {
        assertEquals(DataSourceState.ENABLED, DataSourceState.getDataSourceState("enabled"));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStateIsEmpty() {
        assertThrows("Illegal data source state ``",IllegalArgumentException.class,()->{
           DataSourceState.getDataSourceState("");
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStateIsNull() {
        assertThrows("Illegal data source state ``",IllegalArgumentException.class,()->{
            DataSourceState.getDataSourceState(null);
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStateIsInvalid() {
        assertThrows("Illegal data source state `invalid`",IllegalArgumentException.class,()->{
            DataSourceState.getDataSourceState("invalid");
        });
    }
}