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

package org.apache.shardingsphere.infra.util.props;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Properties utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesUtils {
    
    /**
     * Convert properties To string.
     *
     * @param props properties
     * @return properties string
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String toString(final Properties props) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = new TreeMap(props).keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = props.get(key);
            if (null == value) {
                continue;
            }
            result.append(String.format("'%s'='%s'", key, value));
            if (iterator.hasNext()) {
                result.append(",").append(' ');
            }
        }
        return result.toString();
    }
}
