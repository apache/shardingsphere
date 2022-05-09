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

package org.apache.shardingsphere.data.pipeline.api.ingest.position;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Integer primary key position.
 */
@RequiredArgsConstructor
@Getter
public final class IntegerPrimaryKeyPosition implements IngestPosition<IntegerPrimaryKeyPosition>, PrimaryKeyPosition<Number> {
    
    private final long beginValue;
    
    private final long endValue;
    
    /**
     * Init by string data.
     *
     * @param data string data
     * @return primary key position
     */
    public static IntegerPrimaryKeyPosition init(final String data) {
        String[] array = data.split(",");
        Preconditions.checkArgument(array.length == 2, "Unknown primary key position: " + data);
        return new IntegerPrimaryKeyPosition(Long.parseLong(array[0]), Long.parseLong(array[1]));
    }
    
    @Override
    public int compareTo(final IntegerPrimaryKeyPosition position) {
        return Long.compare(beginValue, position.beginValue);
    }
    
    @Override
    public String toString() {
        return String.format("%d,%d", beginValue, endValue);
    }
}
