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

package org.apache.shardingsphere.core.parse.core.rule.registry;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.core.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.core.parser.SQLParserFactory;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.extractor.ExtractorRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.RuleDefinitionFileConstant;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.extractor.ExtractorRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.filler.FillerRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.core.rule.registry.extractor.ExtractorRuleDefinition;
import org.apache.shardingsphere.core.parse.core.rule.registry.filler.FillerRuleDefinition;
import org.apache.shardingsphere.core.parse.core.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parse.core.rule.registry.statement.SQLStatementRuleDefinition;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.HashMap;
import java.util.Map;

/**
 * Parse rule registry.
 *
 * @author zhangliang
 * @author duhongjun
 */
public final class ParseRuleRegistry {
    
    private static volatile ParseRuleRegistry instance = new ParseRuleRegistry();
    
    private final ExtractorRuleDefinitionEntityLoader extractorRuleLoader = new ExtractorRuleDefinitionEntityLoader();
    
    private final FillerRuleDefinitionEntityLoader fillerRuleLoader = new FillerRuleDefinitionEntityLoader();
    
    private final SQLStatementRuleDefinitionEntityLoader statementRuleLoader = new SQLStatementRuleDefinitionEntityLoader();
    
    private final Map<DatabaseType, FillerRuleDefinition> fillerRuleDefinitions = new HashMap<>();
    
    private final Map<DatabaseType, SQLStatementRuleDefinition> sqlStatementRuleDefinitions = new HashMap<>();
    
    private ParseRuleRegistry() {
        initParseRuleDefinition();
    }
    
    private void initParseRuleDefinition() {
        ExtractorRuleDefinitionEntity generalExtractorRuleEntity = extractorRuleLoader.load(RuleDefinitionFileConstant.getExtractorRuleDefinitionFile());
        FillerRuleDefinitionEntity generalFillerRuleEntity = fillerRuleLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFile());
        for (DatabaseType each : SQLParserFactory.getAddOnDatabaseTypes()) {
            fillerRuleDefinitions.put(each, createFillerRuleDefinition(generalFillerRuleEntity, each));
            sqlStatementRuleDefinitions.put(each, createSQLStatementRuleDefinition(generalExtractorRuleEntity, each));
        }
    }
    
    private FillerRuleDefinition createFillerRuleDefinition(final FillerRuleDefinitionEntity generalFillerRuleEntity, final DatabaseType databaseType) {
        FillerRuleDefinitionEntity databaseDialectFillerRuleEntity = fillerRuleLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFile(databaseType));
        return new FillerRuleDefinition(generalFillerRuleEntity, databaseDialectFillerRuleEntity);
    }
    
    private SQLStatementRuleDefinition createSQLStatementRuleDefinition(final ExtractorRuleDefinitionEntity generalExtractorRuleEntity, final DatabaseType databaseType) {
        ExtractorRuleDefinitionEntity databaseDialectExtractorRuleEntity = extractorRuleLoader.load(RuleDefinitionFileConstant.getExtractorRuleDefinitionFile(databaseType));
        ExtractorRuleDefinition extractorRuleDefinition = new ExtractorRuleDefinition(generalExtractorRuleEntity, databaseDialectExtractorRuleEntity);
        return new SQLStatementRuleDefinition(statementRuleLoader.load(RuleDefinitionFileConstant.getSQLStatementRuleDefinitionFile(databaseType)), extractorRuleDefinition);
    }
    
    /**
     * Get singleton instance of parsing rule registry.
     *
     * @return instance of parsing rule registry
     */
    public static ParseRuleRegistry getInstance() {
        return instance;
    }
    
    /**
     * Get SQL statement rule.
     *
     * @param databaseType database type
     * @param contextClassName context class name
     * @return SQL statement rule
     */
    public SQLStatementRule getSQLStatementRule(final DatabaseType databaseType, final String contextClassName) {
        return sqlStatementRuleDefinitions.get(databaseType).getSQLStatementRule(contextClassName);
    }
    
    /**
     * Find SQL segment rule.
     *
     * @param databaseType database type
     * @param sqlSegmentClass SQL segment class
     * @return SQL segment rule
     */
    public Optional<SQLSegmentFiller> findSQLSegmentFiller(final DatabaseType databaseType, final Class<? extends SQLSegment> sqlSegmentClass) {
        return Optional.fromNullable(fillerRuleDefinitions.get(databaseType).getFiller(sqlSegmentClass));
    }
}
