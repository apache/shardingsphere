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
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleSQLObjectImpl;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ModelClause extends OracleSQLObjectImpl {
    
    private ReturnRowsClause returnRowsClause;
    
    private MainModelClause mainModel;
    
    private final List<CellReferenceOption> cellReferenceOptions  = new ArrayList<>();
    
    private final List<ReferenceModelClause> referenceModelClauses = new ArrayList<>();
    
    @Override
    public void accept0(final OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, returnRowsClause);
            acceptChild(visitor, referenceModelClauses);
            acceptChild(visitor, mainModel);
        }
        visitor.endVisit(this);
    }
    
    @RequiredArgsConstructor
    @Getter
    public enum CellReferenceOption {
        
        IgnoreNav("IGNORE NAV"), 
        KeepNav("KEEP NAV"), 
        UniqueDimension("UNIQUE DIMENSION"),
        UniqueSingleReference("UNIQUE SINGLE REFERENCE");
        
        private final String text;
    }
    
    @Getter
    @Setter
    public static class ReturnRowsClause extends OracleSQLObjectImpl {
        
        private boolean all;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            visitor.visit(this);
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class ReferenceModelClause extends OracleSQLObjectImpl {
        
        private SQLExpr name;
        
        private SQLSelect subQuery;
        
        private final List<CellReferenceOption> cellReferenceOptions = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
        }
    }
    
    @Getter
    @Setter
    public static class ModelColumnClause extends OracleSQLObjectImpl {
        
        private QueryPartitionClause queryPartitionClause;
        
        private final List<ModelColumn> dimensionByColumns = new ArrayList<>();
        
        private final List<ModelColumn> measuresColumns = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, queryPartitionClause);
                acceptChild(visitor, dimensionByColumns);
                acceptChild(visitor, measuresColumns);
            }
            visitor.endVisit(this);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public static class ModelColumn extends OracleSQLObjectImpl {
        
        private final SQLExpr expr;
        
        private final String alias;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, expr);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    public static class QueryPartitionClause extends OracleSQLObjectImpl {
        
        private final List<SQLExpr> exprList = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, exprList);
            }
        }
    }
    
    @Getter
    @Setter
    public static class MainModelClause extends OracleSQLObjectImpl {
        
        private SQLExpr mainModelName;
        
        private ModelColumnClause modelColumnClause;
        
        private ModelRulesClause modelRulesClause;
        
        private final List<CellReferenceOption> cellReferenceOptions = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, mainModelName);
                acceptChild(visitor, modelColumnClause);
                acceptChild(visitor, modelRulesClause);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class ModelRulesClause extends OracleSQLObjectImpl {
        
        private SQLExpr iterate;
        
        private SQLExpr until;
        
        private final List<ModelRuleOption> options = new ArrayList<>();
        
        private final List<CellAssignmentItem> cellAssignmentItems = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, iterate);
                acceptChild(visitor, until);
                acceptChild(visitor, cellAssignmentItems);
            }
            visitor.endVisit(this);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public enum ModelRuleOption {
        
        UPSERT("UPSERT"), 
        UPDATE("UPDATE"), 
        AUTOMATIC_ORDER("AUTOMATIC ORDER"), 
        SEQUENTIAL_ORDER("SEQUENTIAL ORDER");
        
        private final String text;
    }
    
    @Getter
    @Setter
    public static class CellAssignmentItem extends OracleSQLObjectImpl {
        
        private ModelRuleOption option;
        
        private CellAssignment cellAssignment;
        
        private SQLOrderBy orderBy;
        
        private SQLExpr expr;
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, cellAssignment);
                acceptChild(visitor, orderBy);
                acceptChild(visitor, expr);
            }
            visitor.endVisit(this);
        }
    }
    
    @Getter
    @Setter
    public static class CellAssignment extends OracleSQLObjectImpl {
        
        private SQLExpr measureColumn;
        
        private final List<SQLExpr> conditions = new ArrayList<>();
        
        @Override
        public void accept0(final OracleASTVisitor visitor) {
            if (visitor.visit(this)) {
                acceptChild(visitor, measureColumn);
                acceptChild(visitor, conditions);
            }
            visitor.endVisit(this);
        }
    }
}
