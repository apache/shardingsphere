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

package org.apache.shardingsphere.sharding.algorithm.sharding;

import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ShardingAutoTableAlgorithmUtilTest {
    
    private final Collection<String> availableTargetNames = new LinkedList<>();
    
    private final DataNodeInfo dataNodeInfo = new DataNodeInfo("t_order_", 10, '0');
    
    @Before
    public void setup() {
        availableTargetNames.add("t_order_00");
        availableTargetNames.add("t_order_01");
        availableTargetNames.add("t_order_02");
    }
    
    @Test
    public void assertFindMatchedTargetNameWhenTableExist() {
        Optional<String> actual = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(availableTargetNames, "2", dataNodeInfo);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("t_order_02"));
    }
    
    @Test
    public void assertFindMatchedTargetNameWhenTableNotExist() {
        Optional<String> output = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(availableTargetNames, "3", dataNodeInfo);
        assertFalse(output.isPresent());
    }
}
