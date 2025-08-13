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

package org.apache.shardingsphere.test.it.sql.binder.cases.registry.binder;

import lombok.Getter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.SQLParserTestCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.loader.SQLParserTestCaseLoaderCallback;
import org.apache.shardingsphere.test.it.sql.parser.internal.loader.CaseLoaderTemplate;

import java.io.File;

/**
 * SQL binder test cases registry.
 */
@Getter
public final class SQLBinderTestCasesRegistry {
    
    private static final SQLBinderTestCasesRegistry INSTANCE = new SQLBinderTestCasesRegistry();
    
    private final SQLParserTestCases cases;
    
    private SQLBinderTestCasesRegistry() {
        File file = new File(SQLBinderTestCasesRegistry.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        cases = new SQLParserTestCases(CaseLoaderTemplate.load(file, "cases/", new SQLParserTestCaseLoaderCallback()));
    }
    
    /**
     * Get instance.
     *
     * @return got instance
     */
    public static SQLBinderTestCasesRegistry getInstance() {
        return INSTANCE;
    }
}
