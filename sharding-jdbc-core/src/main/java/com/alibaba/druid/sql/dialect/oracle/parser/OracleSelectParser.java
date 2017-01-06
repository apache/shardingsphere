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
package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionOperator;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.CycleClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.FlashbackQueryClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.FlashbackQueryClause.AsOfFlashbackQueryClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.FlashbackQueryClause.AsOfSnapshotClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.FlashbackQueryClause.VersionsFlashbackQueryClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.GroupingSetExpr;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.CellAssignment;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.CellAssignmentItem;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.CellReferenceOption;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.MainModelClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.ModelColumn;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.ModelColumnClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.ModelRuleOption;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.ModelRulesClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.QueryPartitionClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.ReferenceModelClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.ModelClause.ReturnRowsClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleWithSubqueryEntry;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.PartitionExtensionClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.SampleClause;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.SearchClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleOrderByItem;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelect;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectForUpdate;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectHierarchicalQueryClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectJoin;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectPivot;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectSubqueryTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectUnPivot;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;

import java.util.List;

public class OracleSelectParser extends SQLSelectParser {
    
    public OracleSelectParser(SQLExprParser exprParser){
        super(exprParser);
    }
    
    public OracleSelect select() {
        OracleSelect select = new OracleSelect();
        withSubquery(select);
        select.setQuery(query());
        select.setOrderBy(getExprParser().parseOrderBy());

        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();
            accept(Token.UPDATE);

            OracleSelectForUpdate forUpdate = new OracleSelectForUpdate();

            if (getLexer().equalToken(Token.OF)) {
                getLexer().nextToken();
                forUpdate.getOf().addAll(getExprParser().exprList(forUpdate));
            }
            if (getLexer().equalToken(Token.NOWAIT)) {
                getLexer().nextToken();
                forUpdate.setNotWait(true);
            } else if (getLexer().equalToken(Token.WAIT)) {
                getLexer().nextToken();
                forUpdate.setWait(getExprParser().primary());
            } else if (getLexer().identifierEquals("SKIP")) {
                getLexer().nextToken();
                accept("LOCKED");
                forUpdate.setSkipLocked(true);
            }

            select.setForUpdate(forUpdate);
        }

        if (select.getOrderBy() == null) {
            select.setOrderBy(getExprParser().parseOrderBy());
        }

        if (getLexer().equalToken(Token.WITH)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("READ")) {
                getLexer().nextToken();

                if (getLexer().identifierEquals("ONLY")) {
                    getLexer().nextToken();
                } else {
                    throw new ParserException(getLexer());
                }

            } else if (getLexer().equalToken(Token.CHECK)) {
                getLexer().nextToken();

                if (getLexer().identifierEquals("OPTION")) {
                    getLexer().nextToken();
                } else {
                    throw new ParserException(getLexer());
                }

                if (getLexer().equalToken(Token.CONSTRAINT)) {
                    getLexer().nextToken();
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
            } else {
                throw new ParserException(getLexer());
            }
        }

