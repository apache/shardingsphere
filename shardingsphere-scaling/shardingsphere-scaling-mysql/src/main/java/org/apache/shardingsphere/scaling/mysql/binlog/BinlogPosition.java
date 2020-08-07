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

package org.apache.shardingsphere.scaling.mysql.binlog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;

/**
 * Binlog Position.
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class BinlogPosition implements IncrementalPosition {
    
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    
    @Expose
    private final String filename;
    
    @Expose
    private final long position;
    
    private long serverId;
    
    @Override
    public final int compareTo(final Position position) {
        if (null == position) {
            return 1;
        }
        long o1 = toLong();
        long o2 = ((BinlogPosition) position).toLong();
        return Long.compare(o1, o2);
    }
    
    private long toLong() {
        return Long.parseLong(filename.substring(filename.lastIndexOf('.') + 1)) << 32 | position;
    }
    
    @Override
    public JsonElement toJson() {
        return GSON.toJsonTree(this);
    }
}
