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

package io.shardingsphere.transaction.xa.manager;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.manager.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class XATransactionManagerSPILoaderTest {
    
    private XATransactionManagerSPILoader spiLoader = XATransactionManagerSPILoader.getInstance();
    
    @Test
    public void assertGerInstanceWithSPI() {
        assertThat(spiLoader.getTransactionManager(), instanceOf(AtomikosTransactionManager.class));
    }
    
    @Test(expected = ShardingException.class)
    @SuppressWarnings("unchecked")
    public void assertServiceLoaderFailed() {
        Collection xaTransactionManagers = mock(Collection.class);
        doThrow(ShardingException.class).when(xaTransactionManagers).size();
        ReflectiveUtil.setProperty(spiLoader, "xaTransactionManagers", xaTransactionManagers);
        ReflectiveUtil.methodInvoke(spiLoader, "load");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertXaTransactionManagerGreaterThanOne() {
        Collection xaTransactionManagers = mock(Collection.class);
        when(xaTransactionManagers.size()).thenReturn(2);
        Iterator iterator = mock(Iterator.class);
        when(xaTransactionManagers.iterator()).thenReturn(iterator);
        XATransactionManager atomikosTransactionManager = new AtomikosTransactionManager();
        when(iterator.next()).thenReturn(atomikosTransactionManager);
        ReflectiveUtil.setProperty(spiLoader, "xaTransactionManagers", xaTransactionManagers);
        XATransactionManager actual = (XATransactionManager) ReflectiveUtil.methodInvoke(spiLoader, "load");
        assertThat(actual, is(atomikosTransactionManager));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertXaTransactionManagerIsEmpty() {
        Collection xaTransactionManagers = mock(Collection.class);
        when(xaTransactionManagers.isEmpty()).thenReturn(true);
        ReflectiveUtil.setProperty(spiLoader, "xaTransactionManagers", xaTransactionManagers);
        XATransactionManager actual = (XATransactionManager) ReflectiveUtil.methodInvoke(spiLoader, "load");
        assertThat(actual, instanceOf(AtomikosTransactionManager.class));
    }
}
