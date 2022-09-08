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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.external.sql.UnsupportedSQLOperationException;

/**
 * Hint source type.
 */
@RequiredArgsConstructor
@Getter
public enum HintSourceType {
    
    AUTO("auto"),
    
    WRITE("write");
    
    private final String value;
    
    /**
     * Convert string to HintSourceType.
     *
     * @param value value
     * @return hint source type
     */
    public static HintSourceType typeOf(final String value) {
        for (HintSourceType each : values()) {
            if (each.value.equalsIgnoreCase(value)) {
                return each;
            }
        }
        throw new UnsupportedSQLOperationException(String.format("unsupported hint source type: %s", value));
    }
}
