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


public final class PlaceholderPositionTest {
    
    public static final Gson GSON = new Gson();
    
    @Test
    public void assertCompareTo() {
        PlaceholderPosition position1 = new PlaceholderPosition();
        PlaceholderPosition position2 = new PlaceholderPosition();
        assertThat(position1.compareTo(position2), is(1));
    }
    
    @Test
    public void assertTypeAdapter() {
        PlaceholderPosition position = GSON.fromJson("[]", PlaceholderPosition.class);
        assertThat(new Gson().toJson(position), is("[]"));
    }
}
