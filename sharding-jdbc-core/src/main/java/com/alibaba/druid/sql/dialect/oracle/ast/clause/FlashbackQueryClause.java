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
package com.alibaba.druid.sql.dialect.oracle.ast.clause;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public abstract class FlashbackQueryClause extends OracleSQLObjectImpl {
    
    private Type type;
    
    public enum Type {
        SCN, TIMESTAMP
    }
    
    @Getter
    @Setter
    public static class VersionsFlashbackQueryClause extends FlashbackQueryClause {
        
        private SQLExpr begin;
        
        private SQLExpr end;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, begin);
                acceptChild(visitor, end);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class AsOfFlashbackQueryClause extends FlashbackQueryClause {
        
        private SQLExpr expr;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, expr);
            }
            visitor.endVisit(this);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public static class AsOfSnapshotClause extends FlashbackQueryClause {
        
        private final SQLExpr expr;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, expr);
            }
            visitor.endVisit(this);
        }
    }
}
