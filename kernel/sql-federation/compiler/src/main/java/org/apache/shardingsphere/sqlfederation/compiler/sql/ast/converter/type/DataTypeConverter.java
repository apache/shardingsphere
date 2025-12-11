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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.type;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.Map;

/**
 * Data type converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataTypeConverter {
    
    private static final Map<String, SqlTypeName> REGISTRY = new CaseInsensitiveMap<>();
    
    static {
        registerDataType();
    }
    
    private static void registerDataType() {
        REGISTRY.put("INT", SqlTypeName.INTEGER);
        REGISTRY.put("INT2", SqlTypeName.SMALLINT);
        REGISTRY.put("INT4", SqlTypeName.INTEGER);
        REGISTRY.put("INT8", SqlTypeName.BIGINT);
        REGISTRY.put("MONEY", SqlTypeName.DECIMAL);
    }
    
    /**
     * Convert to SQL operator.
     *
     * @param dataType data type to be converted
     * @return converted SQL operator
     */
    public static SqlTypeName convert(final String dataType) {
        return REGISTRY.containsKey(dataType) ? REGISTRY.get(dataType) : SqlTypeName.valueOf(dataType.toUpperCase());
    }
}
