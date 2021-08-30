package org.apache.shardingsphere.mode.manager;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.fixture.DefaultFixtureContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.fixture.FixtureContextManagerBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContextManagerBuilderFactoryTest {

    @Test
    public void assertNewInstanceWithConfiguration() {
        ModeConfiguration config = mock(ModeConfiguration.class);
        when(config.getType()).thenReturn("fixture");
        ContextManagerBuilder contextManagerBuilder = ContextManagerBuilderFactory.newInstance(config);
        assertThat(contextManagerBuilder, instanceOf(FixtureContextManagerBuilder.class));
    }

    @Test
    public void assertNewInstanceWithoutConfiguration() {
        ContextManagerBuilder contextManagerBuilder = ContextManagerBuilderFactory.newInstance(null);
        assertThat(contextManagerBuilder, instanceOf(DefaultFixtureContextManagerBuilder.class));
    }
}
