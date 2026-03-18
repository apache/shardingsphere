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

package org.apache.shardingsphere.infra.util.string;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Hex string utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HexStringUtils {
    
    /**
     * Convert byte array to hex string.
     *
     * @param bytes bytes
     * @return hex string
     */
    public static String toHexString(final byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte each : bytes) {
            String hex = Integer.toHexString(0xff & each);
            if (1 == hex.length()) {
                result.append('0');
            }
            result.append(hex);
        }
        return result.toString();
    }
}