        return select;
    }
    
    @Override
    protected void withSubquery(final SQLSelect select) {
        if (getLexer().equalToken(Token.WITH)) {
            getLexer().nextToken();

            SQLWithSubqueryClause subqueryFactoringClause = new SQLWithSubqueryClause();
            while (true) {
                OracleWithSubqueryEntry entry = new OracleWithSubqueryEntry();
                entry.setName((SQLIdentifierExpr) getExprParser().name());

                if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();
                    getExprParser().names(entry.getColumns());
                    accept(Token.RIGHT_PAREN);
                }

                accept(Token.AS);
                accept(Token.LEFT_PAREN);
                entry.setSubQuery(select());
                accept(Token.RIGHT_PAREN);

                if (getLexer().identifierEquals("SEARCH")) {
                    getLexer().nextToken();
                    SearchClause searchClause = new SearchClause();

                    if (!getLexer().equalToken(Token.IDENTIFIER)) {
                        throw new ParserException(getLexer());
                    }

                    searchClause.setType(SearchClause.Type.valueOf(getLexer().getLiterals()));
                    getLexer().nextToken();

                    accept("FIRST");
                    accept(Token.BY);

                    searchClause.getItems().add((OracleOrderByItem) getExprParser().parseSelectOrderByItem());

                    while (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                        searchClause.getItems().add((OracleOrderByItem) getExprParser().parseSelectOrderByItem());
                    }

                    accept(Token.SET);

                    searchClause.setOrderingColumn((SQLIdentifierExpr) getExprParser().name());

                    entry.setSearchClause(searchClause);
                }

                if (getLexer().identifierEquals("CYCLE")) {
                    getLexer().nextToken();
                    CycleClause cycleClause = new CycleClause();
                    cycleClause.getAliases().addAll(getExprParser().exprList(cycleClause));
                    accept(Token.SET);
                    cycleClause.setMark(getExprParser().expr());
                    accept(Token.TO);
                    cycleClause.setValue(getExprParser().expr());
                    accept(Token.DEFAULT);
                    cycleClause.setDefaultValue(getExprParser().expr());
                    entry.setCycleClause(cycleClause);
                }

                subqueryFactoringClause.getEntries().add(entry);

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }

                break;
            }

            select.setWithSubQuery(subqueryFactoringClause);
        }
    }

    public SQLSelectQuery query() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            SQLSelectQuery select = query();
            accept(Token.RIGHT_PAREN);

            return queryRest(select);
        }

        OracleSelectQueryBlock queryBlock = new OracleSelectQueryBlock();
        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }
            queryBlock.getHints().addAll(getExprParser().parseHints());
            if (getLexer().equalToken(Token.DISTINCT)) {
                queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.UNIQUE)) {
                queryBlock.setDistionOption(SQLSetQuantifier.UNIQUE);
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.ALL)) {
                queryBlock.setDistionOption(SQLSetQuantifier.ALL);
                getLexer().nextToken();
            }
            queryBlock.getHints().addAll(getExprParser().parseHints());
            parseSelectList(queryBlock);
        }

        parseInto(queryBlock);

        parseFrom(queryBlock);

        parseWhere(queryBlock);

        parseHierachical(queryBlock);

        parseGroupBy(queryBlock);

        parseModelClause(queryBlock);

        return queryRest(queryBlock);
    }

    public SQLSelectQuery queryRest(SQLSelectQuery selectQuery) {
        if (getLexer().equalToken(Token.UNION)) {
            SQLUnionQuery union = new SQLUnionQuery();
            union.setLeft(selectQuery);

            getLexer().nextToken();

            if (getLexer().equalToken(Token.ALL)) {
                union.setOperator(SQLUnionOperator.UNION_ALL);
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.DISTINCT)) {
                union.setOperator(SQLUnionOperator.DISTINCT);
                getLexer().nextToken();
            }

            SQLSelectQuery right = query();

            union.setRight(right);

            return queryRest(union);
        }

        if (getLexer().equalToken(Token.INTERSECT)) {
            getLexer().nextToken();

            SQLUnionQuery union = new SQLUnionQuery();
            union.setLeft(selectQuery);

            union.setOperator(SQLUnionOperator.INTERSECT);

            SQLSelectQuery right = this.query();
            union.setRight(right);

            return union;
        }

        if (getLexer().equalToken(Token.MINUS)) {
            getLexer().nextToken();

            SQLUnionQuery union = new SQLUnionQuery();
            union.setLeft(selectQuery);

            union.setOperator(SQLUnionOperator.MINUS);

            SQLSelectQuery right = this.query();
            union.setRight(right);

            return union;
        }

        return selectQuery;
    }

    private void parseModelClause(OracleSelectQueryBlock queryBlock) {
        if (!getLexer().equalToken(Token.MODEL)) {
            return;
        }

        getLexer().nextToken();

        ModelClause model = new ModelClause();
        parseCellReferenceOptions(model.getCellReferenceOptions());

        if (getLexer().identifierEquals("RETURN")) {
            getLexer().nextToken();
            ReturnRowsClause returnRowsClause = new ReturnRowsClause();
            if (getLexer().equalToken(Token.ALL)) {
                getLexer().nextToken();
                returnRowsClause.setAll(true);
            } else {
                accept("UPDATED");
            }
            accept("ROWS");

            model.setReturnRowsClause(returnRowsClause);
        }

        while (getLexer().identifierEquals("REFERENCE")) {
            ReferenceModelClause referenceModelClause = new ReferenceModelClause();
            getLexer().nextToken();

            referenceModelClause.setName(getExprParser().expr());

            accept(Token.ON);
            accept(Token.LEFT_PAREN);
            OracleSelect subQuery = this.select();
            accept(Token.RIGHT_PAREN);
            referenceModelClause.setSubQuery(subQuery);

            parseModelColumnClause();

            parseCellReferenceOptions(referenceModelClause.getCellReferenceOptions());

            model.getReferenceModelClauses().add(referenceModelClause);
        }

        parseMainModelClause(model);

        queryBlock.setModelClause(model);
    }

    private void parseMainModelClause(ModelClause modelClause) {
        MainModelClause mainModel = new MainModelClause();
        if (getLexer().identifierEquals("MAIN")) {
            getLexer().nextToken();
            mainModel.setMainModelName(getExprParser().expr());
        }

        ModelColumnClause modelColumnClause = new ModelColumnClause();
        parseQueryPartitionClause(modelColumnClause);
        mainModel.setModelColumnClause(modelColumnClause);

        accept("DIMENSION");
        accept(Token.BY);
        accept(Token.LEFT_PAREN);
        while (true) {
            if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                getLexer().nextToken();
                break;
            }
            ModelColumn column = new ModelColumn(getExprParser().expr(), as());
            modelColumnClause.getDimensionByColumns().add(column);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            }
        }

        accept("MEASURES");
        accept(Token.LEFT_PAREN);
        while (true) {
            if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                getLexer().nextToken();
                break;
            }

            ModelColumn column = new ModelColumn(getExprParser().expr(), as());
            modelColumnClause.getMeasuresColumns().add(column);

            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            }
        }
        mainModel.setModelColumnClause(modelColumnClause);

        parseCellReferenceOptions(mainModel.getCellReferenceOptions());

        parseModelRulesClause(mainModel);

        modelClause.setMainModel(mainModel);
    }

    private void parseModelRulesClause(MainModelClause mainModel) {
        ModelRulesClause modelRulesClause = new ModelRulesClause();
        if (getLexer().identifierEquals("RULES")) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.UPDATE)) {
                modelRulesClause.getOptions().add(ModelRuleOption.UPDATE);
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("UPSERT")) {
                modelRulesClause.getOptions().add(ModelRuleOption.UPSERT);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("AUTOMATIC")) {
                getLexer().nextToken();
                accept(Token.ORDER);
                modelRulesClause.getOptions().add(ModelRuleOption.AUTOMATIC_ORDER);
            } else if (getLexer().identifierEquals("SEQUENTIAL")) {
                getLexer().nextToken();
                accept(Token.ORDER);
                modelRulesClause.getOptions().add(ModelRuleOption.SEQUENTIAL_ORDER);
            }
        }

        if (getLexer().identifierEquals("ITERATE")) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);
            modelRulesClause.setIterate(getExprParser().expr());
            accept(Token.RIGHT_PAREN);

            if (getLexer().identifierEquals("UNTIL")) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                modelRulesClause.setUntil(getExprParser().expr());
                accept(Token.RIGHT_PAREN);
            }
        }

        accept(Token.LEFT_PAREN);
        while (true) {
            if (getLexer().equalToken(Token.RIGHT_PAREN)) {
                getLexer().nextToken();
                break;
            }

            CellAssignmentItem item = new CellAssignmentItem();
            if (getLexer().equalToken(Token.UPDATE)) {
                item.setOption(ModelRuleOption.UPDATE);
            } else if (getLexer().identifierEquals("UPSERT")) {
                item.setOption(ModelRuleOption.UPSERT);
            }

            item.setCellAssignment(parseCellAssignment());
            item.setOrderBy(getExprParser().parseOrderBy());
            accept(Token.EQ);
            item.setExpr(getExprParser().expr());

            modelRulesClause.getCellAssignmentItems().add(item);
        }

        mainModel.setModelRulesClause(modelRulesClause);
    }

    private CellAssignment parseCellAssignment() {
        CellAssignment cellAssignment = new CellAssignment();

        cellAssignment.setMeasureColumn(getExprParser().expr());
        accept(Token.LEFT_BRACKET);
        cellAssignment.getConditions().addAll(getExprParser().exprList(cellAssignment));
        accept(Token.RIGHT_BRACKET);

        return cellAssignment;
    }

    private void parseQueryPartitionClause(ModelColumnClause modelColumnClause) {
        if (getLexer().identifierEquals("PARTITION")) {
            QueryPartitionClause queryPartitionClause = new QueryPartitionClause();

            getLexer().nextToken();
            accept(Token.BY);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                queryPartitionClause.getExprList().addAll(getExprParser().exprList(queryPartitionClause));
                accept(Token.RIGHT_PAREN);
            } else {
                queryPartitionClause.getExprList().addAll(getExprParser().exprList(queryPartitionClause));
            }
            modelColumnClause.setQueryPartitionClause(queryPartitionClause);
        }
    }

    private void parseModelColumnClause() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }

    private void parseCellReferenceOptions(List<CellReferenceOption> options) {
        if (getLexer().identifierEquals("IGNORE")) {
            getLexer().nextToken();
            accept("NAV");
            options.add(CellReferenceOption.IgnoreNav);
        } else if (getLexer().identifierEquals("KEEP")) {
            getLexer().nextToken();
            accept("NAV");
            options.add(CellReferenceOption.KeepNav);
        }

        if (getLexer().equalToken(Token.UNIQUE)) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("DIMENSION")) {
                getLexer().nextToken();
                options.add(CellReferenceOption.UniqueDimension);
            } else {
                accept("SINGLE");
                accept("REFERENCE");
                options.add(CellReferenceOption.UniqueDimension);
            }
        }
    }

    private void parseGroupBy(OracleSelectQueryBlock queryBlock) {
        if (getLexer().equalToken(Token.GROUP)) {
            getLexer().nextToken();
            accept(Token.BY);

            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            while (true) {
                if (getLexer().identifierEquals("GROUPING")) {
                    GroupingSetExpr groupingSet = new GroupingSetExpr();
                    getLexer().nextToken();
                    accept("SETS");
                    accept(Token.LEFT_PAREN);
                    groupingSet.getParameters().addAll(getExprParser().exprList(groupingSet));
                    accept(Token.RIGHT_PAREN);
                    groupBy.addItem(groupingSet);
                } else {
                    groupBy.addItem(getExprParser().expr());
                }

                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }

                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.HAVING)) {
                getLexer().nextToken();

                groupBy.setHaving(getExprParser().expr());
            }

            queryBlock.setGroupBy(groupBy);
        } else if (getLexer().equalToken(Token.HAVING)) {
            getLexer().nextToken();

            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            groupBy.setHaving(getExprParser().expr());

            if (getLexer().equalToken(Token.GROUP)) {
                getLexer().nextToken();
                accept(Token.BY);

                while (true) {
                    if (getLexer().identifierEquals("GROUPING")) {
                        GroupingSetExpr groupingSet = new GroupingSetExpr();
                        getLexer().nextToken();
                        accept("SETS");
                        accept(Token.LEFT_PAREN);
                        groupingSet.getParameters().addAll(getExprParser().exprList(groupingSet));
                        accept(Token.RIGHT_PAREN);
                        groupBy.addItem(groupingSet);
                    } else {
                        groupBy.addItem(getExprParser().expr());
                    }

                    if (!getLexer().equalToken(Token.COMMA)) {
                        break;
                    }

                    getLexer().nextToken();
                }
            }

            queryBlock.setGroupBy(groupBy);
        }
    }

    protected String as() {
        if (getLexer().equalToken(Token.CONNECT)) {
            return null;
        }
        return super.as();
    }

    private void parseHierachical(OracleSelectQueryBlock queryBlock) {
        OracleSelectHierarchicalQueryClause hierachical = null;

        if (getLexer().equalToken(Token.CONNECT)) {
            hierachical = new OracleSelectHierarchicalQueryClause();
            getLexer().nextToken();
            accept(Token.BY);

            if (getLexer().equalToken(Token.PRIOR)) {
                getLexer().nextToken();
                hierachical.setPrior(true);
            }

            if (getLexer().identifierEquals("NOCYCLE")) {
                hierachical.setNoCycle(true);
                getLexer().nextToken();

                if (getLexer().equalToken(Token.PRIOR)) {
                    getLexer().nextToken();
                    hierachical.setPrior(true);
                }
            }
            hierachical.setConnectBy(getExprParser().expr());
        }

        if (getLexer().equalToken(Token.START)) {
            getLexer().nextToken();
            if (hierachical == null) {
                hierachical = new OracleSelectHierarchicalQueryClause();
            }
            accept(Token.WITH);

            hierachical.setStartWith(getExprParser().expr());
        }

        if (getLexer().equalToken(Token.CONNECT)) {
            if (hierachical == null) {
                hierachical = new OracleSelectHierarchicalQueryClause();
            }

            getLexer().nextToken();
            accept(Token.BY);

            if (getLexer().equalToken(Token.PRIOR)) {
                getLexer().nextToken();
                hierachical.setPrior(true);
            }

            if (getLexer().identifierEquals("NOCYCLE")) {
                hierachical.setNoCycle(true);
                getLexer().nextToken();

                if (getLexer().equalToken(Token.PRIOR)) {
                    getLexer().nextToken();
                    hierachical.setPrior(true);
                }
            }
            hierachical.setConnectBy(getExprParser().expr());
        }

        if (hierachical != null) {
            queryBlock.setHierarchicalQueryClause(hierachical);
        }
    }

    @Override
    public SQLTableSource parseTableSource() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            OracleSelectSubqueryTableSource tableSource;
            if (getLexer().equalToken(Token.SELECT) || getLexer().equalToken(Token.WITH)) {
                tableSource = new OracleSelectSubqueryTableSource(select());
            } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
                tableSource = new OracleSelectSubqueryTableSource(select());
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            accept(Token.RIGHT_PAREN);

            parsePivot(tableSource);

            return parseTableSourceRest(tableSource);
        }

        if (getLexer().equalToken(Token.SELECT)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        OracleSelectTableReference tableReference = new OracleSelectTableReference();

        if (getLexer().identifierEquals("ONLY")) {
            getLexer().nextToken();
            tableReference.setOnly(true);
            accept(Token.LEFT_PAREN);
            parseTableSourceQueryTableExpr(tableReference);
            accept(Token.RIGHT_PAREN);
        } else {
            parseTableSourceQueryTableExpr(tableReference);
            parsePivot(tableReference);
        }

        return parseTableSourceRest(tableReference);
    }

    private void parseTableSourceQueryTableExpr(OracleSelectTableReference tableReference) {
        tableReference.setExpr(getExprParser().expr());

        {
            FlashbackQueryClause clause = flashback();
            tableReference.setFlashback(clause);
        }

        if (getLexer().identifierEquals("SAMPLE")) {
            getLexer().nextToken();

            SampleClause sample = new SampleClause();

            if (getLexer().identifierEquals("BLOCK")) {
                sample.setBlock(true);
                getLexer().nextToken();
            }

            accept(Token.LEFT_PAREN);
            sample.getPercent().addAll(getExprParser().exprList(sample));
            accept(Token.RIGHT_PAREN);
            if (getLexer().identifierEquals("SEED")) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                sample.setSeedValue(getExprParser().expr());
                accept(Token.RIGHT_PAREN);
            }

            tableReference.setSampleClause(sample);
        }

        if (getLexer().identifierEquals("PARTITION")) {
            getLexer().nextToken();
            PartitionExtensionClause partition = new PartitionExtensionClause();

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                partition.setPartition(getExprParser().name());
                accept(Token.RIGHT_PAREN);
            } else {
                accept(Token.FOR);
                accept(Token.LEFT_PAREN);
                getExprParser().names(partition.getTarget());
                accept(Token.RIGHT_PAREN);
            }

            tableReference.setPartition(partition);
        }

        if (getLexer().identifierEquals("SUBPARTITION")) {
            getLexer().nextToken();
            PartitionExtensionClause partition = new PartitionExtensionClause();
            partition.setSubPartition(true);

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                partition.setPartition(getExprParser().name());
                accept(Token.RIGHT_PAREN);
            } else {
                accept(Token.FOR);
                accept(Token.LEFT_PAREN);
                getExprParser().names(partition.getTarget());
                accept(Token.RIGHT_PAREN);
            }

            tableReference.setPartition(partition);
        }

        if (getLexer().identifierEquals("VERSIONS")) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.BETWEEN)) {
                getLexer().nextToken();

                VersionsFlashbackQueryClause clause = new VersionsFlashbackQueryClause();
                if (getLexer().identifierEquals("SCN")) {
                    clause.setType(AsOfFlashbackQueryClause.Type.SCN);
                    getLexer().nextToken();
                } else {
                    accept("TIMESTAMP");
                    clause.setType(AsOfFlashbackQueryClause.Type.TIMESTAMP);
                }

                SQLBinaryOpExpr binaryExpr = (SQLBinaryOpExpr) getExprParser().expr();
                if (binaryExpr.getOperator() != SQLBinaryOperator.BooleanAnd) {
                    throw new ParserException("syntax error : " + binaryExpr.getOperator());
                }

                clause.setBegin(binaryExpr.getLeft());
                clause.setEnd(binaryExpr.getRight());

                tableReference.setFlashback(clause);
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
        }

    }

    private FlashbackQueryClause flashback() {
        if (getLexer().equalToken(Token.AS)) {
            getLexer().nextToken();
        }

        if (getLexer().equalToken(Token.OF)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("SCN")) {
                AsOfFlashbackQueryClause clause = new AsOfFlashbackQueryClause();
                clause.setType(AsOfFlashbackQueryClause.Type.SCN);
                getLexer().nextToken();
                clause.setExpr(getExprParser().expr());

                return clause;
            } else if (getLexer().identifierEquals("SNAPSHOT")) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                AsOfSnapshotClause clause = new AsOfSnapshotClause(getExprParser().expr());
                accept(Token.RIGHT_PAREN);

                return clause;
            } else {
                AsOfFlashbackQueryClause clause = new AsOfFlashbackQueryClause();
                accept("TIMESTAMP");
                clause.setType(AsOfFlashbackQueryClause.Type.TIMESTAMP);
                clause.setExpr(getExprParser().expr());

                return clause;
            }

        }

        return null;
    }

    protected SQLTableSource parseTableSourceRest(OracleSelectTableSource tableSource) {
        if (getLexer().equalToken(Token.AS)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.OF)) {
                tableSource.setFlashback(flashback());
            }

            tableSource.setAlias(as());
        } else if ((tableSource.getAlias() == null) || (tableSource.getAlias().length() == 0)) {
            if (!getLexer().equalToken(Token.LEFT) && !getLexer().equalToken(Token.RIGHT) && !getLexer().equalToken(Token.FULL)) {
                tableSource.setAlias(as());
            }
        }

        if (getLexer().equalToken(Token.HINT)) {
            tableSource.getHints().addAll(getExprParser().parseHints());
        }

        OracleSelectJoin.JoinType joinType = null;

        if (getLexer().equalToken(Token.LEFT)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.OUTER)) {
                getLexer().nextToken();
            }
            accept(Token.JOIN);
            joinType = OracleSelectJoin.JoinType.LEFT_OUTER_JOIN;
        }

        if (getLexer().equalToken(Token.RIGHT)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.OUTER)) {
                getLexer().nextToken();
            }
            accept(Token.JOIN);
            joinType = OracleSelectJoin.JoinType.RIGHT_OUTER_JOIN;
        }

        if (getLexer().equalToken(Token.FULL)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.OUTER)) {
                getLexer().nextToken();
            }
            accept(Token.JOIN);
            joinType = OracleSelectJoin.JoinType.FULL_OUTER_JOIN;
        }

        if (getLexer().equalToken(Token.INNER)) {
            getLexer().nextToken();
            accept(Token.JOIN);
            joinType = OracleSelectJoin.JoinType.INNER_JOIN;
        }
        if (getLexer().equalToken(Token.CROSS)) {
            getLexer().nextToken();
            accept(Token.JOIN);
            joinType = OracleSelectJoin.JoinType.CROSS_JOIN;
        }

        if (getLexer().equalToken(Token.JOIN)) {
            getLexer().nextToken();
            joinType = OracleSelectJoin.JoinType.JOIN;
        }

        if (getLexer().equalToken(Token.COMMA)) {
            getLexer().nextToken();
            joinType = OracleSelectJoin.JoinType.COMMA;
        }

        if (joinType != null) {
            OracleSelectJoin join = new OracleSelectJoin();
            join.setLeft(tableSource);
            join.setJoinType(joinType);
            join.setRight(parseTableSource());

            if (getLexer().equalToken(Token.ON)) {
                getLexer().nextToken();
                join.setCondition(getExprParser().expr());
            } else if (getLexer().equalToken(Token.USING)) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                join.getUsing().addAll(getExprParser().exprList(join));
                accept(Token.RIGHT_PAREN);
            }

            return parseTableSourceRest(join);
        }

        return tableSource;
    }

    private void parsePivot(OracleSelectTableSource tableSource) {
        OracleSelectPivot.Item item;
        if (getLexer().identifierEquals("PIVOT")) {
            getLexer().nextToken();

            OracleSelectPivot pivot = new OracleSelectPivot();

            if (getLexer().identifierEquals("XML")) {
                getLexer().nextToken();
                pivot.setXml(true);
            }

            accept(Token.LEFT_PAREN);
            while (true) {
                item = new OracleSelectPivot.Item();
                item.setExpr(getExprParser().expr());
                item.setAlias(as());
                pivot.getItems().add(item);

                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }
                getLexer().nextToken();
            }

            accept(Token.FOR);

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                while (true) {
                    pivot.getPivotFor().add(new SQLIdentifierExpr(getLexer().getLiterals()));
                    getLexer().nextToken();

                    if (!getLexer().equalToken(Token.COMMA)) {
                        break;
                    }
                    getLexer().nextToken();
                }

                accept(Token.RIGHT_PAREN);
            } else {
                pivot.getPivotFor().add(new SQLIdentifierExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            }

            accept(Token.IN);
            accept(Token.LEFT_PAREN);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                throw new ParserUnsupportedException(getLexer().getToken());
            }

            if (getLexer().equalToken(Token.SELECT)) {
                throw new ParserUnsupportedException(getLexer().getToken());
            }

            while (true) {
                item = new OracleSelectPivot.Item();
                item.setExpr(getExprParser().expr());
                item.setAlias(as());
                pivot.getPivotIn().add(item);

                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }

                getLexer().nextToken();
            }

            accept(Token.RIGHT_PAREN);

            accept(Token.RIGHT_PAREN);

            tableSource.setPivot(pivot);
        } else if (getLexer().identifierEquals("UNPIVOT")) {
            getLexer().nextToken();

            OracleSelectUnPivot unPivot = new OracleSelectUnPivot();
            if (getLexer().identifierEquals("INCLUDE")) {
                getLexer().nextToken();
                accept("NULLS");
                unPivot.setNullsIncludeType(OracleSelectUnPivot.NullsIncludeType.INCLUDE_NULLS);
            } else if (getLexer().identifierEquals("EXCLUDE")) {
                getLexer().nextToken();
                accept("NULLS");
                unPivot.setNullsIncludeType(OracleSelectUnPivot.NullsIncludeType.EXCLUDE_NULLS);
            }

            accept(Token.LEFT_PAREN);

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                unPivot.getItems().addAll(getExprParser().exprList(unPivot));
                accept(Token.RIGHT_PAREN);
            } else {
                unPivot.getItems().add(getExprParser().expr());
            }

            accept(Token.FOR);

            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().nextToken();
                while (true) {
                    unPivot.getPivotFor().add(new SQLIdentifierExpr(getLexer().getLiterals()));
                    getLexer().nextToken();

                    if (!getLexer().equalToken(Token.COMMA)) {
                        break;
                    }
                    getLexer().nextToken();
                }

                accept(Token.RIGHT_PAREN);
            } else {
                unPivot.getPivotFor().add(new SQLIdentifierExpr(getLexer().getLiterals()));
                getLexer().nextToken();
            }

            accept(Token.IN);
            accept(Token.LEFT_PAREN);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            if (getLexer().equalToken(Token.SELECT)) {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            while (true) {
                item = new OracleSelectPivot.Item();
                item.setExpr(getExprParser().expr());
                item.setAlias(as());
                unPivot.getPivotIn().add(item);

                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }

                getLexer().nextToken();
            }

            accept(Token.RIGHT_PAREN);

            accept(Token.RIGHT_PAREN);

            tableSource.setPivot(unPivot);
        }
    }

    protected void parseInto(OracleSelectQueryBlock x) {
        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();
            SQLExpr expr = getExprParser().expr();
            if (!getLexer().equalToken(Token.COMMA)) {
                x.setInto(expr);
                return;
            }
            SQLListExpr list = new SQLListExpr();
            list.getItems().add(expr);
            while (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
                list.getItems().add(getExprParser().expr());
            }
            x.setInto(list);
        }
    }
}
