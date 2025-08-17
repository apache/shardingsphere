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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class TableDataConsistencyCheckerFactoryTest {
    
    @Test
    void assertNewInstanceTypeMatched() {
        assertThat(TableDataConsistencyCheckerFactory.newInstance(null, new Properties()), instanceOf(DataMatchTableDataConsistencyChecker.class));
        assertThat(TableDataConsistencyCheckerFactory.newInstance("DATA_MATCH", new Properties()), instanceOf(DataMatchTableDataConsistencyChecker.class));
        assertThat(TableDataConsistencyCheckerFactory.newInstance("CRC32_MATCH", new Properties()), instanceOf(CRC32MatchTableDataConsistencyChecker.class));
    }
    
    @Test
    void assertNewInstancesDifferent() {
        TableDataConsistencyChecker actual1 = TableDataConsistencyCheckerFactory.newInstance("DATA_MATCH", new Properties());
        TableDataConsistencyChecker actual2 = TableDataConsistencyCheckerFactory.newInstance("DATA_MATCH", new Properties());
        assertThat(actual1, not(actual2));
    }
}
