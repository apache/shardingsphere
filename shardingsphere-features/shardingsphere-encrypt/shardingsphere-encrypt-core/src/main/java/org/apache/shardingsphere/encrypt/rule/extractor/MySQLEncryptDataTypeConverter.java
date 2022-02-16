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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * MySQL encrypt data type extractor.
 * COMPATIBLE_DATA_TYPES ref to com.mysql.cj.MysqlType 8.0.26.
 */
public final class MySQLEncryptDataTypeConverter implements EncryptDataTypeConverter {
    
    private static final Map<String, String> COMPATIBLE_DATA_TYPES = initCompatibleDataTypes();
    
    private final CacheOption cacheOption = new CacheOption(128, 1024L, 4);
    
    private final SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption, false);
    
    private final SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "STATEMENT", new Properties());
    
    private static Map<String, String> initCompatibleDataTypes() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("bool", "tinyint");
        result.put("boolean", "tinyint");
        result.put("character varying", "varchar");
        result.put("fixed", "decimal");
        result.put("float4", "float");
        result.put("float8", "double");
        result.put("int1", "tinyint");
        result.put("int2", "smallint");
        result.put("int3", "mediumint");
        result.put("int4", "int");
        result.put("int8", "bigint");
        result.put("long varbinary", "mediumblob");
        result.put("long varchar", "mediumtext");
        result.put("long char varying", "mediumtext");
        result.put("long", "mediumtext");
        result.put("middleint", "mediumint");
        result.put("numeric", "decimal");
        result.put("nchar", "char");
        result.put("national character", "char");
        result.put("national char", "char");
        result.put("national varchar", "varchar");
        result.put("nvarchar", "varchar");
        result.put("nchar varchar", "varchar");
        result.put("national character varying", "varchar");
        result.put("national char varying", "varchar");
        result.put("nchar varying", "varchar");
        result.put("serial", "bigint");
        result.put("geometrycollection", "binary");
        result.put("year", "date");
        result.put("geometry", "binary");
        result.put("json", "text");
        result.put("point", "binary");
        result.put("multipoint", "binary");
        result.put("linestring", "binary");
        result.put("multilinestring", "binary");
        result.put("polygon", "binary");
        result.put("multipolygon", "binary");
        return result;
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
