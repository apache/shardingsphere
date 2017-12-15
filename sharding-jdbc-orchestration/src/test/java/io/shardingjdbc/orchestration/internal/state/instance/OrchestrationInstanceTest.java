package io.shardingjdbc.orchestration.internal.state.instance;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationInstanceTest {
    
    @Test
    public void assertGetInstanceId() {
        assertThat(new OrchestrationInstance("127.0.0.1@-@0").getInstanceId(), is("127.0.0.1@-@0"));
    }
}
