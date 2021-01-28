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

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Position group.
 */
@Getter
@Setter
public final class JobPosition {
    
    private static final Gson GSON = new Gson();
    
    private static final Gson INVENTORY_POSITION_ADAPTED_GSON = new GsonBuilder().registerTypeHierarchyAdapter(Position.class, new InventoryPositionTypeAdapter()).create();
    
    private String status;
    
    private String databaseType;
    
    private Map<String, Position<?>> inventoryPositions;
    
    private Map<String, Position<?>> incrementalPositions;
    
    /**
     * Get incremental position.
     *
     * @param dataSourceName data source name
     * @return incremental position
     */
    public Position<?> getIncrementalPosition(final String dataSourceName) {
        return incrementalPositions.get(dataSourceName);
    }
    
    /**
     * Get inventory position.
     *
     * @param tableName table name
     * @return inventory position
     */
    public Map<String, Position<?>> getInventoryPosition(final String tableName) {
        Pattern pattern = Pattern.compile(String.format("%s(#\\d+)?", tableName));
        return inventoryPositions.entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * To json.
     *
     * @return json data
     */
    public String toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("status", status);
        result.addProperty("databaseType", databaseType);
        result.add("inventory", getInventoryJson());
        result.add("incremental", getIncrementalJson());
        return result.toString();
    }
    
    private JsonObject getInventoryJson() {
        JsonObject result = new JsonObject();
        JsonArray finished = new JsonArray();
        JsonObject unfinished = new JsonObject();
        for (Map.Entry<String, Position<?>> entry : inventoryPositions.entrySet()) {
            if (entry.getValue() instanceof FinishedPosition) {
                finished.add(entry.getKey());
                continue;
            }
            unfinished.add(entry.getKey(), GSON.toJsonTree(entry.getValue(), entry.getValue().getClass()));
        }
        result.add("finished", finished);
        result.add("unfinished", unfinished);
        return result;
    }
    
    private JsonObject getIncrementalJson() {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, Position<?>> entry : incrementalPositions.entrySet()) {
            result.add(entry.getKey(), GSON.toJsonTree(entry.getValue(), entry.getClass()));
        }
        return result;
    }
    
    /**
     * From json.
     *
     * @param data json data
     * @return job position
     */
    public static JobPosition fromJson(final String data) {
        JobPosition result = new JobPosition();
        JsonObject jsonObject = GSON.fromJson(data, JsonObject.class);
        result.setStatus(jsonObject.get("status").getAsString());
        result.setDatabaseType(jsonObject.get("databaseType").getAsString());
        result.setInventoryPositions(getInventoryPositions(jsonObject.get("inventory").getAsJsonObject()));
        result.setIncrementalPositions(getIncrementalPositions(jsonObject.get("incremental").getAsJsonObject(), jsonObject.get("databaseType").getAsString()));
        return result;
    }
    
    private static Map<String, Position<?>> getInventoryPositions(final JsonObject inventory) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("inventory", inventory);
        return INVENTORY_POSITION_ADAPTED_GSON.fromJson(jsonObject, JobPosition.class).getInventoryPositions();
    }
    
    private static Map<String, Position<?>> getIncrementalPositions(final JsonObject incremental, final String databaseType) {
        Class<?> incrementalPositionClass = PositionInitializerFactory.getPositionClass(databaseType);
        Map<String, Position<?>> result = Maps.newHashMap();
        for (String each : incremental.keySet()) {
            result.put(each, (Position<?>) GSON.fromJson(incremental.get(each), incrementalPositionClass));
        }
        return result;
    }
    
    private static class InventoryPositionTypeAdapter extends TypeAdapter<Position<?>> {
        
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
