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

package org.apache.shardingsphere.encrypt.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Encrypt node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptNodeConverter {
    
    private static final String TABLES = "tables";
    
    private static final String ENCRYPTORS = "encryptors";
    
    /**
     * Get table name path.
     * 
     * @param tableName table name
     * @return table name path
     */
    public static String getTableNamePath(final String tableName) {
        return String.join("/", TABLES, tableName);
    }
    
    /**
     * Get encryptor path.
     * 
     * @param encryptorName encryptor name
     * @return encryptor path
     */
    public static String getEncryptorPath(final String encryptorName) {
        return String.join("/", ENCRYPTORS, encryptorName);
    }
}
