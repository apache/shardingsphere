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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * Use primary key as position.
 */
@RequiredArgsConstructor
@Getter
@JsonAdapter(PrimaryKeyPosition.PositionTypeAdapter.class)
public final class PrimaryKeyPosition implements Position<PrimaryKeyPosition> {
    
    private final long beginValue;
    
    private final long endValue;
    
    @Override
    public int compareTo(final PrimaryKeyPosition position) {
        if (null == position) {
            return 1;
        }
        return Long.compare(beginValue, position.beginValue);
    }
    
    /**
     * Position type adapter.
     */
    public static class PositionTypeAdapter extends TypeAdapter<PrimaryKeyPosition> {
        
        @Override
        public void write(final JsonWriter out, final PrimaryKeyPosition value) throws IOException {
            out.beginArray();
            out.value(value.getBeginValue());
            out.value(value.getEndValue());
            out.endArray();
        }
        
        @Override
        public PrimaryKeyPosition read(final JsonReader in) throws IOException {
            in.beginArray();
            PrimaryKeyPosition result = new PrimaryKeyPosition(in.nextLong(), in.nextLong());
            in.endArray();
            return result;
        }
    }
}
