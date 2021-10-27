/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mode.metadata.persist.node;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CacheNodeTest {
    
    @Test
    public void assertGetCacheId() {
        Optional<String> cacheId = CacheNode.getCacheId("/metadata/sharding_db/rules",
                "/metadata/sharding_db/rules/cache/testCacheId");
        assertTrue(cacheId.isPresent());
        assertThat(cacheId.get(), is("testCacheId"));
    }
    
    @Test
    public void assertGetCachePathWithPathAndCacheId() {
        assertThat(CacheNode.getCachePath("/metadata/sharding_db/rules", "1"),
                is("/metadata/sharding_db/rules/cache/1"));
    }
    
    @Test
    public void assertGetCachePathWithPath() {
        assertThat(CacheNode.getCachePath("/metadata/sharding_db/rules"),
                startsWith("/metadata/sharding_db/rules/"));
    }
}
