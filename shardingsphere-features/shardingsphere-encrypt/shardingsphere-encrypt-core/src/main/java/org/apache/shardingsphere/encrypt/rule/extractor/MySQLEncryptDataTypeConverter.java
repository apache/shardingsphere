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

package org.apache.shardingsphere.encrypt.rule.extractor;

import org.apache.shardingsphere.encrypt.spi.EncryptDataTypeConverter;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.featuresupport.MySQLEncryptConfigDataTypeStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * MySQL encrypt data type extractor.
 * COMPATIBLE_DATA_TYPES ref to com.mysql.cj.MysqlType 8.0.26.
 */
public final class MySQLEncryptDataTypeConverter implements EncryptDataTypeConverter {
    
    private static final Map<String, String> COMPATIBLE_DATA_TYPES = new HashMap<>(37, 1);
    
    private final CacheOption cacheOption = new CacheOption(128, 1024L, 4);
    
    private final SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption, false);
    
    private final SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "STATEMENT", new Properties());
    
    static {
        COMPATIBLE_DATA_TYPES.put("bool", "tinyint");
        COMPATIBLE_DATA_TYPES.put("boolean", "tinyint");
        COMPATIBLE_DATA_TYPES.put("character varying", "varchar");
        COMPATIBLE_DATA_TYPES.put("fixed", "decimal");
        COMPATIBLE_DATA_TYPES.put("float4", "float");
        COMPATIBLE_DATA_TYPES.put("float8", "double");
        COMPATIBLE_DATA_TYPES.put("int1", "tinyint");
        COMPATIBLE_DATA_TYPES.put("int2", "smallint");
        COMPATIBLE_DATA_TYPES.put("int3", "mediumint");
        COMPATIBLE_DATA_TYPES.put("int4", "int");
        COMPATIBLE_DATA_TYPES.put("int8", "bigint");
        COMPATIBLE_DATA_TYPES.put("long varbinary", "mediumblob");
        COMPATIBLE_DATA_TYPES.put("long varchar", "mediumtext");
        COMPATIBLE_DATA_TYPES.put("long char varying", "mediumtext");
        COMPATIBLE_DATA_TYPES.put("long", "mediumtext");
        COMPATIBLE_DATA_TYPES.put("middleint", "mediumint");
        COMPATIBLE_DATA_TYPES.put("numeric", "decimal");
        COMPATIBLE_DATA_TYPES.put("nchar", "char");
        COMPATIBLE_DATA_TYPES.put("national character", "char");
        COMPATIBLE_DATA_TYPES.put("national char", "char");
        COMPATIBLE_DATA_TYPES.put("national varchar", "varchar");
        COMPATIBLE_DATA_TYPES.put("nvarchar", "varchar");
        COMPATIBLE_DATA_TYPES.put("nchar varchar", "varchar");
        COMPATIBLE_DATA_TYPES.put("national character varying", "varchar");
        COMPATIBLE_DATA_TYPES.put("national char varying", "varchar");
        COMPATIBLE_DATA_TYPES.put("nchar varying", "varchar");
        COMPATIBLE_DATA_TYPES.put("serial", "bigint");
        COMPATIBLE_DATA_TYPES.put("geometrycollection", "binary");
        COMPATIBLE_DATA_TYPES.put("year", "date");
        COMPATIBLE_DATA_TYPES.put("geometry", "binary");
        COMPATIBLE_DATA_TYPES.put("json", "text");
        COMPATIBLE_DATA_TYPES.put("point", "binary");
        COMPATIBLE_DATA_TYPES.put("multipoint", "binary");
        COMPATIBLE_DATA_TYPES.put("linestring", "binary");
        COMPATIBLE_DATA_TYPES.put("multilinestring", "binary");
        COMPATIBLE_DATA_TYPES.put("polygon", "binary");
        COMPATIBLE_DATA_TYPES.put("multipolygon", "binary");
    }
    
    @Override
    public Optional<Integer> convertDataType(final String typeName, final Map<String, Integer> dataTypes) {
        return Optional.ofNullable(dataTypes.get(getRealDataTypeName(typeName)));
    }
    
    private String getRealDataTypeName(final String fullDataTypeDefinition) {
        String dataType = extractConfigDataType(fullDataTypeDefinition);
        return COMPATIBLE_DATA_TYPES.getOrDefault(dataType, dataType);
    }
    
    private String extractConfigDataType(final String fullDataTypeDefinition) {
        return ((MySQLEncryptConfigDataTypeStatement) visitorEngine.visit(parserEngine.parse(fullDataTypeDefinition, true))).getDataTypeName().toLowerCase();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
