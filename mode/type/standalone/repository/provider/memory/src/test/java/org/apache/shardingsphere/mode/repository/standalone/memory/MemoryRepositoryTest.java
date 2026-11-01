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

package org.apache.shardingsphere.mode.repository.standalone.memory;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryRepositoryTest {
    
    private final StandalonePersistRepository memoryRepository = TypedSPILoader.getService(StandalonePersistRepository.class, "Memory");
    
    @BeforeEach
    void setUp() {
        memoryRepository.persist("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0", "t_order");
        memoryRepository.persist("/metadata/sharding_db/schemas/sharding_db/tables/t_user/versions/0", "t_user");
        memoryRepository.persist("/metadata/encrypt_db/schemas/encrypt_db/tables/t_encrypt_01/versions/0", "t_encrypt_01");
        memoryRepository.persist("/metadata/encrypt_db/schemas/encrypt_db/tables/t_encrypt_02/versions/0", "t_encrypt_02");
    }
    
    @Test
    void assertPersist() {
        memoryRepository.persist("testKey", "testValue");
        assertThat(memoryRepository.query("testKey"), is("testValue"));
    }
    
    @Test
    void assertUpdate() {
        memoryRepository.update("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0", "t_order_updated");
        assertThat(memoryRepository.query("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0"), is("t_order_updated"));
    }
    
    @Test
    void assertDelete() {
        memoryRepository.delete("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0");
        assertFalse(memoryRepository.isExisted("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0"));
        assertThat(memoryRepository.query("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0"), is((String) null));
    }
    
    @ParameterizedTest(name = "{0}, parentExists={1}")
    @CsvSource({"/metadata/foo_db/schemas/foo_db/tables/foo_tbl,true", "/metadata/foo_db/schemas/foo_db/tables/foo_tbl,false", "/metadata/foo_db/schemas/foo_db/tables/foo_tbl/,true"})
    void assertDeleteSubtree(final String key, final boolean parentExists) {
        if (parentExists) {
            memoryRepository.persist(key, "foo_tbl");
        }
        String tableKey = "/metadata/foo_db/schemas/foo_db/tables/foo_tbl";
        memoryRepository.persist(tableKey + "/active_version", "1");
        memoryRepository.persist(tableKey + "/versions/0", "foo_version_0");
        memoryRepository.persist(tableKey + "/versions/1", "foo_version_1");
        memoryRepository.persist(tableKey + "_extra/versions/0", "foo_sibling");
        memoryRepository.delete(key);
        assertNull(memoryRepository.query(key));
        assertNull(memoryRepository.query(tableKey + "/active_version"));
        assertNull(memoryRepository.query(tableKey + "/versions/0"));
        assertNull(memoryRepository.query(tableKey + "/versions/1"));
        assertTrue(memoryRepository.getChildrenKeys(tableKey + "/").isEmpty());
        assertThat(memoryRepository.getChildrenKeys("/metadata/foo_db/schemas/foo_db/tables"), is(Collections.singletonList("foo_tbl_extra")));
        assertThat(memoryRepository.query(tableKey + "_extra/versions/0"), is("foo_sibling"));
    }
    
    @Test
    void assertDeleteMissingNode() {
        memoryRepository.delete("/metadata/zzzz_missing");
        assertFalse(memoryRepository.isExisted("/metadata/zzzz_missing"));
        assertThat(memoryRepository.getChildrenKeys("/metadata"), is(Arrays.asList("encrypt_db", "sharding_db")));
        assertThat(memoryRepository.query("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0"), is("t_order"));
    }
    
    @Test
    void assertGetChildrenKeys() {
        assertThat(memoryRepository.getChildrenKeys("/metadata").size(), is(2));
        assertTrue(memoryRepository.getChildrenKeys("/metadata").containsAll(Arrays.asList("encrypt_db", "sharding_db")));
        assertThat(memoryRepository.getChildrenKeys("/metadata/sharding_db/schemas/sharding_db/tables/").size(), is(2));
        assertThat(memoryRepository.getChildrenKeys("/metadata/encrypt_db/schemas/encrypt_db/tables/").size(), is(2));
    }
}
