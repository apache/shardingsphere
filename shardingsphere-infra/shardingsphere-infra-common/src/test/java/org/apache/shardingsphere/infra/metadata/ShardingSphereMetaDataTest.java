package org.apache.shardingsphere.infra.metadata;

import org.junit.Test;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ShardingSphereMetaDataTest {

    @Test
    public void assertDropDatabase() {
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        shardingSphereMetaData.dropDatabase("foo_db");
        verify(shardingSphereMetaData.getDatabases().remove("foo_db"));
    }
}
