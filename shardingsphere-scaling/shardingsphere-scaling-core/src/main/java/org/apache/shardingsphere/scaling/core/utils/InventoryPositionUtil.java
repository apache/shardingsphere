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

package org.apache.shardingsphere.scaling.core.utils;

import com.google.gson.Gson;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderInventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;

import java.util.List;

/**
 * Inventory position util.
 */
public final class InventoryPositionUtil {
    
    private static final Gson GSON = new Gson();
    
    /**
     * Transform primary key position from json to object.
     *
     * @param json json data
     * @return primary key position
     */
    public static InventoryPosition fromJson(final String json) {
        List<Double> values = GSON.<List<Double>>fromJson(json, List.class);
        if (2 == values.size()) {
            return new PrimaryKeyPosition(values.get(0).longValue(), values.get(1).longValue());
        }
        return new PlaceholderInventoryPosition();
    }
}
