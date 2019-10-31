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

package org.apache.shardingsphere.core.parse.core.rule.jaxb.loader;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.database.DatabaseType;

/**
 * Rule definition file constant.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleDefinitionFileConstant {
    
    private static final String ROOT_PATH = "META-INF/parsing-rule-definition";
    
    private static final String EXTRACTOR_RULE_DEFINITION_FILE_NAME = "extractor-rule-definition.xml";
    
    private static final String FILLER_RULE_DEFINITION_FILE_NAME = "filler-rule-definition.xml";
    
    private static final String SQL_STATEMENT_RULE_DEFINITION_FILE_NAME = "sql-statement-rule-definition.xml";
    
    /**
     * Get general extractor rule definition file name.
     *
     * @return general extractor rule definition file name
     */
    public static String getExtractorRuleDefinitionFile() {
        return Joiner.on('/').join(ROOT_PATH, EXTRACTOR_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get extractor rule definition file name.
     *
     * @param databaseType database type
     * @return extractor rule definition file name
     */
    public static String getExtractorRuleDefinitionFile(final DatabaseType databaseType) {
        return Joiner.on('/').join(ROOT_PATH, databaseType.getName().toLowerCase(), EXTRACTOR_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get general filler rule definition file name.
     *
     * @return general filler rule definition file name
     */
    public static String getFillerRuleDefinitionFile() {
        return Joiner.on('/').join(ROOT_PATH, FILLER_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get general filler rule definition file name.
     *
     * @param featureType feature type
     * @return filler rule definition file name
     */
    public static String getFillerRuleDefinitionFile(final String featureType) {
        return Joiner.on('/').join(ROOT_PATH, featureType, FILLER_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get filler rule definition file name.
     *
     * @param databaseType database type
     * @return filler rule definition file name
     */
    public static String getFillerRuleDefinitionFile(final DatabaseType databaseType) {
        return Joiner.on('/').join(ROOT_PATH, databaseType.getName().toLowerCase(), FILLER_RULE_DEFINITION_FILE_NAME);
    }
    
    /**
     * Get SQL statement rule definition file name.
     *
     * @param databaseType database type
     * @return SQL statement rule definition file name
     */
    public static String getSQLStatementRuleDefinitionFile(final DatabaseType databaseType) {
        return Joiner.on('/').join(ROOT_PATH, databaseType.getName().toLowerCase(), SQL_STATEMENT_RULE_DEFINITION_FILE_NAME);
    }
}
