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

package org.apache.shardingsphere.infra.distsql.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Exportable constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExportableConstants {
    
    public static final String EXPORTABLE_KEY_PRIMARY_DATA_SOURCE = "primary_data_source";
    
    public static final String EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE = "auto_aware_data_source";
    
    public static final String EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE_NAME = "auto_aware_data_source_name";
    
    public static final String EXPORTABLE_KEY_ENABLED_DATA_SOURCE = "enabled_data_source";
    
    public static final String EXPORTABLE_KEY_DATA_SOURCE = "data_source";
    
    public static final String EXPORTABLE_KEY_SINGLE_TABLES = "single_tables";
    
    public static final String PRIMARY_DATA_SOURCE_NAME = "primary_data_source_name";
    
    public static final String REPLICA_DATA_SOURCE_NAMES = "replica_data_source_names";
}
