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

package org.apache.shardingsphere.data.pipeline.core.config.process;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineProcessConfigurationUtilTest {
    
    @Test
    public void assertVerifyConfPathSuccess() {
        for (String each : Arrays.asList("/", "/READ", "/READ/RATE_LIMITER")) {
            PipelineProcessConfigurationUtil.verifyConfPath(each);
        }
    }
    
    @Test
    public void assertVerifyConfPathFailed() {
        Collection<String> confPaths = Arrays.asList("", "//", "READ", "/READ/");
        int failCount = 0;
        for (String each : confPaths) {
            try {
                PipelineProcessConfigurationUtil.verifyConfPath(each);
            } catch (final IllegalArgumentException ex) {
                ++failCount;
            }
        }
        assertThat(failCount, is(confPaths.size()));
    }
}
