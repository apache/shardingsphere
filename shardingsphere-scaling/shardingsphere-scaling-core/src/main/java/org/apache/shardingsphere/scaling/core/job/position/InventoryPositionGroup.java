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
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Inventory position group.
 */
@Getter
@Setter
public final class InventoryPositionGroup {
    
    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Position.class, new PositionTypeAdapter()).create();
    
    private Map<String, Position<?>> unfinished;
    
    private Set<String> finished;
    
    /**
     * init {@code InventoryPositionGroup} from json.
     *
     * @param json data
     * @return Inventory position group
     */
    public static InventoryPositionGroup fromJson(final String json) {
        return GSON.fromJson(json, InventoryPositionGroup.class);
    }
    
    /**
     * To json.
     *
     * @return json string
     */
    public String toJson() {
        return GSON.toJson(this);
    }
    
    private static class PositionTypeAdapter extends TypeAdapter<Position<?>> {
        
        @Override
        public void write(final JsonWriter out, final Position<?> value) throws IOException {
            if (value instanceof PrimaryKeyPosition) {
                new PrimaryKeyPosition.PositionTypeAdapter().write(out, (PrimaryKeyPosition) value);
            } else if (value instanceof PlaceholderPosition) {
                new PlaceholderPosition.PositionTypeAdapter().write(out, (PlaceholderPosition) value);
            }
        }
        
        @Override
        public Position<?> read(final JsonReader in) throws IOException {
            in.beginArray();
            Position<?> result = in.hasNext() ? new PrimaryKeyPosition(in.nextLong(), in.nextLong()) : new PlaceholderPosition();
            in.endArray();
            return result;
        }
    }
}
