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

package org.apache.shardingsphere.orchestration.center.util;

/**
 * Config key utils.
 */
public final class ConfigKeyUtils {
    
    private static final String DOT_SEPARATOR = ".";
    
    private static final String PATH_SEPARATOR = "/";
    
    /**
     * Convert path to key.
     * 
     * @param path config path
     * @return config key
     */
    public static String pathToKey(final String path) {
        String key = path.replace(PATH_SEPARATOR, DOT_SEPARATOR);
        return key.substring(key.indexOf(DOT_SEPARATOR) + 1);
    }
    
    /**
     * Convert key to path.
     * 
     * @param key config key
     * @return config path
     */
    public static String keyToPath(final String key) {
        return PATH_SEPARATOR + key.replace(DOT_SEPARATOR, PATH_SEPARATOR);
    }
}
