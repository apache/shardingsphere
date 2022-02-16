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

import org.apache.shardingsphere.encrypt.spi.EncryptDataTypeExtractor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL encrypt data type extractor.
 */
public final class MySQLEncryptDataTypeExtractor implements EncryptDataTypeExtractor {
    
    private final Map<String, String> compatibleDbDataTypes = initCompatibleDataTypes();
    
    private Map<String, String> initCompatibleDataTypes() {
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
    public Optional<Integer> getDataType(final String typeName, final Map<String, Integer> dataTypes) {
        return Optional.ofNullable(dataTypes.get(getRealDataTypeName(typeName)));
    }
    
    private String getRealDataTypeName(final String fullDataTypeDefinition) {
        String dataType = extractConfigDataType(fullDataTypeDefinition);
        return compatibleDbDataTypes.getOrDefault(dataType, dataType);
    }
    
    private String extractConfigDataType(final String fullDataTypeDefinition) {
        String dataType = fullDataTypeDefinition.trim().toLowerCase();
        Optional<String> multiWordDataType = findMultiWordDataType(dataType);
        if (multiWordDataType.isPresent()) {
            return multiWordDataType.get();
        }
        if (dataType.contains("(")) {
            return dataType.substring(0, dataType.indexOf("("));
        } 
        if (dataType.contains(" ")) {
            return dataType.substring(0, dataType.indexOf(" "));
        } 
        return dataType;
    }
    
    private Optional<String> findMultiWordDataType(final String dataType) {
        if (dataType.startsWith("character varying")) {
            return Optional.of("character varying");
        }
        if (dataType.startsWith("long varbinary")) {
            return Optional.of("long varbinary");
        }
        if (dataType.startsWith("long varchar")) {
            return Optional.of("long varchar");
        }
        if (dataType.startsWith("long char varying")) {
            return Optional.of("long char varying");
        }
        if (dataType.startsWith("national character varying")) {
            return Optional.of("national character varying");
        }
        if (dataType.startsWith("national char varying")) {
            return Optional.of("national char varying");
        }
        if (dataType.startsWith("nchar varying")) {
            return Optional.of("nchar varying");
        }
        if (dataType.startsWith("national character")) {
            return Optional.of("national character");
        }
        if (dataType.startsWith("national char")) {
            return Optional.of("char");
        }
        if (dataType.startsWith("national varchar")) {
            return Optional.of("national varchar");
        }
        if (dataType.startsWith("nchar varchar")) {
            return Optional.of("nchar varchar");
        }
        return Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
