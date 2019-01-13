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

package io.shardingsphere.transaction.xa.manager.atomikos;

import io.shardingsphere.transaction.xa.jta.ShardingXAResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XADataSource;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AtomikosXARecoverableResourceTest {
    
    @Mock
    private ShardingXAResource shardingXAResource;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Before
    public void setUp() {
        when(shardingXAResource.getResourceName()).thenReturn("ds1");
    }
    
    @Test
    public void assertUseXAResource() {
        AtomikosXARecoverableResource atomikosXARecoverableResource = new AtomikosXARecoverableResource("ds1", xaDataSource);
        assertTrue(atomikosXARecoverableResource.usesXAResource(shardingXAResource));
    }
}
