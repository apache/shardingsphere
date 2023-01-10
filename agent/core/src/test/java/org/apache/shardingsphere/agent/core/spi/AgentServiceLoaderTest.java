package org.apache.shardingsphere.agent.core.spi;

import org.apache.shardingsphere.fixture.agent.AgentServiceEmptySPIFixture;
import org.apache.shardingsphere.fixture.agent.AgentServiceSPIFixture;
import org.apache.shardingsphere.fixture.agent.impl.AgentServiceSPIFixtureImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class AgentServiceLoaderTest {

    @Test(expected = NullPointerException.class)
    public void assertGetServiceLoaderWithNullValue() {
        AgentServiceLoader.getServiceLoader(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertGetServiceLoaderWithNoInterface() {
        AgentServiceLoader.getServiceLoader(Object.class);
    }

    @Test
    public void assertGetServiceLoaderWithEmptyInstances() {
        assertTrue(AgentServiceLoader.getServiceLoader(AgentServiceEmptySPIFixture.class).getServices().isEmpty());
    }

    @Test
    public void assertGetServiceLoaderWithImplementSPI() {
        AgentServiceLoader<AgentServiceSPIFixture> actual = AgentServiceLoader.getServiceLoader(AgentServiceSPIFixture.class);
        assertThat(actual.getServices().size(), is(1));
        AgentServiceSPIFixture actualInstance = actual.getServices().iterator().next();
        assertThat(actualInstance, instanceOf(AgentServiceSPIFixtureImpl.class));
        assertThat(actualInstance, is(AgentServiceLoader.getServiceLoader(AgentServiceSPIFixture.class).getServices().iterator().next()));
    }
}
