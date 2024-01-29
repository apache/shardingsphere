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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.CRC32MatchTableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.DataMatchTableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyCheckerFactory;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TableDataConsistencyCheckerFactoryTest {
    
    @Test
    void assertNewInstanceTypeMatched() {
        assertInstanceOf(DataMatchTableDataConsistencyChecker.class, TableDataConsistencyCheckerFactory.newInstance(null, new Properties()));
        assertInstanceOf(DataMatchTableDataConsistencyChecker.class, TableDataConsistencyCheckerFactory.newInstance("DATA_MATCH", new Properties()));
        assertInstanceOf(CRC32MatchTableDataConsistencyChecker.class, TableDataConsistencyCheckerFactory.newInstance("CRC32_MATCH", new Properties()));
    }
    
    @Test
    void assertNewInstancesDifferent() {
        TableDataConsistencyChecker actual1 = TableDataConsistencyCheckerFactory.newInstance("DATA_MATCH", new Properties());
        TableDataConsistencyChecker actual2 = TableDataConsistencyCheckerFactory.newInstance("DATA_MATCH", new Properties());
        assertThat(actual1, not(actual2));
    }
}
