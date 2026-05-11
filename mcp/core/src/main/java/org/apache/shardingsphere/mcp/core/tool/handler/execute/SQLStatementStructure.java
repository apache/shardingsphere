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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import java.util.Collection;

final class SQLStatementStructure {
    
    private final String mainSql;
    
    private final String statementType;
    
    private final boolean containsDataModifyingCommonTableExpression;
    
    private final Collection<SQLCommonTableExpression> commonTableExpressions;
    
    SQLStatementStructure(final String mainSql, final String statementType, final boolean containsDataModifyingCommonTableExpression,
                          final Collection<SQLCommonTableExpression> commonTableExpressions) {
        this.mainSql = mainSql;
        this.statementType = statementType;
        this.containsDataModifyingCommonTableExpression = containsDataModifyingCommonTableExpression;
        this.commonTableExpressions = commonTableExpressions;
    }
    
    String mainSql() {
        return mainSql;
    }
    
    String statementType() {
        return statementType;
    }
    
    boolean containsDataModifyingCommonTableExpression() {
        return containsDataModifyingCommonTableExpression;
    }
    
    Collection<SQLCommonTableExpression> commonTableExpressions() {
        return commonTableExpressions;
    }
}
