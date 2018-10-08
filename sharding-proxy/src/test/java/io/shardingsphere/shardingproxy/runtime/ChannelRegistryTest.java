/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.runtime;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ChannelRegistryTest {
    
    @Test
    public void assertGetConnectionIdIfPresent() {
        ChannelRegistry.getInstance().putConnectionId("0x0a", 1000);
        assertThat(ChannelRegistry.getInstance().getConnectionId("0x0a"), is(1000));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetConnectionIdIfAbsent() {
        ChannelRegistry.getInstance().getConnectionId("0x0b");
    }
}
