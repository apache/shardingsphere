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

package org.apache.shardingsphere.data.pipeline.core.metadata.node;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PipelineMetaDataNodeTest {
    
    @Test
    public void assertGetJobConfigPath() {
        String actualOffsetPath = PipelineMetaDataNode.getScalingJobOffsetPath("0130317c30317c3054317c7368617264696e675f6462");
        assertThat(actualOffsetPath, is("/scaling/0130317c30317c3054317c7368617264696e675f6462/offset"));
        actualOffsetPath = PipelineMetaDataNode.getScalingJobOffsetPath("0130317c30317c3054317c7368617264696e675f6462", 1);
        assertThat(actualOffsetPath, is("/scaling/0130317c30317c3054317c7368617264696e675f6462/offset/1"));
    }
    
    @Test
    public void assertGetScalingJobConfigPath() {
        assertThat(PipelineMetaDataNode.getScalingJobConfigPath("0130317c30317c3054317c7368617264696e675f6462"), is("/scaling/0130317c30317c3054317c7368617264696e675f6462/config"));
    }
    
    @Test
    public void assertGetScalingCheckResultPath() {
        assertThat(PipelineMetaDataNode.getScalingCheckResultPath("0130317c30317c3054317c7368617264696e675f6462"), is("/scaling/0130317c30317c3054317c7368617264696e675f6462/check/result"));
    }
}
