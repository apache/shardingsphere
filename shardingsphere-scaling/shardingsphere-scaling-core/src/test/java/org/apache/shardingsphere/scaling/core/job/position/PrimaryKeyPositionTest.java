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

package org.apache.shardingsphere.scaling.core.job.position;

import com.google.gson.Gson;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PrimaryKeyPositionTest {
    
    public static final Gson GSON = new Gson();
    
    @Test
    public void assertCompareTo() {
        PrimaryKeyPosition position1 = new PrimaryKeyPosition(1, 100);
        PrimaryKeyPosition position2 = new PrimaryKeyPosition(101, 200);
        assertThat(position1.compareTo(null), is(1));
        assertTrue(position1.compareTo(position2) < 0);
    }
    
    @Test
    public void assertFormJson() {
        PrimaryKeyPosition position = GSON.fromJson("[1,100]", PrimaryKeyPosition.class);
        assertThat(position.getBeginValue(), is(1L));
        assertThat(position.getEndValue(), is(100L));
    }
    
    @Test
    public void assertToJson() {
        PrimaryKeyPosition position = new PrimaryKeyPosition(1, 100);
        assertThat(new Gson().toJson(position), is("[1,100]"));
    }
}
