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

package org.apache.shardingsphere.core.optimize.sharding.engnie.dml;

import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSelectOptimizeEngineTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private TableMetas tableMetas;
    
    private SelectStatement selectStatement;
    
    @Before
    public void setUp() {
        selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
    }
    
    @Test
    public void assertOptimizeAlwaysFalseListConditions() {
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(), 
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(), new PredicateCompareRightValue("=", new LiteralExpressionSegment(0, 0, 3))));
        andPredicates.add(andPredicate);
        WhereSegment whereSegment = new WhereSegment(0, 0, 0);
        whereSegment.getAndPredicates().addAll(andPredicates);
        selectStatement.setWhere(whereSegment);
    }
    
    @Test
    public void assertOptimizeAlwaysFalseRangeConditions() {
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 3), new LiteralExpressionSegment(0, 0, 4)))));
        andPredicates.add(andPredicate);
        WhereSegment whereSegment = new WhereSegment(0, 0, 0);
        whereSegment.getAndPredicates().addAll(andPredicates);
        selectStatement.setWhere(whereSegment);
    }
    
    @Test
    public void assertOptimizeAlwaysFalseListConditionsAndRangeConditions() {
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 3), new LiteralExpressionSegment(0, 0, 4))));
        andPredicates.add(andPredicate);
        WhereSegment whereSegment = new WhereSegment(0, 0, 0);
        whereSegment.getAndPredicates().addAll(andPredicates);
        selectStatement.setWhere(whereSegment);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeListConditions() {
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(), new PredicateCompareRightValue("=", new LiteralExpressionSegment(0, 0, 1))));
        andPredicates.add(andPredicate);
        WhereSegment whereSegment = new WhereSegment(0, 0, 0);
        whereSegment.getAndPredicates().addAll(andPredicates);
        selectStatement.setWhere(whereSegment);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeRangeConditions() {
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 3))));
        andPredicates.add(andPredicate);
        WhereSegment whereSegment = new WhereSegment(0, 0, 0);
        whereSegment.getAndPredicates().addAll(andPredicates);
        selectStatement.setWhere(whereSegment);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeListConditionsAndRangeConditions() {
        selectStatement.setSelectItems(new SelectItemsSegment(0, 0, false));
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2))));
        andPredicates.add(andPredicate);
        WhereSegment whereSegment = new WhereSegment(0, 0, 0);
        whereSegment.getAndPredicates().addAll(andPredicates);
        selectStatement.setWhere(whereSegment);
    }
    
    private ColumnSegment createColumnSegment() {
        ColumnSegment result = new ColumnSegment(0, 0, "column");
        result.setOwner(new TableSegment(0, 0, "tbl"));
        return result;
    }
    
    @Test
    public void assertOptimizeWithShorthandItems() {
        when(tableMetas.get("tbl")).thenReturn(createTableMetaData());
        selectStatement.setSelectItems(createSelectItemsSegment());
        selectStatement.getTables().add(new TableSegment(0, 0, "tbl"));
        SelectItems selectItems = new ShardingSelectOptimizeEngine().optimize(shardingRule, tableMetas, "", Collections.emptyList(), selectStatement).getSelectItems();
        assertThat(selectItems.getColumnLabels().size(), is(2));
        assertThat(selectItems.getColumnLabels().get(0), is("id"));
        assertThat(selectItems.getColumnLabels().get(1), is("user_id"));
    }
    
    private SelectItemsSegment createSelectItemsSegment() {
        TableSegment owner = mock(TableSegment.class);
        when(owner.getTableName()).thenReturn("tbl");
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 0, "tbl.*");
        shorthandSelectItemSegment.setOwner(owner);
        SelectItemsSegment result = new SelectItemsSegment(0, 0, false);
        result.getSelectItems().add(shorthandSelectItemSegment);
        return result;
    }
    
    private TableMetaData createTableMetaData() {
        ColumnMetaData idColumnMetaData = new ColumnMetaData("id", "int", true);
        ColumnMetaData nameColumnMetaData = new ColumnMetaData("user_id", "int", false);
        return new TableMetaData(Arrays.asList(idColumnMetaData, nameColumnMetaData), Arrays.asList("id", "user_id"));
    }
}
