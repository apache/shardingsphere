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

package org.apache.shardingsphere.infra.props;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Properties converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesConverter {
    
    /**
     * Convert properties to string content.
     * 
     * @param props properties to be converted
     * @return converted string content
     */
    public static String convert(final Properties props) {
        if (null == props) {
            return "";
        }
        Map<Object, Object> sortedProps = new LinkedHashMap<>();
        props.keySet().stream().map(Object::toString).sorted().forEach(each -> sortedProps.put(each, props.get(each)));
        return sortedProps.isEmpty() ? "" : JsonUtils.toJsonString(sortedProps);
    }
}
