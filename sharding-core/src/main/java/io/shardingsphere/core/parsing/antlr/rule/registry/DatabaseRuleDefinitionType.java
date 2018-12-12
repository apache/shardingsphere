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

import io.shardingsphere.core.constant.DatabaseType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Database rule definition type.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum DatabaseRuleDefinitionType {
    
    MySQL(DatabaseType.MySQL, "parsing-rule-definition/mysql/sql-statement-rule-definition.xml", "parsing-rule-definition/mysql/extractor-rule-definition.xml"), 
    PostgreSQL(DatabaseType.PostgreSQL, "parsing-rule-definition/postgresql/sql-statement-rule-definition.xml", "parsing-rule-definition/postgresql/extractor-rule-definition.xml"), 
    Oracle(DatabaseType.Oracle, "parsing-rule-definition/oracle/sql-statement-rule-definition.xml", "parsing-rule-definition/oracle/extractor-rule-definition.xml"),  
    SQLServer(DatabaseType.SQLServer, "parsing-rule-definition/sqlserver/sql-statement-rule-definition.xml", "parsing-rule-definition/sqlserver/extractor-rule-definition.xml");
    
    public static final String COMMON_SQL_SEGMENT_RULE_DEFINITION = "parsing-rule-definition/common/extractor-rule-definition.xml";
    
    public static final String COMMON_FILLER_RULE_DEFINITION = "parsing-rule-definition/common/filler-rule-definition.xml";
    
    private final DatabaseType databaseType;
    
    private final String sqlStatementRuleDefinitionFile;
    
    private final String extractorRuleDefinitionFile;
    
    /**
     * Value of database rule definition type via database type.
     * 
     * @param databaseType database type
     * @return database rule definition type
     */
    public static DatabaseRuleDefinitionType valueOf(final DatabaseType databaseType) {
        for (DatabaseRuleDefinitionType each : DatabaseRuleDefinitionType.values()) {
            if (each.getDatabaseType() == databaseType) {
                return each;
            }
        }
        throw new IllegalArgumentException(databaseType.name());
    }
}
