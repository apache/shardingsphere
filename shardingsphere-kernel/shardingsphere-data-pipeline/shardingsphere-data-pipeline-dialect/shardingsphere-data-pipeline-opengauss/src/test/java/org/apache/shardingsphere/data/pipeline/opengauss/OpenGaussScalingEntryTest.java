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

package org.apache.shardingsphere.data.pipeline.opengauss;

import org.apache.shardingsphere.data.pipeline.opengauss.ingest.OpenGaussWalDumper;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class OpenGaussScalingEntryTest {
    
    @Test
    public void assertGetScalingEntryByDatabaseType() {
        ScalingEntry actual = ScalingEntryFactory.getInstance("openGauss");
        assertThat(actual, instanceOf(OpenGaussScalingEntry.class));
        assertThat(actual.getIncrementalDumperClass(), equalTo(OpenGaussWalDumper.class));
    }
}
