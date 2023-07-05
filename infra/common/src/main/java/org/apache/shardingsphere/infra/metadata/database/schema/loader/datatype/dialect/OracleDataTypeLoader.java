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
 * Oracle data type loader.
 */
public final class OracleDataTypeLoader implements DialectDataTypeLoader {
    
    @Override
    public Map<String, Integer> load() throws SQLException {
        Map<String, Integer> result = new HashMap<>(18, 1F);
        result.putIfAbsent("NUMBER", Types.NUMERIC);
        result.putIfAbsent("BINARY_FLOAT", 100);
        result.putIfAbsent("BINARY_DOUBLE", 101);
        result.putIfAbsent("INTERVAL YEAR", -103);
        result.putIfAbsent("INTERVAL DAY", -104);
        result.putIfAbsent("BFILE", -13);
        result.putIfAbsent("ROWID", Types.ROWID);
        result.putIfAbsent("UROWID", Types.ROWID);
        result.putIfAbsent("ANYDATA", Types.OTHER);
        result.putIfAbsent("ANYTYPE", Types.OTHER);
        result.putIfAbsent("ANYDATASET", Types.OTHER);
        result.putIfAbsent("XMLTYPE", Types.SQLXML);
        result.putIfAbsent("URITYPE", Types.STRUCT);
        result.putIfAbsent("SDO_ELEM_INFO_ARRAY", Types.OTHER);
        result.putIfAbsent("SDO_GEOMETRY", Types.STRUCT);
        result.putIfAbsent("SDO_ORDINATE_ARRAY", Types.OTHER);
        result.putIfAbsent("SDO_TOPO_GEOMETRY", Types.STRUCT);
        result.putIfAbsent("SDO_GEORASTER", Types.STRUCT);
        return result;
    }
    
    @Override
    public String getType() {
        return "Oracle";
    }
}
