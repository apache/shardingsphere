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

package org.apache.shardingsphere.core.parse.rule.registry;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.rule.jaxb.entity.extractor.ExtractorRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.rule.jaxb.loader.RuleDefinitionFileConstant;
import org.apache.shardingsphere.core.parse.rule.jaxb.loader.extractor.ExtractorRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.rule.jaxb.loader.filler.FillerRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parse.rule.registry.extractor.ExtractorRuleDefinition;
import org.apache.shardingsphere.core.parse.rule.registry.filler.FillerRuleDefinition;
import org.apache.shardingsphere.core.parse.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parse.rule.registry.statement.SQLStatementRuleDefinition;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Parse rule registry.
 *
 * @author zhangliang
 * @author duhongjun
 */
public abstract class ParseRuleRegistry {
    
    private final ExtractorRuleDefinitionEntityLoader extractorRuleDefinitionLoader = new ExtractorRuleDefinitionEntityLoader();
    
    private final FillerRuleDefinitionEntityLoader fillerRuleDefinitionLoader = new FillerRuleDefinitionEntityLoader();
    
    private final SQLStatementRuleDefinitionEntityLoader statementRuleDefinitionLoader = new SQLStatementRuleDefinitionEntityLoader();
    
    private final Map<DatabaseType, ExtractorRuleDefinition> extractorRuleDefinitions = new HashMap<>();
    
    private final Map<DatabaseType, FillerRuleDefinition> fillerRuleDefinitions = new HashMap<>();
    
    private final Map<DatabaseType, SQLStatementRuleDefinition> sqlStatementRuleDefinitions = new HashMap<>();
    
    public ParseRuleRegistry() {
        initParseRuleDefinition();
    }
    
    private void initParseRuleDefinition() {
        ExtractorRuleDefinitionEntity generalExtractorRuleDefinitionEntity = extractorRuleDefinitionLoader.load(RuleDefinitionFileConstant.getGeneralExtractorRuleDefinitionFileName());
        FillerRuleDefinitionEntity generalFillerRuleDefinitionEntity = fillerRuleDefinitionLoader.load(RuleDefinitionFileConstant.getGeneralFillerRuleDefinitionFileName());
        for (DatabaseType each : DatabaseType.values()) {
            if (DatabaseType.H2 != each) {
                extractorRuleDefinitions.put(each, new ExtractorRuleDefinition(generalExtractorRuleDefinitionEntity, extractorRuleDefinitionLoader.load(getExtractorFile(each))));
                initFillerRuleDefinition(generalFillerRuleDefinitionEntity, each);
                initSQLStatementRuleDefinition(each);
            }
        }
    }
    
    private void initFillerRuleDefinition(final FillerRuleDefinitionEntity generalFillerRuleDefinitionEntity, final DatabaseType databaseType) {
        List<FillerRuleDefinitionEntity> entities = new LinkedList<>();
        entities.add(generalFillerRuleDefinitionEntity);
        for (String each : getFillerFiles(databaseType)) {
            entities.add(fillerRuleDefinitionLoader.load(each));
        }
        fillerRuleDefinitions.put(databaseType, new FillerRuleDefinition(entities.toArray(new FillerRuleDefinitionEntity[entities.size()])));
    }
    
    private void initSQLStatementRuleDefinition(final DatabaseType databaseType) {
        SQLStatementRuleDefinition sqlStatementRuleDefinition = new SQLStatementRuleDefinition();
        sqlStatementRuleDefinition.init(statementRuleDefinitionLoader.load(getStatementRuleFile(databaseType)), extractorRuleDefinitions.get(databaseType));
        sqlStatementRuleDefinitions.put(databaseType, sqlStatementRuleDefinition);
    }
    
    protected abstract String getExtractorFile(DatabaseType databaseType);
    
    protected abstract Collection<String> getFillerFiles(DatabaseType databaseType);
    
    protected abstract String getStatementRuleFile(DatabaseType databaseType);
    
    /**
     * Find SQL statement rule.
     *
     * @param databaseType database type
     * @param contextClassName context class name
     * @return SQL statement rule
     */
    public Optional<SQLStatementRule> findSQLStatementRule(final DatabaseType databaseType, final String contextClassName) {
        return Optional.fromNullable(sqlStatementRuleDefinitions.get(DatabaseType.H2 == databaseType ? DatabaseType.MySQL : databaseType).getRules().get(contextClassName));
    }
    
    /**
     * Find SQL segment rule.
     *
     * @param databaseType database type
     * @param sqlSegmentClass SQL segment class
     * @return SQL segment rule
     */
    public Optional<SQLSegmentFiller> findSQLSegmentFiller(final DatabaseType databaseType, final Class<? extends SQLSegment> sqlSegmentClass) {
        return Optional.fromNullable(fillerRuleDefinitions.get(DatabaseType.H2 == databaseType ? DatabaseType.MySQL : databaseType).getRules().get(sqlSegmentClass));
    }
}
