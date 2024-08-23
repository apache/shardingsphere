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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineStringUtilsTest {
    
    @Test
    void assertEqualsIgnoreCase() {
        assertTrue(PipelineStringUtils.equalsIgnoreCase(null, null));
        assertFalse(PipelineStringUtils.equalsIgnoreCase(null, Collections.emptyList()));
        assertFalse(PipelineStringUtils.equalsIgnoreCase(Collections.emptyList(), null));
        assertFalse(PipelineStringUtils.equalsIgnoreCase(Collections.singletonList("test"), Collections.emptyList()));
        assertFalse(PipelineStringUtils.equalsIgnoreCase(Collections.emptyList(), Collections.singletonList("test")));
        assertTrue(PipelineStringUtils.equalsIgnoreCase(Collections.singletonList("test"), Collections.singletonList("test")));
        assertTrue(PipelineStringUtils.equalsIgnoreCase(Collections.singletonList("TEST"), Collections.singletonList("test")));
        assertTrue(PipelineStringUtils.equalsIgnoreCase(Collections.singletonList("test"), Collections.singletonList("TEST")));
    }
}
