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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;

/**
 * Random string utils.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class RandomStrings {
    
    private static final RandomStringGenerator GENERATOR;
    
    static {
        char[][] pairs = {{'a', 'z'}, {'A', 'Z'}, {'0', '9'}};
        GENERATOR = RandomStringGenerator.builder().withinRange(pairs).build();
    }
    
    /**
     * Generate a random alphanumeric string of the given length.
     *
     * @param count desired string length
     * @return random alphanumeric string
     */
    public static String randomAlphanumeric(final int count) {
        return GENERATOR.generate(count);
    }
}
