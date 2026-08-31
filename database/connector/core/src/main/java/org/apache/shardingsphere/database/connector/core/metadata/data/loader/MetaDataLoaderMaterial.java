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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Meta data loader material.
 */
@RequiredArgsConstructor
@Getter
public final class MetaDataLoaderMaterial {
    
    private final Collection<String> actualTableNames;
    
    private final String storageUnitName;
    
    private final DataSource dataSource;
    
    private final DatabaseType storageType;
    
    private final String defaultSchemaName;
    
    private final Set<String> tableNamesLoadedFromStorage;
    
    public MetaDataLoaderMaterial(final Collection<String> actualTableNames, final String storageUnitName, final DataSource dataSource,
                                  final DatabaseType storageType, final String defaultSchemaName) {
        this(actualTableNames, storageUnitName, dataSource, storageType, defaultSchemaName, toSet(actualTableNames));
    }
    
    private static Set<String> toSet(final Collection<String> tableNames) {
        Set<String> result = new LinkedHashSet<>(tableNames.size(), 1F);
        result.addAll(tableNames);
        return result;
    }
}
