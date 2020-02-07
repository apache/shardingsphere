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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.extractor.util.RuleName;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Parse tree visitor factory.
 * 
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParseTreeVisitorFactory {
    
    private static final Map<String, Collection<String>> SQL_VISITOR_RULES = new LinkedHashMap<>();
    
    static {
        SQL_VISITOR_RULES.put("DMLVisitor", Lists.newArrayList(RuleName.SELECT.getName(), 
                RuleName.DELETE.getName(), RuleName.UPDATE.getName(), RuleName.INSERT.getName(), RuleName.REPLACE.getName()));
        SQL_VISITOR_RULES.put("DDLVisitor", Lists.newArrayList(RuleName.CREATE_TABLE.getName(), RuleName.ALTER_TABLE.getName(), 
                RuleName.DROP_TABLE.getName(), RuleName.TRUNCATE_TABLE.getName(), RuleName.CREATE_INDEX.getName(), RuleName.DROP_INDEX.getName()));
        SQL_VISITOR_RULES.put("TCLVisitor", Lists.newArrayList(RuleName.SET_TRANSACTION.getName(), RuleName.BEGIN_TRANSACTION.getName(), 
                RuleName.SET_AUTOCOMMIT.getName(), RuleName.COMMIT.getName(), RuleName.ROLLBACK.getName(), RuleName.SAVE_POINT.getName()));
        SQL_VISITOR_RULES.put("DCLVisitor", Lists.newArrayList(RuleName.GRANT.getName(), RuleName.REVOKE.getName(), RuleName.CREATE_USER.getName(), 
                RuleName.DROP_USER.getName(), RuleName.ALTER_USER.getName(), RuleName.RENAME_USER.getName(), RuleName.CREATE_ROLE.getName(), 
                RuleName.DROP_ROLE.getName(), RuleName.SET_DEFAULT_ROLE.getName(), RuleName.SET_ROLE.getName(), RuleName.SET_PASSWORD.getName()));
        SQL_VISITOR_RULES.put("DALVisitor", Lists.newArrayList(RuleName.USE.getName(), RuleName.DESC.getName(), RuleName.SHOW_DATABASES.getName(),
                RuleName.SHOW_TABLES.getName(), RuleName.SHOW_TABLE_STATUS.getName(), RuleName.SHOW_COLUMNS.getName(), RuleName.SHOW_INDEX.getName(),
                RuleName.SHOW_CREATE_TABLE.getName(), RuleName.SHOW_OTHER.getName(), RuleName.SET_VARIABLE.getName(), RuleName.CALL.getName()));
    }
    
    /** 
     * New instance of SQL visitor.
     * 
     * @param databaseTypeName name of database type
     * @param parseTree parse tree
     * @return parse tree visitor
     */
    public static ParseTreeVisitor newInstance(final String databaseTypeName, final ParseTree parseTree) {
        for (SQLParserEntry each : NewInstanceServiceLoader.newServiceInstances(SQLParserEntry.class)) {
            if (each.getDatabaseTypeName().equals(databaseTypeName)) {
                return createParseTreeVisitor(each, getVisitorName(parseTree.getClass().getSimpleName()));
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseTypeName));
    }
    
    @SneakyThrows
    private static ParseTreeVisitor createParseTreeVisitor(final SQLParserEntry parserEntry, final String visitorName) {
        return parserEntry.getVisitorClass(visitorName).getConstructor().newInstance();
    }
    
    private static String getVisitorName(final String visitorRuleName) {
        for (Entry<String, Collection<String>> entry : SQL_VISITOR_RULES.entrySet()) {
            if (entry.getValue().contains(visitorRuleName)) {
                return entry.getKey();
            }
        }
        throw new SQLParsingException("Could not find corresponding SQL visitor for %s.", visitorRuleName);
    }
}
