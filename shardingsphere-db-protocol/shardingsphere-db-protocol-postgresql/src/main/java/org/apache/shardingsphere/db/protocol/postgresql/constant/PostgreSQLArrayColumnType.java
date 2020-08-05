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

package org.apache.shardingsphere.db.protocol.postgresql.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Array column types for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLArrayColumnType {
    
    private static final String ORIGINAL_RECORD_LINES =
        "_aclitem 1034\n"
            + "_array_tc_0 16425\n"
            + "_array_tc_1 16433\n"
            + "_bit 1561\n"
            + "_bool 1000\n"
            + "_box 1020\n"
            + "_bpchar 1014\n"
            + "_bytea 1001\n"
            + "_cardinal_number 13031\n"
            + "_char 1002\n"
            + "_character_data 13034\n"
            + "_cid 1012\n"
            + "_cidr 651\n"
            + "_circle 719\n"
            + "_cstring 1263\n"
            + "_date 1182\n"
            + "_daterange 3913\n"
            + "_float4 1021\n"
            + "_float8 1022\n"
            + "_gtsvector 3644\n"
            + "_inet 1041\n"
            + "_int2 1005\n"
            + "_int2vector 1006\n"
            + "_int4 1007\n"
            + "_int4range 3905\n"
            + "_int8 1016\n"
            + "_int8range 3927\n"
            + "_interval 1187\n"
            + "_json 199\n"
            + "_jsonb 3807\n"
            + "_jsonpath 4073\n"
            + "_line 629\n"
            + "_lseg 1018\n"
            + "_macaddr 1040\n"
            + "_macaddr8 775\n"
            + "_money 791\n"
            + "_name 1003\n"
            + "_numeric 1231\n"
            + "_numrange 3907\n"
            + "_oid 1028\n"
            + "_oidvector 1013\n"
            + "_path 1019\n"
            + "_pg_foreign_data_wrappers 13265\n"
            + "_pg_foreign_servers 13274\n"
            + "_pg_foreign_table_columns 13258\n"
            + "_pg_foreign_tables 13284\n"
            + "_pg_lsn 3221\n"
            + "_pg_user_mappings 13294\n"
            + "_point 1017\n"
            + "_polygon 1027\n"
            + "_record 2287\n"
            + "_refcursor 2201\n"
            + "_regclass 2210\n"
            + "_regconfig 3735\n"
            + "_regdictionary 3770\n"
            + "_regnamespace 4090\n"
            + "_regoper 2208\n"
            + "_regoperator 2209\n"
            + "_regproc 1008\n"
            + "_regprocedure 2207\n"
            + "_regrole 4097\n"
            + "_regtype 2211\n"
            + "_sql_identifier 13036\n"
            + "_test_array 16395\n"
            + "_text 1009\n"
            + "_tid 1010\n"
            + "_time 1183\n"
            + "_time_stamp 13041\n"
            + "_timestamp 1115\n"
            + "_timestamptz 1185\n"
            + "_timetz 1270\n"
            + "_tsquery 3645\n"
            + "_tsrange 3909\n"
            + "_tstzrange 3911\n"
            + "_tsvector 3643\n"
            + "_txid_snapshot 2949\n"
            + "_uuid 2951\n"
            + "_varbit 1563\n"
            + "_varchar 1015\n"
            + "_xid 1011\n"
            + "_xml 143\n"
            + "_yes_or_no 13043";
    
    private static final Map<String, Integer> COLUMN_TYPE_NAME_OID_MAP = new HashMap<>(128, 1.0F);
    
    static {
        for (String line : ORIGINAL_RECORD_LINES.split("\n")) {
            String[] values = line.split(" ");
            COLUMN_TYPE_NAME_OID_MAP.put(values[0], Integer.parseInt(values[1]));
        }
    }
    
    /**
     * Get type oid by database-specific column type name.
     *
     * @param columnTypeName PostgreSQL column type name, e.g. {@code int4}
     * @return type oid, e.g. {@code 23} for {@code int4}
     * @throws IllegalArgumentException if no type oid could be found
     */
    public static int getTypeOidByColumnTypeName(final String columnTypeName) throws IllegalArgumentException {
        if (COLUMN_TYPE_NAME_OID_MAP.containsKey(columnTypeName)) {
            return COLUMN_TYPE_NAME_OID_MAP.get(columnTypeName);
        }
        throw new IllegalArgumentException(String.format("Cannot find PostgreSQL type oid for columnTypeName '%s'", columnTypeName));
    }
    
}
