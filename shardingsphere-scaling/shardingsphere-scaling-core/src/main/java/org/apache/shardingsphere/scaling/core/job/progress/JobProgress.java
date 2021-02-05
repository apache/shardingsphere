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

package org.apache.shardingsphere.scaling.core.job.progress;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.job.position.PositionInitializerFactory;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskDelay;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTaskProgress;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Job progress.
 */
@Getter
@Setter
public final class JobProgress {
    
    private static final Gson GSON = new Gson();
    
    private static final Gson INVENTORY_POSITION_ADAPTED_GSON = new GsonBuilder().registerTypeHierarchyAdapter(Position.class, new InventoryPositionTypeAdapter()).create();
    
    private JobStatus status = JobStatus.RUNNING;
    
    private String databaseType;
    
    private Map<String, InventoryTaskProgress> inventoryTaskProgressMap;
    
    private Map<String, IncrementalTaskProgress> incrementalTaskProgressMap;
    
    /**
     * Get incremental position.
     *
     * @param dataSourceName data source name
     * @return incremental position
     */
    public Position<?> getIncrementalPosition(final String dataSourceName) {
        return incrementalTaskProgressMap.get(dataSourceName).getPosition();
    }
    
    /**
     * Get inventory position.
     *
     * @param tableName table name
     * @return inventory position
     */
    public Map<String, Position<?>> getInventoryPosition(final String tableName) {
        Pattern pattern = Pattern.compile(String.format("%s(#\\d+)?", tableName));
        return inventoryTaskProgressMap.entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPosition()));
    }
    
    /**
     * To json.
     *
     * @return json data
     */
    public String toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("status", status.name());
        result.addProperty("databaseType", databaseType);
        result.add("inventory", getInventoryJson());
        result.add("incremental", getIncrementalJson());
        return result.toString();
    }
    
    private JsonElement getInventoryJson() {
        JsonObject result = new JsonObject();
        result.add("finished", getInventoryFinishedJson());
        result.add("unfinished", getInventoryUnfinishedJson());
        return result;
    }
    
    private JsonElement getInventoryFinishedJson() {
        JsonArray result = new JsonArray();
        inventoryTaskProgressMap.entrySet().stream()
                .filter(entry -> entry.getValue().getPosition() instanceof FinishedPosition)
                .forEach(entry -> result.add(entry.getKey()));
        return result;
    }
    
    private JsonElement getInventoryUnfinishedJson() {
        JsonObject result = new JsonObject();
        inventoryTaskProgressMap.entrySet().stream()
                .filter(entry -> !(entry.getValue().getPosition() instanceof FinishedPosition))
                .forEach(entry -> result.add(entry.getKey(), GSON.toJsonTree(entry.getValue(), entry.getValue().getClass())));
        return result;
    }
    
    private JsonElement getIncrementalJson() {
        JsonObject result = new JsonObject();
        incrementalTaskProgressMap.forEach((key, value) -> result.add(key, getIncrementalJson(value)));
        return result;
    }
    
    private JsonElement getIncrementalJson(final IncrementalTaskProgress incrementalTaskProgress) {
        JsonObject result = new JsonObject();
        result.add("position", GSON.toJsonTree(incrementalTaskProgress.getPosition(), incrementalTaskProgress.getPosition().getClass()));
        result.add("delay", GSON.toJsonTree(incrementalTaskProgress.getIncrementalTaskDelay()));
        return result;
    }
    
    /**
     * From json.
     *
     * @param data json data
     * @return job position
     */
    public static JobProgress fromJson(final String data) {
        JobProgress result = new JobProgress();
        JsonObject jsonObject = GSON.fromJson(data, JsonObject.class);
        result.setStatus(JobStatus.valueOf(jsonObject.get("status").getAsString()));
        result.setDatabaseType(jsonObject.get("databaseType").getAsString());
        result.setInventoryTaskProgressMap(getInventoryTaskProgressMap(jsonObject.get("inventory").getAsJsonObject()));
        result.setIncrementalTaskProgressMap(getIncrementalTaskProgressMap(jsonObject.get("incremental").getAsJsonObject(), jsonObject.get("databaseType").getAsString()));
        return result;
    }
    
    private static Map<String, InventoryTaskProgress> getInventoryTaskProgressMap(final JsonObject inventory) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("inventory", inventory);
        return INVENTORY_POSITION_ADAPTED_GSON.fromJson(jsonObject, JobProgress.class).inventoryTaskProgressMap;
    }
    
    private static Map<String, IncrementalTaskProgress> getIncrementalTaskProgressMap(final JsonObject incremental, final String databaseType) {
        Class<?> incrementalPositionClass = PositionInitializerFactory.getPositionClass(databaseType);
        Map<String, IncrementalTaskProgress> result = Maps.newHashMap();
        for (String each : incremental.keySet()) {
            Position<?> position = (Position<?>) GSON.fromJson(incremental.get(each).getAsJsonObject().get("position"), incrementalPositionClass);
            IncrementalTaskDelay incrementalTaskDelay = GSON.fromJson(incremental.get(each).getAsJsonObject().get("delay"), IncrementalTaskDelay.class);
            result.put(each, new IncrementalTaskProgress(position, incrementalTaskDelay));
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
