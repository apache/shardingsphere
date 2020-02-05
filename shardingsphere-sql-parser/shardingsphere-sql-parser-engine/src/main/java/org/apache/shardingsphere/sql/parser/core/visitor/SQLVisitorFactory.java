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
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.api.SQLVisitor;
import org.apache.shardingsphere.sql.parser.core.extractor.util.RuleName;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL visitor factory.
 * 
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLVisitorFactory {
    
    private static final Map<String, Collection<String>> SQL_VISITOR_RULES = new LinkedHashMap<>();
    
    static {
        SQL_VISITOR_RULES.put("DMLVisitor", 
                Lists.newArrayList(RuleName.SELECT.getName(), RuleName.DELETE.getName(), RuleName.UPDATE.getName(), RuleName.INSERT.getName()));
    }
    
    
    /** 
     * New instance of SQL visitor.
     * 
     * @param databaseTypeName name of database type
     * @param visitorRuleName visitor rule name
     * @return SQL visitor
     */
    public static SQLVisitor newInstance(final String databaseTypeName, final String visitorRuleName) {
        for (SQLParserEntry each : NewInstanceServiceLoader.newServiceInstances(SQLParserEntry.class)) {
            if (each.getDatabaseTypeName().equals(databaseTypeName)) {
                return createSQLVisitor(each, getVisitorName(visitorRuleName));
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseTypeName));
    }
    
    private static String getVisitorName(final String visitorRuleName) {
        for (Entry<String, Collection<String>> entry : SQL_VISITOR_RULES.entrySet()) {
            if (entry.getValue().contains(visitorRuleName)) {
                return entry.getKey();
            }
        }
        return "MySQLVisitor";
//        throw new SQLParsingException("Could not find corresponding SQL visitor for %s.", visitorRuleName);
    }
    
    @SneakyThrows
    private static SQLVisitor createSQLVisitor(final SQLParserEntry parserEntry, final String visitorName) {
        return parserEntry.getVisitorClass(visitorName).getConstructor().newInstance();
    }
}
