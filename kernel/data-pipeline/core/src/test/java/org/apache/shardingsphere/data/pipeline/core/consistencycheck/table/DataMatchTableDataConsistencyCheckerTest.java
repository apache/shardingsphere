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

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataMatchTableDataConsistencyCheckerTest {
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    void assertChunkSizeInitSuccess() {
        for (String each : Arrays.asList("1", "1000")) {
            DataMatchTableDataConsistencyChecker checker = new DataMatchTableDataConsistencyChecker();
            checker.init(buildChunkSizeProperties(each));
            String actual = Plugins.getMemberAccessor().get(DataMatchTableDataConsistencyChecker.class.getDeclaredField("chunkSize"), checker).toString();
            assertThat(actual, is(each));
        }
    }
    
    @Test
    void assertChunkSizeInitFailure() {
        assertThrows(PipelineInvalidParameterException.class, () -> new DataMatchTableDataConsistencyChecker().init(buildChunkSizeProperties("xyz")));
        for (String each : Arrays.asList("0", "-1")) {
            assertThrows(PipelineInvalidParameterException.class, () -> new DataMatchTableDataConsistencyChecker().init(buildChunkSizeProperties(each)));
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    void assertStreamingRangeTypeInitSuccess() {
        for (String each : Arrays.asList("small", "large", "SMALL", "LARGE")) {
            DataMatchTableDataConsistencyChecker checker = new DataMatchTableDataConsistencyChecker();
            checker.init(buildStreamingRangeTypeProperties(each));
            String actual = Plugins.getMemberAccessor().get(DataMatchTableDataConsistencyChecker.class.getDeclaredField("streamingRangeType"), checker).toString();
            assertThat(actual, is(each.toUpperCase()));
        }
    }
    
    @Test
    void assertStreamingRangeTypeInitFailure() {
        assertThrows(PipelineInvalidParameterException.class, () -> new DataMatchTableDataConsistencyChecker().init(buildStreamingRangeTypeProperties("xyz")));
    }
    
    private Properties buildChunkSizeProperties(final String chunkSize) {
        Properties result = new Properties();
        result.put("chunk-size", chunkSize);
        return result;
    }
    
    private Properties buildStreamingRangeTypeProperties(final String streamingRangeType) {
        Properties result = new Properties();
        result.put("streaming-range-type", streamingRangeType);
        return result;
    }
}
