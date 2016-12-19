/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */
package com.alibaba.druid.sql.dialect.mysql.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * MySQL关键词.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySqlKeyword {

    static final String AUTO_INCREMENT = "AUTO_INCREMENT";
    
    static final String COLLATE2       = "COLLATE";
    
    static final String CHAIN          = "CHAIN";
    
    static final String ENGINES        = "ENGINES";
    
    static final String ENGINE         = "ENGINE";
    
    static final String BINLOG         = "BINLOG";
    
    static final String EVENTS         = "EVENTS";
    
    static final String CHARACTER      = "CHARACTER";
    
    static final String SESSION        = "SESSION";
    
    static final String GLOBAL         = "GLOBAL";
    
    static final String VARIABLES      = "VARIABLES";
    
    static final String ERRORS         = "ERRORS";
    
    static final String STATUS         = "STATUS";
    
    static final String IGNORE         = "IGNORE";
    
    static final String RESET          = "RESET";
    
    static final String DESCRIBE       = "DESCRIBE";
    
    static final String WRITE          = "WRITE";
    
    static final String READ           = "READ";
    
    static final String LOCAL          = "LOCAL";
    
    static final String TABLES         = "TABLES";
    
    static final String TEMPORARY      = "TEMPORARY";
    
    static final String SPATIAL        = "SPATIAL";
    
    static final String FULLTEXT       = "FULLTEXT";
    
    static final String LOW_PRIORITY   = "LOW_PRIORITY";
    
    static final String DELAYED        = "DELAYED";
    
    static final String HIGH_PRIORITY   = "HIGH_PRIORITY";
}
