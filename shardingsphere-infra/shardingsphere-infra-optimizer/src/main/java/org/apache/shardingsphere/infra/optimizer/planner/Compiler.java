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

package org.apache.shardingsphere.infra.optimizer.planner;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimizer.ExecStmt;
import org.apache.shardingsphere.infra.optimizer.converter.RelNodeConverter;
import org.apache.shardingsphere.infra.optimizer.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Optional;

public class Compiler {
    
    /**
     * compile ast to physical plan. 
     * @param schemaName schema name
     * @param shardingSphereSchema schema of logical db
     * @param sqlStatement ast
     * @return Wrapper class for Physical plan
     */
    public static ExecStmt compileQuery(final String schemaName, final ShardingSphereSchema shardingSphereSchema, final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return new ExecStmt(); 
        }
        Optional<SqlNode> convertSqlStatement = SqlNodeConverter.convertSqlStatement(sqlStatement);
        if (!convertSqlStatement.isPresent()) {
            return new ExecStmt();
        }
        SqlNode sqlNode = convertSqlStatement.get();
        RelNodeConverter relNodeConverter = new RelNodeConverter(schemaName, shardingSphereSchema);
        RelNode relNode = relNodeConverter.validateAndConvert(sqlNode);
        DefaultPlanner defaultPlanner = new DefaultPlanner();
        RelNode finalPlan = defaultPlanner.getPhysicPlan(relNode);
        return new ExecStmt(sqlNode, finalPlan, null);
    }
}
