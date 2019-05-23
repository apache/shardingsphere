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

import java.util.HashMap;
import java.util.Map;

/**
 * Parse rule registry.
 *
 * @author zhangliang
 * @author duhongjun
 */
public abstract class ParseRuleRegistry {
    
    private final ExtractorRuleDefinitionEntityLoader extractorRuleLoader = new ExtractorRuleDefinitionEntityLoader();
    
    private final FillerRuleDefinitionEntityLoader fillerRuleLoader = new FillerRuleDefinitionEntityLoader();
    
    private final SQLStatementRuleDefinitionEntityLoader statementRuleLoader = new SQLStatementRuleDefinitionEntityLoader();
    
    private final Map<DatabaseType, FillerRuleDefinition> fillerRuleDefinitions = new HashMap<>();
    
    private final Map<DatabaseType, SQLStatementRuleDefinition> sqlStatementRuleDefinitions = new HashMap<>();
    
    public ParseRuleRegistry() {
        initParseRuleDefinition();
    }
    
    private void initParseRuleDefinition() {
        ExtractorRuleDefinitionEntity generalExtractorRuleEntity = extractorRuleLoader.load(RuleDefinitionFileConstant.getExtractorRuleDefinitionFile());
        FillerRuleDefinitionEntity generalFillerRuleEntity = fillerRuleLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFile());
        FillerRuleDefinitionEntity featureGeneralFillerRuleEntity = fillerRuleLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFile(getType()));
        for (DatabaseType each : DatabaseType.values()) {
            if (DatabaseType.H2 != each) {
                fillerRuleDefinitions.put(each, createFillerRuleDefinition(generalFillerRuleEntity, featureGeneralFillerRuleEntity, each));
                sqlStatementRuleDefinitions.put(each, createSQLStatementRuleDefinition(generalExtractorRuleEntity, each));
            }
        }
    }
    
    private FillerRuleDefinition createFillerRuleDefinition(final FillerRuleDefinitionEntity generalFillerRuleEntity,
                                                            final FillerRuleDefinitionEntity featureGeneralFillerRuleEntity, final DatabaseType databaseType) {
        return new FillerRuleDefinition(
                generalFillerRuleEntity, featureGeneralFillerRuleEntity, fillerRuleLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFile(getType(), databaseType)));
    }
    
    private SQLStatementRuleDefinition createSQLStatementRuleDefinition(final ExtractorRuleDefinitionEntity generalExtractorRuleEntity, final DatabaseType databaseType) {
        ExtractorRuleDefinition extractorRuleDefinition = new ExtractorRuleDefinition(
                generalExtractorRuleEntity, extractorRuleLoader.load(RuleDefinitionFileConstant.getExtractorRuleDefinitionFile(getType(), databaseType)));
        return new SQLStatementRuleDefinition(statementRuleLoader.load(RuleDefinitionFileConstant.getSQLStatementRuleDefinitionFile(getType(), databaseType)), extractorRuleDefinition);
    }
    
    /**
     * Get feature type.
     * 
     * @return feature type
     */
    protected abstract String getType();
    
    /**
     * Get SQL statement rule.
     *
     * @param databaseType database type
     * @param contextClassName context class name
     * @return SQL statement rule
     */
    public SQLStatementRule getSQLStatementRule(final DatabaseType databaseType, final String contextClassName) {
        return sqlStatementRuleDefinitions.get(DatabaseType.H2 == databaseType ? DatabaseType.MySQL : databaseType).getSQLStatementRule(contextClassName);
    }
    
    /**
     * Find SQL segment rule.
     *
     * @param databaseType database type
     * @param sqlSegmentClass SQL segment class
     * @return SQL segment rule
     */
    public Optional<SQLSegmentFiller> findSQLSegmentFiller(final DatabaseType databaseType, final Class<? extends SQLSegment> sqlSegmentClass) {
        return Optional.fromNullable(fillerRuleDefinitions.get(DatabaseType.H2 == databaseType ? DatabaseType.MySQL : databaseType).getFiller(sqlSegmentClass));
    }
}
