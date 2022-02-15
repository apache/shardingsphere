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
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;

import java.util.Map;

/**
 * Encrypt column data type.
 */
@Getter
public final class EncryptColumnDataType {
    
    private final String typeName;
    
    private final int dataType;
    
    public EncryptColumnDataType(final String typeName, final Map<String, Integer> dataTypes) {
        this.typeName = typeName;
        this.dataType = getDataTypeByTypeName(typeName, dataTypes); 
    }
    
    private static Integer getDataTypeByTypeName(final String typeName, final Map<String, Integer> dataTypes) {
        Integer result = dataTypes.get(getExactlyTypeName(typeName));
        if (null == result) {
            throw new ShardingSphereConfigurationException("Can not get data types, please check config: %s", typeName);
        }
        return result;
    }
    
    private static String getExactlyTypeName(final String dataTypeName) {
        String dataType = dataTypeName.trim().toLowerCase();
        if (dataType.contains("(")) {
            return dataType.substring(0, dataType.indexOf("("));
        } else if (dataType.contains(" ")) {
            return dataType.substring(0, dataType.indexOf(" "));
        } else {
            return dataType;
        }
        // TODO refactor as dialect config extractor
    }
}
