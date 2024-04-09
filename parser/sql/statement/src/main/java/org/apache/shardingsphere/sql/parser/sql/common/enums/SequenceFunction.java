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

package org.apache.shardingsphere.sql.parser.sql.common.enums;

import com.cedarsoftware.util.CaseInsensitiveMap;

import java.util.Map;
import java.util.Optional;

/**
 * Sequence function.
 */
public enum SequenceFunction {
    
    CURRVAL, NEXTVAL;
    
    private static final Map<String, SequenceFunction> SEQUENCE_FUNCTIONS = new CaseInsensitiveMap<>(2, 1F);
    
    static {
        SEQUENCE_FUNCTIONS.put("CURRVAL", CURRVAL);
        SEQUENCE_FUNCTIONS.put("NEXTVAL", NEXTVAL);
    }
    
    /**
     * Get sequence function value from text.
     *
     * @param text text
     * @return sequence function value
     */
    public static Optional<SequenceFunction> valueFrom(final String text) {
        return Optional.ofNullable(SEQUENCE_FUNCTIONS.get(text));
    }
}
