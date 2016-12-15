/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.dialect.oracle.ast.stmt;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OracleAlterTableSplitPartition extends OracleAlterTableItem {
    
    private SQLName name;
    
    private UpdateIndexesClause updateIndexes;
    
    private final List<SQLExpr> at = new ArrayList<>();
    
    private final List<SQLExpr> values = new ArrayList<>();
    
    private final List<NestedTablePartitionSpec> into = new ArrayList<>();
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, name);
            acceptChild(visitor, at);
            acceptChild(visitor, values);
            acceptChild(visitor, updateIndexes);
        }
        visitor.endVisit(this);
    }
    
    @Getter
    @Setter
    public static class NestedTablePartitionSpec extends OracleSQLObjectImpl {
        
        private SQLName partition;
        
        private final List<SQLObject> segmentAttributeItems = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, partition);
                acceptChild(visitor, segmentAttributeItems);
            }
            visitor.endVisit(this);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public static class TableSpaceItem extends OracleSQLObjectImpl {
        
        private final SQLName tablespace;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, tablespace);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    public static class UpdateIndexesClause extends OracleSQLObjectImpl {
        
        private final List<SQLObject> items = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, items);
            }
            visitor.endVisit(this);
        }
    }
}
