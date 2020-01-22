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

package org.apache.shardingsphere.sql.parser.core.rule.registry;

import com.google.common.base.Optional;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.filler.SQLSegmentFiller;
import org.apache.shardingsphere.sql.parser.core.rule.jaxb.entity.extractor.ExtractorRuleDefinitionEntity;
import org.apache.shardingsphere.sql.parser.core.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import org.apache.shardingsphere.sql.parser.core.rule.jaxb.loader.RuleDefinitionFileConstant;
import org.apache.shardingsphere.sql.parser.core.rule.jaxb.loader.extractor.ExtractorRuleDefinitionEntityLoader;
import org.apache.shardingsphere.sql.parser.core.rule.jaxb.loader.filler.FillerRuleDefinitionEntityLoader;
import org.apache.shardingsphere.sql.parser.core.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import org.apache.shardingsphere.sql.parser.core.rule.registry.extractor.ExtractorRuleDefinition;
import org.apache.shardingsphere.sql.parser.core.rule.registry.filler.FillerRuleDefinition;
import org.apache.shardingsphere.sql.parser.core.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.sql.parser.core.rule.registry.statement.SQLStatementRuleDefinition;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;

import java.util.HashMap;
import java.util.Map;

/**
 * Parse rule registry.
 *
 * @author zhangliang
 * @author duhongjun
 */
public final class ParseRuleRegistry {
    
    private static volatile ParseRuleRegistry instance;
    
    private final ExtractorRuleDefinitionEntityLoader extractorRuleLoader = new ExtractorRuleDefinitionEntityLoader();
    
    private final FillerRuleDefinitionEntityLoader fillerRuleLoader = new FillerRuleDefinitionEntityLoader();
    
    private final SQLStatementRuleDefinitionEntityLoader statementRuleLoader = new SQLStatementRuleDefinitionEntityLoader();
    
    private final Map<String, FillerRuleDefinition> fillerRuleDefinitions = new HashMap<>();
    
    private final Map<String, SQLStatementRuleDefinition> sqlStatementRuleDefinitions = new HashMap<>();
    
    static {
        NewInstanceServiceLoader.register(SQLParserEntry.class);
        instance = new ParseRuleRegistry();
    }
    
    private ParseRuleRegistry() {
        initParseRuleDefinition();
    }
    
    private void initParseRuleDefinition() {
        ExtractorRuleDefinitionEntity generalExtractorRuleEntity = extractorRuleLoader.load(RuleDefinitionFileConstant.getExtractorRuleDefinitionFile());
        FillerRuleDefinitionEntity generalFillerRuleEntity = fillerRuleLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFile());
        for (SQLParserEntry each : NewInstanceServiceLoader.newServiceInstances(SQLParserEntry.class)) {
            String databaseTypeName = each.getDatabaseTypeName();
            fillerRuleDefinitions.put(databaseTypeName, createFillerRuleDefinition(generalFillerRuleEntity, databaseTypeName));
            sqlStatementRuleDefinitions.put(databaseTypeName, createSQLStatementRuleDefinition(generalExtractorRuleEntity, databaseTypeName));
        }
    }
    
    private FillerRuleDefinition createFillerRuleDefinition(final FillerRuleDefinitionEntity generalFillerRuleEntity, final String databaseTypeName) {
        FillerRuleDefinitionEntity databaseDialectFillerRuleEntity = fillerRuleLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFile(databaseTypeName));
        return new FillerRuleDefinition(generalFillerRuleEntity, databaseDialectFillerRuleEntity);
    }
    
    private SQLStatementRuleDefinition createSQLStatementRuleDefinition(final ExtractorRuleDefinitionEntity generalExtractorRuleEntity, final String databaseTypeName) {
        ExtractorRuleDefinitionEntity databaseDialectExtractorRuleEntity = extractorRuleLoader.load(RuleDefinitionFileConstant.getExtractorRuleDefinitionFile(databaseTypeName));
        ExtractorRuleDefinition extractorRuleDefinition = new ExtractorRuleDefinition(generalExtractorRuleEntity, databaseDialectExtractorRuleEntity);
        return new SQLStatementRuleDefinition(statementRuleLoader.load(RuleDefinitionFileConstant.getSQLStatementRuleDefinitionFile(databaseTypeName)), extractorRuleDefinition);
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
     * @param databaseTypeName name of database type
     * @param contextClassName context class name
     * @return SQL statement rule
     */
    public SQLStatementRule getSQLStatementRule(final String databaseTypeName, final String contextClassName) {
        return sqlStatementRuleDefinitions.get(databaseTypeName).getSQLStatementRule(contextClassName);
    }
    
    /**
     * Find SQL segment rule.
     *
     * @param databaseTypeName name of database type
     * @param sqlSegmentClass SQL segment class
     * @return SQL segment rule
     */
    public Optional<SQLSegmentFiller> findSQLSegmentFiller(final String databaseTypeName, final Class<? extends SQLSegment> sqlSegmentClass) {
        return Optional.fromNullable(fillerRuleDefinitions.get(databaseTypeName).getFiller(sqlSegmentClass));
    }
}
