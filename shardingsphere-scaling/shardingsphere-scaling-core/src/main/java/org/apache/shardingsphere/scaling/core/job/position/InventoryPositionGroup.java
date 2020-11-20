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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public final class InventoryPositionGroup {
    
    private static final Gson GSON = new Gson();
    
    private static final String UNFINISHED = "unfinished";
    
    private static final String FINISHED = "finished";
    
    private Map<String, Position<?>> unfinished;
    
    private Set<String> finished;
    
    /**
     * Transform inventory position from json to object.
     *
     * @param data json data
     * @return inventory position
     */
    public static InventoryPositionGroup fromJson(final String data) {
        InventoryPositionGroup result = new InventoryPositionGroup();
        JsonObject json = JsonParser.parseString(data).getAsJsonObject();
        Map<String, Object> unfinished = GSON.<Map<String, Object>>fromJson(json.getAsJsonObject(UNFINISHED), Map.class);
        result.setUnfinished(unfinished.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> fromJson(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        result.setFinished(GSON.<Set<String>>fromJson(json.getAsJsonArray(FINISHED), Set.class));
        return result;
    }
    
    private static Position<?> fromJson(final Object json) {
        List<Double> values = GSON.<List<Double>>fromJson(json.toString(), List.class);
        if (2 == values.size()) {
            return new PrimaryKeyPosition(values.get(0).longValue(), values.get(1).longValue());
        }
        return new PlaceholderPosition();
    }
    
    /**
     * To json.
     *
     * @return json string
     */
    public String toJson() {
        JsonObject result = new JsonObject();
        result.add(UNFINISHED, GSON.toJsonTree(unfinished));
        result.add(FINISHED, GSON.toJsonTree(finished));
        return GSON.toJson(result);
    }
}
