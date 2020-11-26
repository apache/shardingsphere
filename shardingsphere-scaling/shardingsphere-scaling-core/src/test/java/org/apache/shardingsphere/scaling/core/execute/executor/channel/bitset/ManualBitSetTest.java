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

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ManualBitSetTest {
    
    @Test
    public void assertGet() {
        ManualBitSet manualBitSet = new ManualBitSet();
        Lists.newArrayList(0, 1).forEach(each -> manualBitSet.set(each));
        BitSet actual = manualBitSet.get(0, 1);
        assertThat(actual.get(0), is(true));
        assertThat(actual.get(1), is(true));
    }
}
