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

package org.apache.shardingsphere.mcp.metadata.query;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;

/**
 * Metadata object query condition.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetadataObjectQueryCondition {
    
    private final String schemaName;
    
    private final String objectName;
    
    private final String parentObjectType;
    
    private final String parentObjectName;
    
    /**
     * Create empty condition.
     *
     * @return empty condition
     */
    public static MetadataObjectQueryCondition empty() {
        return new MetadataObjectQueryCondition("", "", "", "");
    }
    
    /**
     * Create schema condition.
     *
     * @param schemaName schema name
     * @return schema condition
     */
    public static MetadataObjectQueryCondition schema(final String schemaName) {
        return new MetadataObjectQueryCondition(schemaName, "", "", "");
    }
    
    /**
     * Create schema and object condition.
     *
     * @param schemaName schema name
     * @param objectName object name
     * @return schema and object condition
     */
    public static MetadataObjectQueryCondition schemaAndObject(final String schemaName, final String objectName) {
        return new MetadataObjectQueryCondition(schemaName, objectName, "", "");
    }
    
    /**
     * Create parent condition.
     *
     * @param schemaName schema name
     * @param parentObjectType parent object type
     * @param parentObjectName parent object name
     * @return parent condition
     */
    public static MetadataObjectQueryCondition parent(final String schemaName, final String parentObjectType, final String parentObjectName) {
        return new MetadataObjectQueryCondition(schemaName, "", parentObjectType, parentObjectName);
    }
    
    /**
     * Create parent and object condition.
     *
     * @param schemaName schema name
     * @param parentObjectType parent object type
     * @param parentObjectName parent object name
     * @param objectName object name
     * @return parent and object condition
     */
    public static MetadataObjectQueryCondition parentAndObject(final String schemaName, final String parentObjectType,
                                                               final String parentObjectName, final String objectName) {
        return new MetadataObjectQueryCondition(schemaName, objectName, parentObjectType, parentObjectName);
    }
    
    /**
     * Create custom condition.
     *
     * @param schemaName schema name
     * @param objectName object name
     * @param parentObjectType parent object type
     * @param parentObjectName parent object name
     * @return custom condition
     */
    public static MetadataObjectQueryCondition custom(final String schemaName, final String objectName,
                                                      final String parentObjectType, final String parentObjectName) {
        return new MetadataObjectQueryCondition(schemaName, objectName, parentObjectType, parentObjectName);
    }
    
    boolean matches(final MetadataObject metadataObject) {
        return matchesSchema(metadataObject) && matchesObjectName(metadataObject)
                && matchesParentObjectType(metadataObject) && matchesParentObjectName(metadataObject);
    }
    
    private boolean matchesSchema(final MetadataObject metadataObject) {
        return schemaName.isEmpty() || schemaName.equals(metadataObject.getSchema());
    }
    
    private boolean matchesObjectName(final MetadataObject metadataObject) {
        return objectName.isEmpty() || objectName.equals(metadataObject.getName());
    }
    
    private boolean matchesParentObjectType(final MetadataObject metadataObject) {
        return parentObjectType.isEmpty() || parentObjectType.equals(metadataObject.getParentObjectType());
    }
    
    private boolean matchesParentObjectName(final MetadataObject metadataObject) {
        return parentObjectName.isEmpty() || parentObjectName.equals(metadataObject.getParentObjectName());
    }
}
