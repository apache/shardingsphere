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

package org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;

/**
 * Rule definition file constant.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleDefinitionFileConstant {
    
    private static final String ROOT_PATH = "META-INF/parsing-rule-definition";
    
    private static final String SHARDING_ROOT_PATH = ROOT_PATH + "/sharding";
    
    private static final String COMMON = "common";
    
    private static final String SQL_STATEMENT_RULE_DEFINITION_FILE_NAME = "sql-statement-rule-definition.xml";
    
    private static final String EXTRACTOR_RULE_DEFINITION_FILE_NAME = "extractor-rule-definition.xml";
    
    private static final String FILLER_DEFINITION_FILE_NAME = "filler-rule-definition.xml";
    
    /**
     * Get SQL statement rule definition file name.
     * 
     * @param databaseType database type
     * @return SQL statement rule definition file name
     */
    public static String getSQLStatementRuleDefinitionFileName(final DatabaseType databaseType) {
        return Joiner.on('/').join(SHARDING_ROOT_PATH, databaseType.name().toLowerCase(), SQL_STATEMENT_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get extractor rule definition file name.
     *
     * @param databaseType database type
     * @return extractor rule definition file name
     */
    public static String getExtractorRuleDefinitionFileName(final DatabaseType databaseType) {
        return Joiner.on('/').join(SHARDING_ROOT_PATH, databaseType.name().toLowerCase(), EXTRACTOR_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get common extractor rule definition file name.
     * 
     * @return common extractor rule definition file name
     */
    public static String getCommonExtractorRuleDefinitionFileName() {
        return Joiner.on('/').join(ROOT_PATH, COMMON, EXTRACTOR_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get filler rule definition file name.
     * 
     * @return filler rule definition file name
     */
    public static String getFillerRuleDefinitionFileName() {
        return Joiner.on('/').join(ROOT_PATH, COMMON, FILLER_DEFINITION_FILE_NAME);
    }
}
