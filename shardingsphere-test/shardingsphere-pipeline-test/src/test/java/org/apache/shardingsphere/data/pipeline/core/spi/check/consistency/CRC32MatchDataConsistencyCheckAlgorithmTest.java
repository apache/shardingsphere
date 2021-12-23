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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class CRC32MatchDataConsistencyCheckAlgorithmTest {
    
    @Test
    public void assertNewInstance() {
        CRC32MatchDataConsistencyCheckAlgorithm checkAlgorithm = new CRC32MatchDataConsistencyCheckAlgorithm();
        checkAlgorithm.init();
        assertThat(checkAlgorithm.getType(), is(CRC32MatchDataConsistencyCheckAlgorithm.TYPE));
        assertNotNull(checkAlgorithm.getDescription());
        assertThat(checkAlgorithm.getProvider(), is("ShardingSphere"));
        assertThat(checkAlgorithm.getSupportedDatabaseTypes(), is(Collections.singletonList("MySQL")));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetSingleTableDataCalculator() {
        CRC32MatchDataConsistencyCheckAlgorithm checkAlgorithm = new CRC32MatchDataConsistencyCheckAlgorithm();
        checkAlgorithm.getSupportedDatabaseTypes().forEach(checkAlgorithm::getSingleTableDataCalculator);
    }
}
