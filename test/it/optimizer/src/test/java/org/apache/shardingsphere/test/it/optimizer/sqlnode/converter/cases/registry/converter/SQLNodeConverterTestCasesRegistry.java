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

package org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.registry.converter;

import lombok.Getter;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.SQLNodeConverterTestCaseLoaderCallback;
import org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.SQLNodeConverterTestCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.loader.CaseLoaderTemplate;

import java.io.File;

/**
 * SQL node converter test cases registry.
 */
@Getter
public final class SQLNodeConverterTestCasesRegistry {
    
    private static final SQLNodeConverterTestCasesRegistry INSTANCE = new SQLNodeConverterTestCasesRegistry();
    
    private final SQLNodeConverterTestCases cases;
    
    private SQLNodeConverterTestCasesRegistry() {
        File file = new File(SQLNodeConverterTestCasesRegistry.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        cases = new SQLNodeConverterTestCases(CaseLoaderTemplate.load(file, "converted-sql/", new SQLNodeConverterTestCaseLoaderCallback()));
    }
    
    /**
     * Get instance.
     *
     * @return got instance
     */
    public static SQLNodeConverterTestCasesRegistry getInstance() {
        return INSTANCE;
    }
}
