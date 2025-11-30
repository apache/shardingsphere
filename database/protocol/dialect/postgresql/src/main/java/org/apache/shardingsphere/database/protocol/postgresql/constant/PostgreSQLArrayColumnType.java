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

package org.apache.shardingsphere.database.protocol.postgresql.constant;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Array column types for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLArrayColumnType {
    
    private static final Map<String, Integer> COLUMN_TYPE_NAME_OID_MAP = new HashMap<>(128, 1F);
    
    // CHECKSTYLE:OFF
    static {
        COLUMN_TYPE_NAME_OID_MAP.put("_aclitem", 1034);
        COLUMN_TYPE_NAME_OID_MAP.put("_array_tc_0", 16425);
        COLUMN_TYPE_NAME_OID_MAP.put("_array_tc_1", 16433);
        COLUMN_TYPE_NAME_OID_MAP.put("_bit", 1561);
        COLUMN_TYPE_NAME_OID_MAP.put("_bool", 1000);
        COLUMN_TYPE_NAME_OID_MAP.put("_box", 1020);
        COLUMN_TYPE_NAME_OID_MAP.put("_bpchar", 1014);
        COLUMN_TYPE_NAME_OID_MAP.put("_bytea", 1001);
        COLUMN_TYPE_NAME_OID_MAP.put("_cardinal_number", 13031);
        COLUMN_TYPE_NAME_OID_MAP.put("_char", 1002);
        COLUMN_TYPE_NAME_OID_MAP.put("_character_data", 13034);
        COLUMN_TYPE_NAME_OID_MAP.put("_cid", 1012);
        COLUMN_TYPE_NAME_OID_MAP.put("_cidr", 651);
        COLUMN_TYPE_NAME_OID_MAP.put("_circle", 719);
        COLUMN_TYPE_NAME_OID_MAP.put("_cstring", 1263);
        COLUMN_TYPE_NAME_OID_MAP.put("_date", 1182);
        COLUMN_TYPE_NAME_OID_MAP.put("_daterange", 3913);
        COLUMN_TYPE_NAME_OID_MAP.put("_float4", 1021);
        COLUMN_TYPE_NAME_OID_MAP.put("_float8", 1022);
        COLUMN_TYPE_NAME_OID_MAP.put("_gtsvector", 3644);
        COLUMN_TYPE_NAME_OID_MAP.put("_inet", 1041);
        COLUMN_TYPE_NAME_OID_MAP.put("_int2", 1005);
        COLUMN_TYPE_NAME_OID_MAP.put("_int2vector", 1006);
        COLUMN_TYPE_NAME_OID_MAP.put("_int4", 1007);
        COLUMN_TYPE_NAME_OID_MAP.put("_int4range", 3905);
        COLUMN_TYPE_NAME_OID_MAP.put("_int8", 1016);
        COLUMN_TYPE_NAME_OID_MAP.put("_int8range", 3927);
        COLUMN_TYPE_NAME_OID_MAP.put("_interval", 1187);
        COLUMN_TYPE_NAME_OID_MAP.put("_json", 199);
        COLUMN_TYPE_NAME_OID_MAP.put("_jsonb", 3807);
        COLUMN_TYPE_NAME_OID_MAP.put("_jsonpath", 4073);
        COLUMN_TYPE_NAME_OID_MAP.put("_line", 629);
        COLUMN_TYPE_NAME_OID_MAP.put("_lseg", 1018);
        COLUMN_TYPE_NAME_OID_MAP.put("_macaddr", 1040);
        COLUMN_TYPE_NAME_OID_MAP.put("_macaddr8", 775);
        COLUMN_TYPE_NAME_OID_MAP.put("_money", 791);
        COLUMN_TYPE_NAME_OID_MAP.put("_name", 1003);
        COLUMN_TYPE_NAME_OID_MAP.put("_numeric", 1231);
        COLUMN_TYPE_NAME_OID_MAP.put("_numrange", 3907);
        COLUMN_TYPE_NAME_OID_MAP.put("_oid", 1028);
        COLUMN_TYPE_NAME_OID_MAP.put("_oidvector", 1013);
        COLUMN_TYPE_NAME_OID_MAP.put("_path", 1019);
        COLUMN_TYPE_NAME_OID_MAP.put("_pg_foreign_data_wrappers", 13265);
        COLUMN_TYPE_NAME_OID_MAP.put("_pg_foreign_servers", 13274);
        COLUMN_TYPE_NAME_OID_MAP.put("_pg_foreign_table_columns", 13258);
        COLUMN_TYPE_NAME_OID_MAP.put("_pg_foreign_tables", 13284);
        COLUMN_TYPE_NAME_OID_MAP.put("_pg_lsn", 3221);
        COLUMN_TYPE_NAME_OID_MAP.put("_pg_user_mappings", 13294);
        COLUMN_TYPE_NAME_OID_MAP.put("_point", 1017);
        COLUMN_TYPE_NAME_OID_MAP.put("_polygon", 1027);
        COLUMN_TYPE_NAME_OID_MAP.put("_record", 2287);
        COLUMN_TYPE_NAME_OID_MAP.put("_refcursor", 2201);
        COLUMN_TYPE_NAME_OID_MAP.put("_regclass", 2210);
        COLUMN_TYPE_NAME_OID_MAP.put("_regconfig", 3735);
        COLUMN_TYPE_NAME_OID_MAP.put("_regdictionary", 3770);
        COLUMN_TYPE_NAME_OID_MAP.put("_regnamespace", 4090);
        COLUMN_TYPE_NAME_OID_MAP.put("_regoper", 2208);
        COLUMN_TYPE_NAME_OID_MAP.put("_regoperator", 2209);
        COLUMN_TYPE_NAME_OID_MAP.put("_regproc", 1008);
        COLUMN_TYPE_NAME_OID_MAP.put("_regprocedure", 2207);
        COLUMN_TYPE_NAME_OID_MAP.put("_regrole", 4097);
        COLUMN_TYPE_NAME_OID_MAP.put("_regtype", 2211);
        COLUMN_TYPE_NAME_OID_MAP.put("_sql_identifier", 13036);
        COLUMN_TYPE_NAME_OID_MAP.put("_test_array", 16395);
        COLUMN_TYPE_NAME_OID_MAP.put("_text", 1009);
        COLUMN_TYPE_NAME_OID_MAP.put("_tid", 1010);
        COLUMN_TYPE_NAME_OID_MAP.put("_time", 1183);
        COLUMN_TYPE_NAME_OID_MAP.put("_time_stamp", 13041);
        COLUMN_TYPE_NAME_OID_MAP.put("_timestamp", 1115);
        COLUMN_TYPE_NAME_OID_MAP.put("_timestamptz", 1185);
        COLUMN_TYPE_NAME_OID_MAP.put("_timetz", 1270);
        COLUMN_TYPE_NAME_OID_MAP.put("_tsquery", 3645);
        COLUMN_TYPE_NAME_OID_MAP.put("_tsrange", 3909);
        COLUMN_TYPE_NAME_OID_MAP.put("_tstzrange", 3911);
        COLUMN_TYPE_NAME_OID_MAP.put("_tsvector", 3643);
        COLUMN_TYPE_NAME_OID_MAP.put("_txid_snapshot", 2949);
        COLUMN_TYPE_NAME_OID_MAP.put("_uuid", 2951);
        COLUMN_TYPE_NAME_OID_MAP.put("_varbit", 1563);
        COLUMN_TYPE_NAME_OID_MAP.put("_varchar", 1015);
        COLUMN_TYPE_NAME_OID_MAP.put("_xid", 1011);
        COLUMN_TYPE_NAME_OID_MAP.put("_xml", 143);
        COLUMN_TYPE_NAME_OID_MAP.put("_yes_or_no", 13043);
    }
    // CHECKSTYLE:ON
    
    /**
     * Get type oid by database-specific column type name.
     *
     * @param columnTypeName PostgreSQL column type name, e.g. {@code int4}
     * @return type oid, e.g. {@code 23} for {@code int4}
     */
    public static int getTypeOid(final String columnTypeName) {
        Preconditions.checkArgument(COLUMN_TYPE_NAME_OID_MAP.containsKey(columnTypeName), "Cannot find PostgreSQL type oid for columnTypeName '%s'", columnTypeName);
        return COLUMN_TYPE_NAME_OID_MAP.get(columnTypeName);
    }
}
