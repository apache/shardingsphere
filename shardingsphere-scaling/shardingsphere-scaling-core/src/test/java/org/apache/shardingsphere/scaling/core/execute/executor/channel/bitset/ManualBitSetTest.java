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

package org.apache.shardingsphere.scaling.core.execute.executor.channel.bitset;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.junit.Test;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ManualBitSetTest {
    
    @Test
    public void assertGet() {
        ManualBitSet bitSet = new ManualBitSet();
        IntStream.range(0, 10).forEach(bitSet::set);
        assertFalse(bitSet.get(0, 9).get(9));
        assertTrue(bitSet.get(0, 10).get(9));
        assertFalse(bitSet.get(0, 10).get(10));
        assertFalse(bitSet.get(0, 11).get(10));
    }
    
    @Test
    public void assertGetSetEndIndex() {
        ManualBitSet bitSet = new ManualBitSet();
        IntStream.range(0, 10).filter(each -> each % 2 == 1).forEach(bitSet::set);
        assertThat(bitSet.getEndIndex(0L, 5), is(10L));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertClear() {
        ManualBitSet bitSet = new ManualBitSet();
        IntStream.range(0, 100).forEach(bitSet::set);
        List<BitSet> bitSets = ReflectionUtil.getFieldValue(bitSet, "bitSets", List.class);
        assertNotNull(bitSets);
        assertThat(bitSets.size(), is(1));
        bitSet.clear(1025);
        assertThat(bitSets.size(), is(0));
        bitSet.clear(2048);
        assertThat(bitSets.size(), is(0));
    }
}
