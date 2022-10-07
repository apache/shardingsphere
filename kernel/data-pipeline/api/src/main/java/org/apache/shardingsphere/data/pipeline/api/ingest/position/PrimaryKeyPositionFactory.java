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
import lombok.NonNull;

/**
 * Primary key position factory.
 */
public final class PrimaryKeyPositionFactory {
    
    /**
     * New instance by string data.
     *
     * @param data string data
     * @return primary key position
     */
    public static IngestPosition<?> newInstance(final String data) {
        String[] array = data.split(",");
        Preconditions.checkArgument(3 == array.length, "Unknown primary key position: " + data);
        Preconditions.checkArgument(1 == array[0].length(), "Invalid primary key position type: " + array[0]);
        char type = array[0].charAt(0);
        String beginValue = array[1];
        String endValue = array[2];
        switch (type) {
            case 'i':
                return new IntegerPrimaryKeyPosition(Long.parseLong(beginValue), Long.parseLong(endValue));
            case 's':
                return new StringPrimaryKeyPosition(beginValue, endValue);
            default:
                throw new IllegalArgumentException("Unknown primary key position type: " + type);
        }
    }
    
    /**
     * New instance by begin value and end value.
     *
     * @param beginValue begin value
     * @param endValue end value
     * @return ingest position
     */
    public static IngestPosition<?> newInstance(final @NonNull Object beginValue, final @NonNull Object endValue) {
        if (beginValue instanceof Number) {
            return new IntegerPrimaryKeyPosition(((Number) beginValue).longValue(), ((Number) endValue).longValue());
        }
        if (beginValue instanceof CharSequence) {
            return new StringPrimaryKeyPosition(beginValue.toString(), endValue.toString());
        }
        throw new IllegalArgumentException("Unknown begin value type: " + beginValue.getClass().getName());
    }
}
