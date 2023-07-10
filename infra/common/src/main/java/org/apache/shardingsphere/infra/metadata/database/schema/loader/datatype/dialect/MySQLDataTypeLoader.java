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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.datatype.dialect;

import org.apache.shardingsphere.infra.metadata.database.schema.loader.datatype.DialectDataTypeLoader;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL data type loader.
 */
public final class MySQLDataTypeLoader implements DialectDataTypeLoader {
    
    @Override
    public Map<String, Integer> load() throws SQLException {
        Map<String, Integer> result = new HashMap<>(10, 1F);
        result.putIfAbsent("JSON", Types.LONGVARCHAR);
        result.putIfAbsent("GEOMETRY", Types.BINARY);
        result.putIfAbsent("GEOMETRYCOLLECTION", Types.BINARY);
        result.putIfAbsent("YEAR", Types.DATE);
        result.putIfAbsent("POINT", Types.BINARY);
        result.putIfAbsent("MULTIPOINT", Types.BINARY);
        result.putIfAbsent("POLYGON", Types.BINARY);
        result.putIfAbsent("MULTIPOLYGON", Types.BINARY);
        result.putIfAbsent("LINESTRING", Types.BINARY);
        result.putIfAbsent("MULTILINESTRING", Types.BINARY);
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
