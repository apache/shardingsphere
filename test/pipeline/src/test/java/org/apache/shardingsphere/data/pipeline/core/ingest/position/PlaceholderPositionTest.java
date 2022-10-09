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

package org.apache.shardingsphere.data.pipeline.core.ingest.position;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PlaceholderPositionTest {
    
    @Test
    public void assertCompareTo() {
        PlaceholderPosition position1 = new PlaceholderPosition();
        PlaceholderPosition position2 = new PlaceholderPosition();
        assertThat(position1.compareTo(position2), is(1));
    }
    
    @Test
    public void assertToString() {
        assertThat(new PlaceholderPosition().toString(), is(""));
    }
}
