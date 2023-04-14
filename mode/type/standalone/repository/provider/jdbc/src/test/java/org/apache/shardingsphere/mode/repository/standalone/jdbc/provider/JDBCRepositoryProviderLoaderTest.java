package org.apache.shardingsphere.mode.repository.standalone.jdbc.provider;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JDBCRepositoryProviderLoaderTest {
    
    @Test
    void assertLoadFixtureProvider() {
        final JDBCRepositoryProvider fixture = JDBCRepositoryProviderLoader.load("FIXTURE");
        assertThat(fixture.getType(), is("FIXTURE"));
    }
    
    @Test
    void assertLoadDefaultProvider() {
        final JDBCRepositoryProvider fixture = JDBCRepositoryProviderLoader.load("nonexistent");
        assertThat(fixture.getType(), is("DEFAULT"));
    }
}
