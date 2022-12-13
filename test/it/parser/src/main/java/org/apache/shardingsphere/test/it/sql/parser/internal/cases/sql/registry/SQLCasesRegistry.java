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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.registry;

import lombok.Getter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.SQLCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.loader.SQLCaseLoaderCallback;
import org.apache.shardingsphere.test.it.sql.parser.internal.loader.CaseLoaderTemplate;

/**
 * SQL cases registry.
 */
@Getter
public final class SQLCasesRegistry {
    
    private static final SQLCasesRegistry INSTANCE = new SQLCasesRegistry();
    
    private final SQLCases cases;
    
    private SQLCasesRegistry() {
        cases = new SQLCases(CaseLoaderTemplate.load("sql/supported/", new SQLCaseLoaderCallback()));
    }
    
    /**
     * Get instance.
     * 
     * @return got instance
     */
    public static SQLCasesRegistry getInstance() {
        return INSTANCE;
    }
}
