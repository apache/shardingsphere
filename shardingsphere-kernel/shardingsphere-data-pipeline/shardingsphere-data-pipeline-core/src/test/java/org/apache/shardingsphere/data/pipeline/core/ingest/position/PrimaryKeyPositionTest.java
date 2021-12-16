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

import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PrimaryKeyPositionTest {
    
    @Test
    public void assertCompareTo() {
        PrimaryKeyPosition position1 = new PrimaryKeyPosition(1, 100);
        PrimaryKeyPosition position2 = new PrimaryKeyPosition(101, 200);
        assertThat(position1.compareTo(null), is(1));
        assertTrue(position1.compareTo(position2) < 0);
    }
    
    @Test
    public void assertInit() {
        PrimaryKeyPosition position = PrimaryKeyPosition.init("1,100");
        assertThat(position.getBeginValue(), is(1L));
        assertThat(position.getEndValue(), is(100L));
    }
    
    @Test
    public void assertToString() {
        assertThat(new PrimaryKeyPosition(1, 100).toString(), is("1,100"));
    }
}
