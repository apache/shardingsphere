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
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Use primary key as position.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class PrimaryKeyPosition implements InventoryPosition {
    
    private static final Gson GSON = new Gson();
    
    private final boolean finished = false;
    
    private long beginValue;
    
    private long endValue;
    
    @Override
    public int compareTo(final Position position) {
        if (null == position) {
            return 1;
        }
        return Long.compare(beginValue, ((PrimaryKeyPosition) position).beginValue);
    }
    
    @Override
    public JsonElement toJson() {
        return GSON.toJsonTree(new long[]{beginValue, endValue});
    }
}
