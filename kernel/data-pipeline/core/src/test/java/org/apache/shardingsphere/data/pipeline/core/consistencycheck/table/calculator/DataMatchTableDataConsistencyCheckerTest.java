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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.DataMatchTableDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DataMatchTableDataConsistencyCheckerTest {
    
    @Test
    void assertInitSuccess() {
        for (String each : Arrays.asList("1", "1000")) {
            new DataMatchTableDataConsistencyChecker().init(buildAlgorithmProperties(each));
        }
    }
    
    @Test
    void assertInitFailure() {
        assertThrows(PipelineInvalidParameterException.class, () -> new DataMatchTableDataConsistencyChecker().init(buildAlgorithmProperties("xyz")));
        for (String each : Arrays.asList("0", "-1")) {
            assertThrows(PipelineInvalidParameterException.class, () -> new DataMatchTableDataConsistencyChecker().init(buildAlgorithmProperties(each)));
        }
    }
    
    private Properties buildAlgorithmProperties(final String chunkSize) {
        Properties result = new Properties();
        result.put("chunk-size", chunkSize);
        return result;
    }
}
