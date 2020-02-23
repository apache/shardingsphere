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

package org.apache.shardingsphere.sql.parser.core.visitor;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.constant.RuleName;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Parse tree visitor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParseTreeVisitorFactory {
    
    private static final Map<String, Collection<RuleName>> SQL_VISITOR_RULES = new LinkedHashMap<>();
    
    static {
        SQL_VISITOR_RULES.put("DMLVisitor", Lists.newArrayList(RuleName.SELECT, RuleName.DELETE, RuleName.UPDATE, RuleName.INSERT, RuleName.REPLACE));
        SQL_VISITOR_RULES.put("DDLVisitor", Lists.newArrayList(RuleName.CREATE_TABLE, RuleName.ALTER_TABLE, 
                RuleName.DROP_TABLE, RuleName.TRUNCATE_TABLE, RuleName.CREATE_INDEX, RuleName.ALTER_INDEX, RuleName.DROP_INDEX));
        SQL_VISITOR_RULES.put("TCLVisitor", Lists.newArrayList(RuleName.SET_TRANSACTION, RuleName.SET_IMPLICIT_TRANSACTIONS, RuleName.BEGIN_TRANSACTION, 
                RuleName.SET_AUTOCOMMIT, RuleName.COMMIT, RuleName.ROLLBACK, RuleName.SAVE_POINT));
        SQL_VISITOR_RULES.put("DCLVisitor", Lists.newArrayList(RuleName.GRANT, RuleName.REVOKE, RuleName.CREATE_USER, 
                RuleName.DROP_USER, RuleName.ALTER_USER, RuleName.DENY_USER, RuleName.RENAME_USER, 
                RuleName.CREATE_ROLE, RuleName.ALTER_ROLE, RuleName.DROP_ROLE, RuleName.CREATE_LOGIN, RuleName.ALTER_LOGIN,
                RuleName.DROP_LOGIN, RuleName.SET_DEFAULT_ROLE, RuleName.SET_ROLE, RuleName.SET_PASSWORD));
        SQL_VISITOR_RULES.put("DALVisitor", Lists.newArrayList(RuleName.USE, RuleName.DESC, RuleName.SHOW_DATABASES,
                RuleName.SHOW_TABLES, RuleName.SHOW_TABLE_STATUS, RuleName.SHOW_COLUMNS, RuleName.SHOW_INDEX,
                RuleName.SHOW_CREATE_TABLE, RuleName.SHOW_OTHER, RuleName.SHOW, RuleName.SET_VARIABLE, RuleName.SET, 
                RuleName.RESET_PARAMETER, RuleName.CALL));
    }
    
    /** 
     * New instance of SQL visitor.
     * 
     * @param databaseTypeName name of database type
     * @param ruleName rule name
     * @return parse tree visitor
     */
    public static ParseTreeVisitor newInstance(final String databaseTypeName, final RuleName ruleName) {
        for (SQLParserConfiguration each : NewInstanceServiceLoader.newServiceInstances(SQLParserConfiguration.class)) {
            if (each.getDatabaseTypeName().equals(databaseTypeName)) {
                return createParseTreeVisitor(each, getVisitorName(ruleName));
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseTypeName));
    }
    
    @SneakyThrows
    private static ParseTreeVisitor createParseTreeVisitor(final SQLParserConfiguration configuration, final String visitorName) {
        return configuration.getVisitorClass(visitorName).getConstructor().newInstance();
    }
    
    private static String getVisitorName(final RuleName ruleName) {
        for (Entry<String, Collection<RuleName>> entry : SQL_VISITOR_RULES.entrySet()) {
            if (entry.getValue().contains(ruleName)) {
                return entry.getKey();
            }
        }
        throw new SQLParsingException("Could not find corresponding SQL visitor for %s.", ruleName);
    }
}
