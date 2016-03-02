/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api;

import java.util.Iterator;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HintShardingValueManagerTest {
    
    @Before
    public void init() {
        HintShardingValueManager.init();
    }
    
    @After
    public void clear() {
        HintShardingValueManager.clear();
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void testInit() throws Exception {
        HintShardingValueManager.init();
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void testRegisterWithoutInit1() throws Exception {
        HintShardingValueManager.clear();
        HintShardingValueManager.registerShardingValueOfDatabase("test", "test", 1);
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void testRegisterWithoutInit2() throws Exception {
        HintShardingValueManager.clear();
        HintShardingValueManager.registerShardingValueOfTable("test", "test", 1);
    }
    
    @Test
    public void testRegisterShardingValueOfDatabase() throws Exception {
        HintShardingValueManager.registerShardingValueOfDatabase("test", "test", 1);
        assertThat(HintShardingValueManager.getShardingValueOfDatabase("test").get().iterator().next().getColumnName(), is("test"));
        assertThat((Integer) HintShardingValueManager.getShardingValueOfDatabase("test").get().iterator().next().getValue(), is(1));
        assertThat(HintShardingValueManager.getShardingValueOfDatabase("null").isPresent(), is(false));
    }
    
    @Test
    public void testGetShardingValueOfDatabase() throws Exception {
        HintShardingValueManager.clear();
        assertThat(HintShardingValueManager.getShardingValueOfDatabase("null").isPresent(), is(false));
    }
    
    @Test
    public void testRegisterShardingValueOfTable() throws Exception {
        HintShardingValueManager.registerShardingValueOfTable("test", "test", 1);
        assertThat(HintShardingValueManager.getShardingValueOfTable("test").get().iterator().next().getColumnName(), is("test"));
        assertThat((Integer) HintShardingValueManager.getShardingValueOfTable("test").get().iterator().next().getValue(), is(1));
        assertThat(HintShardingValueManager.getShardingValueOfTable("null").isPresent(), is(false));
    }
    
    @Test
    public void testGetShardingValueOfTable() throws Exception {
        HintShardingValueManager.clear();
        assertThat(HintShardingValueManager.getShardingValueOfTable("null").isPresent(), is(false));
    }
    
    @Test
    public void testMultiValues() {
        HintShardingValueManager.registerShardingValueOfTable("test", "test1", 1);
        HintShardingValueManager.registerShardingValueOfTable("test", "test2", 2);
        Iterator<ShardingValue<?>> iter = HintShardingValueManager.getShardingValueOfTable("test").get().iterator();
        ShardingValue<?> shardingValue = iter.next();
        assertThat(shardingValue.getColumnName(), is("test1"));
        assertThat((Integer) shardingValue.getValue(), is(1));
        shardingValue = iter.next();
        assertThat(shardingValue.getColumnName(), is("test2"));
        assertThat((Integer) shardingValue.getValue(), is(2));
    }
}