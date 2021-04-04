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

package org.apache.shardingsphere.infra.optimizer.sql;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.util.SqlShuttle;

public abstract class ExtendedSqlShuttle extends SqlShuttle {
    
    /**
     * Visit {@link SqlCall}.
     * @param call sqlCall
     * @return result of sqlCall
     */
    @Override
    public SqlNode visit(final SqlCall call) {
        if (call.getKind() == SqlKind.SELECT) {
            return visit((SqlSelect) call);
        } else if (call.getKind() == SqlKind.JOIN) {
            return visit((SqlJoin) call);
        } else if (call.getKind() == SqlKind.ORDER_BY) {
            return visit((SqlOrderBy) call);
        }
        
        return super.visit(call);
    }
    
    abstract SqlSelect visit(SqlSelect sqlSelect);
    
    abstract SqlJoin visit(SqlJoin sqlJoin);
    
    abstract SqlOrderBy visit(SqlOrderBy sqlOrderBy);
}
