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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

import bitronix.tm.resource.common.ResourceBean;
import javax.transaction.xa.XAResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class SingleXAResourceHolderTest {
    
    @Mock
    private XAResource xaResource;
    
    @Mock
    private ResourceBean resourceBean;
    
    private SingleXAResourceHolder singleXAResourceHolder;
    
    @Before
    public void setUp() {
        singleXAResourceHolder = new SingleXAResourceHolder(xaResource, resourceBean);
    }
    
    @Test
    public void assertGetXAResource() {
        assertThat(singleXAResourceHolder.getXAResource(), is(xaResource));
    }
    
    @Test
    public void assertGetResourceBean() {
        assertThat(singleXAResourceHolder.getResourceBean(), is(resourceBean));
    }
    
    @Test
    public void assertGetXAResourceHolders() {
        assertNull(singleXAResourceHolder.getXAResourceHolders());
    }
    
    @Test
    public void assertGetConnectionHandle() {
        assertNull(singleXAResourceHolder.getConnectionHandle());
    }
    
    @Test
    public void assertGetLastReleaseDate() {
        assertNull(singleXAResourceHolder.getLastReleaseDate());
    }
    
    @Test
    public void assertClose() {
        singleXAResourceHolder.close();
    }
}
