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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MemoryRepositoryTest {
    
    private final MemoryRepository memoryRepository = new MemoryRepository();
    
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
        assertThat(memoryRepository.query("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0"),
                is("t_order_updated"));
    }
    
    @Test
    void assertDelete() {
        memoryRepository.delete("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0");
        assertThat(memoryRepository.isExisted("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0"), is(false));
        assertThat(memoryRepository.query("/metadata/sharding_db/schemas/sharding_db/tables/t_order/versions/0"), is((String) null));
    }
    
    @Test
    void assertGetChildrenKeys() {
        assertThat(memoryRepository.getChildrenKeys("/metadata").size(), is(2));
        assertThat(memoryRepository.getChildrenKeys("/metadata").containsAll(Arrays.asList("encrypt_db", "sharding_db")), is(true));
        assertThat(memoryRepository.getChildrenKeys("/metadata/sharding_db/schemas/sharding_db/tables/").size(), is(2));
        assertThat(memoryRepository.getChildrenKeys("/metadata/encrypt_db/schemas/encrypt_db/tables/").size(), is(2));
    }
}
