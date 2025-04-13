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

package org.apache.shardingsphere.infra.database.mysql.metadata.database.option;

import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;

import java.math.BigInteger;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data type option for MySQL.
 */
public final class MySQLDataTypeOption implements DialectDataTypeOption {
    
    @Override
    public Map<String, Integer> getExtraDataTypes() {
        Map<String, Integer> result = new HashMap<>(10, 1F);
        result.put("JSON", Types.LONGVARCHAR);
        result.put("GEOMETRY", Types.BINARY);
        result.put("GEOMETRYCOLLECTION", Types.BINARY);
        result.put("YEAR", Types.DATE);
        result.put("POINT", Types.BINARY);
        result.put("MULTIPOINT", Types.BINARY);
        result.put("POLYGON", Types.BINARY);
        result.put("MULTIPOLYGON", Types.BINARY);
        result.put("LINESTRING", Types.BINARY);
        result.put("MULTILINESTRING", Types.BINARY);
        return result;
    }
    
    @Override
    public Optional<Class<?>> findExtraSQLTypeClass(final int dataType, final boolean unsigned) {
        if (Types.TINYINT == dataType || Types.SMALLINT == dataType) {
            return Optional.of(Integer.class);
        }
        if (Types.INTEGER == dataType) {
            return unsigned ? Optional.of(Long.class) : Optional.of(Integer.class);
        }
        if (Types.BIGINT == dataType) {
            return unsigned ? Optional.of(BigInteger.class) : Optional.of(Long.class);
        }
        return Optional.empty();
    }
}
