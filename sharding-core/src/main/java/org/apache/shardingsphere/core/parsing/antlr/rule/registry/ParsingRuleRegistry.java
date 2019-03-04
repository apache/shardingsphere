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

package org.apache.shardingsphere.core.parsing.antlr.rule.registry;

import java.util.HashMap;
import java.util.Map;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.RuleDefinitionFileConstant;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.extractor.ExtractorRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.filler.FillerRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.extractor.ExtractorRuleDefinition;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.filler.FillerRuleDefinition;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRuleDefinition;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.rule.SQLStatementFillerRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import com.google.common.base.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Parsing rule registry.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParsingRuleRegistry {
    
    private static volatile ParsingRuleRegistry instance;
    
    private final SQLStatementRuleDefinitionEntityLoader statementRuleDefinitionLoader = new SQLStatementRuleDefinitionEntityLoader();
    
    private final ExtractorRuleDefinitionEntityLoader extractorRuleDefinitionLoader = new ExtractorRuleDefinitionEntityLoader();
    
    private final FillerRuleDefinitionEntityLoader fillerRuleDefinitionLoader = new FillerRuleDefinitionEntityLoader();
    
    private final Map<DatabaseType, SQLStatementRuleDefinition> statementRuleDefinitions = new HashMap<>(4, 1);
    
    private final FillerRuleDefinition fillerRuleDefinition = new FillerRuleDefinition();
    
    private final Map<Class<? extends SQLStatementFillerRule>, Map<Class<? extends SQLSegment>, SQLStatementFiller>> statementFillerRuleMap = new HashMap<>();
    
    /**
     * Get singleton instance of parsing rule registry.
     *
     * @return instance of parsing rule registry
     */
    public static ParsingRuleRegistry getInstance() {
        if (null == instance) {
            synchronized (ParsingRuleRegistry.class) {
                if (null == instance) {
                    instance = new ParsingRuleRegistry();
                    instance.init();
                }
            }
        }
        return instance;
    }
    
    private synchronized void init() {
        for (DatabaseType each : DatabaseType.values()) {
            if (DatabaseType.H2 != each) {
                statementRuleDefinitions.put(each, init(each));
            }
        }
        fillerRuleDefinition.init(fillerRuleDefinitionLoader.load(RuleDefinitionFileConstant.getFillerRuleDefinitionFileName()));
        statementFillerRuleMap.put(ShardingRule.class, fillerRuleDefinition.getRules());
    }
    
    private SQLStatementRuleDefinition init(final DatabaseType databaseType) {
        ExtractorRuleDefinition extractorRuleDefinition = new ExtractorRuleDefinition();
        extractorRuleDefinition.init(
                extractorRuleDefinitionLoader.load(RuleDefinitionFileConstant.getCommonExtractorRuleDefinitionFileName()),
                extractorRuleDefinitionLoader.load(RuleDefinitionFileConstant.getExtractorRuleDefinitionFileName(databaseType)));
        SQLStatementRuleDefinition result = new SQLStatementRuleDefinition();
        result.init(statementRuleDefinitionLoader.load(RuleDefinitionFileConstant.getSQLStatementRuleDefinitionFileName(databaseType)), extractorRuleDefinition);
        return result;
    }
    
    /**
     * Find SQL statement rule.
     * 
     * @param databaseType database type
     * @param contextClassName context class name
     * @return SQL statement rule
     */
    public Optional<SQLStatementRule> findSQLStatementRule(final DatabaseType databaseType, final String contextClassName) {
        return Optional.fromNullable(statementRuleDefinitions.get(DatabaseType.H2 == databaseType ? DatabaseType.MySQL : databaseType).getRules().get(contextClassName));
    }
    
    /**
     * Find SQL statement filler.
     *
     * @param sqlSegmentClass SQL segment class
     * @return SQL statement filler
     */
    public Optional<SQLStatementFiller> findSQLStatementFiller(final Class<? extends SQLSegment> sqlSegmentClass) {
        return Optional.fromNullable(fillerRuleDefinition.getRules().get(sqlSegmentClass));
    }
    
    /**
     * Find SQL statement filler.
     *
     * @param sqlStatementFillerRuleClass SQL filler rule class
     * @param sqlSegmentClass             SQL segment class
     * @return SQL statement filler
     */
    public Optional<SQLStatementFiller> findSQLStatementFiller(final Class<? extends SQLStatementFillerRule> sqlStatementFillerRuleClass, final Class<? extends SQLSegment> sqlSegmentClass) {
        Map<Class<? extends SQLSegment>, SQLStatementFiller> sqlStatementFillerMap = statementFillerRuleMap.get(sqlStatementFillerRuleClass);
        if (null == sqlStatementFillerMap) {
            return Optional.absent();
        }
        return Optional.fromNullable(sqlStatementFillerMap.get(sqlSegmentClass));
    }
}
