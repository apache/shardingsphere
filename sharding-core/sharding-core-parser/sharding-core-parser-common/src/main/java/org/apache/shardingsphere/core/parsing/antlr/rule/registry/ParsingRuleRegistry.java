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

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.RuleDefinitionFileConstant;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.extractor.ExtractorRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.filler.FillerRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import org.apache.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Parsing rule registry.
 *
 * @author zhangliang
 * @author duhongjun
 */
public abstract class ParsingRuleRegistry {
    
    private final SQLStatementRuleDefinitionEntityLoader statementRuleDefinitionLoader = new SQLStatementRuleDefinitionEntityLoader();
    
    private final ExtractorRuleDefinitionEntityLoader extractorRuleDefinitionLoader = new ExtractorRuleDefinitionEntityLoader();
    
    private final FillerRuleDefinitionEntityLoader fillerRuleDefinitionLoader = new FillerRuleDefinitionEntityLoader();
    
    private final ParserRuleDefinition commonRuleDefinition = new ParserRuleDefinition();
    
    private final Map<DatabaseType, ParserRuleDefinition> parserRuleDefinitions = new HashMap<>(4, 1);
    
    protected void init() {
        initCommonParserRuleDefinition();
        initParserRuleDefinition();
    }
    
    private void initCommonParserRuleDefinition() {
        List<String> fillerFilePaths = Arrays.asList(new String[]{RuleDefinitionFileConstant.getCommonFillerRuleDefinitionFileName()});
        List<String> extractorFilePaths = Arrays.asList(new String[]{RuleDefinitionFileConstant.getCommonExtractorRuleDefinitionFileName()});
        initParserRuleDefinition(commonRuleDefinition, fillerFilePaths, extractorFilePaths, new ArrayList<String>());
    }
    
    private void initParserRuleDefinition() {
        for (DatabaseType each : DatabaseType.values()) {
            if (DatabaseType.H2 != each) {
                if (!needParser(each)) {
                    continue;
                }
                List<String> fillerFilePaths = new LinkedList<>();
                List<String> extractorFilePaths = new LinkedList<>();
                List<String> sqlStateRuleFilePaths = new LinkedList<>();
                fillRuleFilePaths(each, fillerFilePaths, extractorFilePaths, sqlStateRuleFilePaths);
                ParserRuleDefinition shardingRuleDefinition = new ParserRuleDefinition();
                initParserRuleDefinitionFromCommon(shardingRuleDefinition, fillerFilePaths, extractorFilePaths, sqlStateRuleFilePaths);
                parserRuleDefinitions.put(each, shardingRuleDefinition);
            }
        }
    }
    
    protected boolean needParser(final DatabaseType databaseType) { 
        return true;
    }
    
    protected abstract void fillRuleFilePaths(DatabaseType databaseType, List<String> fillerFilePaths, List<String> extractorFilePaths, List<String> sqlStateRuleFilePaths);
    
    private void initParserRuleDefinitionFromCommon(final ParserRuleDefinition parserRuleDefinition, final List<String> fillerFilePaths, final List<String> extractorFilePaths,
                                                    final List<String> sqlStateRuleFilePaths) {
        parserRuleDefinition.getExtractorRuleDefinition().getRules().putAll(commonRuleDefinition.getExtractorRuleDefinition().getRules());
        parserRuleDefinition.getFillerRuleDefinition().getRules().putAll(commonRuleDefinition.getFillerRuleDefinition().getRules());
        initParserRuleDefinition(parserRuleDefinition, fillerFilePaths, extractorFilePaths, sqlStateRuleFilePaths);
    }
    
    private void initParserRuleDefinition(final ParserRuleDefinition parserRuleDefinition, final List<String> fillerFilePaths, final List<String> extractorFilePaths,
                                          final List<String> sqlStateRuleFilePaths) {
        for (String each : fillerFilePaths) {
            parserRuleDefinition.getFillerRuleDefinition().init(fillerRuleDefinitionLoader.load(each));
        }
        for (String each : extractorFilePaths) {
            parserRuleDefinition.getExtractorRuleDefinition().init(extractorRuleDefinitionLoader.load(each));
        }
        for (String each : sqlStateRuleFilePaths) {
            parserRuleDefinition.getSqlStatementRuleDefinition().init(statementRuleDefinitionLoader.load(each), parserRuleDefinition.getExtractorRuleDefinition());
        }
    }
    
    /**
     * Find SQL statement rule.
     *
     * @param databaseType     database type
     * @param contextClassName context class name
     * @return SQL statement rule
     */
    public Optional<SQLStatementRule> findSQLStatementRule(final DatabaseType databaseType, final String contextClassName) {
        return Optional.fromNullable(parserRuleDefinitions.get(DatabaseType.H2 == databaseType ? DatabaseType.MySQL : databaseType).getSqlStatementRuleDefinition().getRules().get(contextClassName));
    }
    
    /**
     * Find SQL segment rule.
     *
     * @param databaseType database type
     * @param sqlSegmentClass SQL segment class
     * @return SQL segment rule
     */
    public Optional<SQLSegmentFiller> findSQLSegmentFiller(final DatabaseType databaseType, final Class<? extends SQLSegment> sqlSegmentClass) {
        return Optional.fromNullable(parserRuleDefinitions.get(DatabaseType.H2 == databaseType ? DatabaseType.MySQL : databaseType).getFillerRuleDefinition().getRules().get(sqlSegmentClass));
    }
}
