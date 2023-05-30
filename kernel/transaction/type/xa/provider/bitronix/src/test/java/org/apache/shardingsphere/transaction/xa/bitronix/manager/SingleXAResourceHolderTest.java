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

package org.apache.shardingsphere.transaction.xa.bitronix.manager;

import bitronix.tm.resource.common.ResourceBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.transaction.xa.XAResource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SingleXAResourceHolderTest {
    
    @Mock
    private XAResource xaResource;
    
    @Mock
    private ResourceBean resourceBean;
    
    private SingleXAResourceHolder singleXAResourceHolder;
    
    @BeforeEach
    void setUp() {
        singleXAResourceHolder = new SingleXAResourceHolder(xaResource, resourceBean);
    }
    
    @Test
    void assertGetXAResource() {
        assertThat(singleXAResourceHolder.getXAResource(), is(xaResource));
    }
    
    @Test
    void assertGetResourceBean() {
        assertThat(singleXAResourceHolder.getResourceBean(), is(resourceBean));
    }
    
    @Test
    void assertGetXAResourceHolders() {
        assertTrue(singleXAResourceHolder.getXAResourceHolders().isEmpty());
    }
    
    @Test
    void assertGetConnectionHandle() {
        assertNull(singleXAResourceHolder.getConnectionHandle());
    }
    
    @Test
    void assertGetLastReleaseDate() {
        assertNull(singleXAResourceHolder.getLastReleaseDate());
    }
    
    @Test
    void assertClose() {
        assertDoesNotThrow(() -> singleXAResourceHolder.close());
    }
}
