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

package org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Properties segment.
 */
@RequiredArgsConstructor
@Getter
public final class PropertiesSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final List<PropertySegment> properties = new LinkedList<>();
    
    /**
     * Convert to map.
     *
     * @return map
     */
    public Map<String, String> toMap() {
        return properties.stream()
                .collect(Collectors.toMap(PropertySegment::getKey, PropertySegment::getValue, (existingValue, newValue) -> newValue, () -> new LinkedHashMap<>(properties.size(), 1F)));
    }
}
