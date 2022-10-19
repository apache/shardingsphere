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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.junit.Before;
import org.junit.Test;

public final class ShardingAutoTableAlgorithmUtilTest {
    
    private Collection<String> collection;
    
    private DataNodeInfo dataNodeInfo;
    
    @Before
    public void setup() {
        collection = new ArrayList<>();
        collection.add("PREFIX----SUFFIX");
        collection.add("PREFIXSUFFIXSTRING");
        collection.add("PREFIX----------");
        String prefix = "PREFIX";
        int suffixMinLength = 10;
        char paddingChar = '-';
        dataNodeInfo = new DataNodeInfo(prefix, suffixMinLength, paddingChar);
    }
    
    @Test
    public void assertFindMatchedTargetNameForValidInputs() {
        Optional<String> output = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection, "SUFFIX", dataNodeInfo);
        assertTrue(output.isPresent());
        assertEquals("PREFIX----SUFFIX", output.get());
        Optional<String> output1 = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection, "SUFFIXSTRING", dataNodeInfo);
        assertTrue(output1.isPresent());
        assertEquals("PREFIXSUFFIXSTRING", output1.get());
        Optional<String> output2 = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection, "", dataNodeInfo);
        assertTrue(output2.isPresent());
        assertEquals("PREFIX----------", output2.get());
    }
    
    @Test
    public void assertFindMatchedTargetNameNonExistingInput() {
        Optional<String> output = ShardingAutoTableAlgorithmUtil.findMatchedTargetName(collection, "NONEXISTINGSUFFIX", dataNodeInfo);
        assertFalse(output.isPresent());
    }
}
