package org.apache.shardingsphere.core.util;

import org.junit.Test;

public class ShardingVersionTest {

    @Test
    public void assertCheckDuplicate() {
        ShardingVersion.checkDuplicateClass(ShardingVersion.class);
    }
}
