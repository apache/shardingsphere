/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.spring.namespace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 数据源解析标签.
 * 
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingJdbcDataSourceBeanDefinitionParserTag {
    
    public static final String SHARDING_RULE_CONFIG_TAG = "sharding-rule";
    
    public static final String PROPS_TAG = "props";
    
    public static final String DATA_SOURCES_TAG = "data-sources";
    
    public static final String DEFAULT_DATA_SOURCE_TAG = "default-data-source";
    
    public static final String TABLE_RULES_TAG = "table-rules";
    
    public static final String TABLE_RULE_TAG = "table-rule";
    
    public static final String BINDING_TABLE_RULES_TAG = "binding-table-rules";
    
    public static final String BINDING_TABLE_RULE_TAG = "binding-table-rule";
    
    public static final String LOGIC_TABLE_ATTRIBUTE = "logic-table";
    
    public static final String LOGIC_TABLES_ATTRIBUTE = "logic-tables";
    
    public static final String DYNAMIC_TABLE_ATTRIBUTE = "dynamic";
    
    public static final String ACTUAL_TABLES_ATTRIBUTE = "actual-tables";
    
    public static final String DATA_SOURCE_NAMES_ATTRIBUTE = "data-source-names";
    
    public static final String DATABASE_STRATEGY_ATTRIBUTE = "database-strategy";
    
    public static final String TABLE_STRATEGY_ATTRIBUTE = "table-strategy";
    
    public static final String DEFAULT_DATABASE_STRATEGY_ATTRIBUTE = "default-database-strategy";
    
    public static final String DEFAULT_TABLE_STRATEGY_ATTRIBUTE = "default-table-strategy";
    
    public static final String AUTO_INCREMENT_COLUMN = "auto-increment-column";
    
    public static final String COLUMN_NAME = "column-name";
     
    public static final String COLUMN_ID_GENERATOR_CLASS = "column-id-generator-class";
    
    public static final String ID_GENERATOR_CLASS = "id-generator-class";
}
