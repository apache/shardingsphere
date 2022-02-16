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

package org.apache.shardingsphere.encrypt.spi.context;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.spi.EncryptDataTypeConverter;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.spi.singleton.SingletonSPIRegistry;

import java.util.Map;
import java.util.Optional;

/**
 * Encrypt column data type.
 */
@Getter
public final class EncryptColumnDataType {
    
    private final String typeName;
    
    private final int dataType;
    
    public EncryptColumnDataType(final String typeName, final Map<String, Integer> dataTypes, final DatabaseType databaseType) {
        this.typeName = typeName;
        this.dataType = getDataTypeByTypeName(typeName, dataTypes, databaseType); 
    }
    
    private static Integer getDataTypeByTypeName(final String typeName, final Map<String, Integer> dataTypes, final DatabaseType databaseType) {
        Optional<EncryptDataTypeConverter> converter = findEncryptDataTypeExtractor(databaseType);
        Optional<Integer> result = converter.isPresent() ? converter.get().convertDataType(typeName, dataTypes) : getDataTypeByDefault(typeName, dataTypes);
        if (!result.isPresent()) {
            throw new ShardingSphereConfigurationException("Can not get data types, please check config: %s", typeName);
        }
        return result.get();
    }
    
    private static Optional<Integer> getDataTypeByDefault(final String fullDataTypeDefinition, final Map<String, Integer> dataTypes) {
        String dataType = fullDataTypeDefinition.trim().toLowerCase();
        if (dataType.contains("(")) {
            dataType = dataType.substring(0, dataType.indexOf("("));
        } else if (dataType.contains(" ")) {
            dataType = dataType.substring(0, dataType.indexOf(" "));
        }
        return Optional.ofNullable(dataTypes.get(dataType));
    }
    
    private static Optional<EncryptDataTypeConverter> findEncryptDataTypeExtractor(final DatabaseType databaseType) {
        return Optional.ofNullable(SingletonSPIRegistry.getSingletonInstancesMap(EncryptDataTypeConverter.class, EncryptDataTypeConverter::getDatabaseType).get(databaseType.getName()));
    }
}
