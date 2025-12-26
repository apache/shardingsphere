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
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Properties segment.
 */
@Getter
public final class PropertiesSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final List<PropertySegment> properties = new LinkedList<>();
    
    public PropertiesSegment(final int startIndex, final int stopIndex) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }
    
    /**
     * Convert to properties map.
     *
     * @return properties map
     */
    public Properties toProperties() {
        Properties result = new Properties();
        for (PropertySegment each : properties) {
            result.setProperty(each.getKey(), each.getValue());
        }
        return result;
    }
    
    /**
     * Convert to map.
     *
     * @return map
     */
    public Map<String, String> toMap() {
        Map<String, String> result = new LinkedHashMap<>();
        for (PropertySegment each : properties) {
            result.put(each.getKey(), each.getValue());
        }
        return result;
    }
}
