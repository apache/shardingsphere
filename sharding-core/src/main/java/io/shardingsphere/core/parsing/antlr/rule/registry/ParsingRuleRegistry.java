/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.rule.registry;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.filler.FillerRuleDefinitionEntityLoader;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.segment.SQLSegmentRuleDefinitionEntityLoader;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import io.shardingsphere.core.parsing.antlr.rule.registry.filler.FillerRuleDefinition;
import io.shardingsphere.core.parsing.antlr.rule.registry.segment.SQLSegmentRuleDefinition;
import io.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRule;
import io.shardingsphere.core.parsing.antlr.rule.registry.statement.SQLStatementRuleDefinition;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Parsing rule registry.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParsingRuleRegistry {
    
    private static volatile ParsingRuleRegistry instance;
    
    private final SQLStatementRuleDefinitionEntityLoader statementRuleDefinitionLoader = new SQLStatementRuleDefinitionEntityLoader();
    
    private final SQLSegmentRuleDefinitionEntityLoader segmentRuleDefinitionLoader = new SQLSegmentRuleDefinitionEntityLoader();
    
    private final FillerRuleDefinitionEntityLoader fillerRuleDefinitionLoader = new FillerRuleDefinitionEntityLoader();
    
    private final Map<DatabaseType, SQLStatementRuleDefinition> statementRuleDefinitions = new HashMap<>(4, 1);
    
    private final FillerRuleDefinition fillerRuleDefinition = new FillerRuleDefinition();
    
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
                statementRuleDefinitions.put(each, init(DatabaseRuleDefinitionType.valueOf(each)));
            }
        }
        fillerRuleDefinition.init(fillerRuleDefinitionLoader.load(DatabaseRuleDefinitionType.COMMON_FILLER_RULE_DEFINITION));
    }
    
    private SQLStatementRuleDefinition init(final DatabaseRuleDefinitionType type) {
        SQLSegmentRuleDefinition segmentRuleDefinition = new SQLSegmentRuleDefinition();
        segmentRuleDefinition.init(
                segmentRuleDefinitionLoader.load(DatabaseRuleDefinitionType.COMMON_SQL_SEGMENT_RULE_DEFINITION), segmentRuleDefinitionLoader.load(type.getSqlSegmentRuleDefinitionFile()));
        SQLStatementRuleDefinition result = new SQLStatementRuleDefinition();
        result.init(statementRuleDefinitionLoader.load(type.getSqlStatementRuleDefinitionFile()), segmentRuleDefinition);
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
}
