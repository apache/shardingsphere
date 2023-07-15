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

package org.apache.shardingsphere.data.pipeline.common.ingest.position.pk;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.StringPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.UnsupportedKeyPosition;

import java.util.List;

/**
 * Primary key position factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrimaryKeyPositionFactory {
    
    /**
     * New instance by string data.
     *
     * @param data string data
     * @return primary key position
     * @throws IllegalArgumentException illegal argument exception
     */
    public static IngestPosition newInstance(final String data) {
        List<String> parts = Splitter.on(',').splitToList(data);
        Preconditions.checkArgument(3 == parts.size(), "Unknown primary key position: " + data);
        Preconditions.checkArgument(1 == parts.get(0).length(), "Invalid primary key position type: " + parts.get(0));
        char type = parts.get(0).charAt(0);
        String beginValue = parts.get(1);
        String endValue = parts.get(2);
        switch (type) {
            case 'i':
                return new IntegerPrimaryKeyPosition(Long.parseLong(beginValue), Long.parseLong(endValue));
            case 's':
                return new StringPrimaryKeyPosition(beginValue, endValue);
            case 'u':
                return new UnsupportedKeyPosition();
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
    public static IngestPosition newInstance(final Object beginValue, final Object endValue) {
        if (beginValue instanceof Number) {
            return new IntegerPrimaryKeyPosition(((Number) beginValue).longValue(), null != endValue ? ((Number) endValue).longValue() : Long.MAX_VALUE);
        }
        if (beginValue instanceof CharSequence) {
            return new StringPrimaryKeyPosition(beginValue.toString(), null != endValue ? endValue.toString() : null);
        }
        // TODO support more types, e.g. byte[] (MySQL varbinary)
        return new UnsupportedKeyPosition();
    }
}
