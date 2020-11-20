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

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Placeholder position.
 */
@JsonAdapter(PlaceholderPosition.PositionTypeAdapter.class)
public final class PlaceholderPosition implements Position<PlaceholderPosition> {
    
    @Override
    public int compareTo(final PlaceholderPosition position) {
        return 1;
    }
    
    /**
     * Position type adapter.
     */
    public static class PositionTypeAdapter extends TypeAdapter<PlaceholderPosition> {
        
        @Override
        public void write(final JsonWriter out, final PlaceholderPosition value) throws IOException {
            out.beginArray();
            out.endArray();
        }
        
        @Override
        public PlaceholderPosition read(final JsonReader in) throws IOException {
            in.beginArray();
            in.endArray();
            return new PlaceholderPosition();
        }
    }
}
