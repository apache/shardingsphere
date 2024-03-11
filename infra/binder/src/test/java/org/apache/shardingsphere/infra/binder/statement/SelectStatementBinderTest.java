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

package org.apache.shardingsphere.infra.binder.statement;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectStatementBinderTest {
    
    @Test
    void assertBind() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projections);
        ColumnProjectionSegment orderIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id")));
        ColumnProjectionSegment userIdProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id")));
        ColumnProjectionSegment statusProjection = new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("status")));
        projections.getProjections().add(orderIdProjection);
        projections.getProjections().add(userIdProjection);
        projections.getProjections().add(statusProjection);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        selectStatement.setFrom(simpleTableSegment);
        selectStatement.setWhere(mockWhereSegment());
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement, createMetaData(), DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(selectStatement));
        assertThat(actual.getFrom(), not(selectStatement.getFrom()));
        assertThat(actual.getFrom(), instanceOf(SimpleTableSegment.class));
        assertThat(((SimpleTableSegment) actual.getFrom()).getTableName(), not(simpleTableSegment.getTableName()));
        assertThat(actual.getProjections(), not(selectStatement.getProjections()));
        List<ProjectionSegment> actualProjections = new ArrayList<>(actual.getProjections().getProjections());
        assertThat(actualProjections, not(selectStatement.getProjections()));
        assertThat(actualProjections.get(0), not(orderIdProjection));
        assertThat(actualProjections.get(0), instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(0)).getColumn(), not(orderIdProjection.getColumn()));
        assertThat(actualProjections.get(1), not(userIdProjection));
        assertThat(actualProjections.get(1), instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(1)).getColumn(), not(userIdProjection.getColumn()));
        assertThat(actualProjections.get(2), not(statusProjection));
        assertThat(actualProjections.get(2), instanceOf(ColumnProjectionSegment.class));
        assertThat(((ColumnProjectionSegment) actualProjections.get(2)).getColumn(), not(statusProjection.getColumn()));
        assertTrue(actual.getWhere().isPresent());
        assertThat(actual.getWhere().get(), not(selectStatement.getWhere()));
        assertThat(actual.getWhere().get(), instanceOf(WhereSegment.class));
        assertTrue(selectStatement.getWhere().isPresent());
        assertThat(actual.getWhere().get().getExpr(), not(selectStatement.getWhere().get().getExpr()));
        assertThat(actual.getWhere().get().getExpr(), instanceOf(BinaryOperationExpression.class));
        assertThat(((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft(), instanceOf(FunctionSegment.class));
        assertThat(((FunctionSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getParameters().iterator().next(), instanceOf(ColumnSegment.class));
        assertThat(((ColumnSegment) ((FunctionSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getParameters().iterator().next())
                .getColumnBoundedInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    private static WhereSegment mockWhereSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "nvl", "nvl(status, 0)");
        functionSegment.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("status")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, 0));
        return new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, functionSegment, new LiteralExpressionSegment(0, 0, 0), "=", "nvl(status, 0) = 0"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        when(result.containsDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).containsSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME).containsTable("t_order")).thenReturn(true);
        return result;
    }
    
    @Test
    void assertBindWithOracleWithClause() {
        String sql = "WITH user_temp AS (SELECT t.id, t.user_id, t.user_sn, t.user_no FROM t_user t), "
                + "product1_temp AS (SELECT * FROM (SELECT t.name, t.detail FROM t_order_product1 t, user_temp WHERE t.user_sn = user_temp.user_sn) WHERE ROWNUM = 1), "
                + "product2_temp AS (SELECT * FROM (SELECT t.sub_product_url FROM t_order_product2 t, user_temp WHERE t.user_no = user_temp.user_no) WHERE ROWNUM = 1), "
                + "item_temp AS (SELECT COUNT(item.id) n FROM t_order_item item, user_temp WHERE item.user_id = user_temp.user_id AND item.status IN (1,2)), "
                + "item_ext_temp AS (SELECT * FROM (SELECT c.item_order_url FROM t_order_item_ext c, t_store r, user_temp WHERE c.item_no = r.item_no AND c.status = 1 "
                + "OR ((SELECT * FROM item_temp) >= 1) AND r.user_rn = user_temp.id AND r.store_no = 's1234') WHERE ROWNUM = 1) "
                + "SELECT product1_temp.name, product1_temp.detail, product2_temp.sub_product_url, item_ext_temp.item_order_url FROM product1_temp, product2_temp, item_ext_temp";
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, databaseType, resourceMetaData, ruleMetaData, buildSchemas());
        SQLStatementParserEngine parserEngine = new SQLStatementParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
        ShardingSphereMetaData metaData =
                new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), resourceMetaData, ruleMetaData, new ConfigurationProperties(new Properties()));
        SelectStatement selectStatement = (SelectStatement) parserEngine.parse(sql, false);
        SelectStatement actual = new SelectStatementBinder().bind(selectStatement, metaData, DefaultDatabase.LOGIC_NAME);
        assertThat(actual, not(selectStatement));
        assertThat(actual, instanceOf(OracleSelectStatement.class));
        assertTrue(((OracleSelectStatement) actual).getWithSegment().isPresent());
        assertThat(((OracleSelectStatement) actual).getWithSegment().get().getCommonTableExpressions().size(), is(5));
    }
    
    private Map<String, ShardingSphereSchema> buildSchemas() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(6, 1F);
        tables.put("t_user", new ShardingSphereTable("t_user", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_sn", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("user_no", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_product1", new ShardingSphereTable("t_order_product1", Arrays.asList(
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("detail", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_sn", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_product2", new ShardingSphereTable("t_order_product2", Arrays.asList(
                new ShardingSphereColumn("user_no", Types.VARCHAR, true, false, false, true, false, false),
                new ShardingSphereColumn("sub_product_url", Types.VARCHAR, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_item", new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_item_ext", new ShardingSphereTable("t_order_item_ext", Arrays.asList(
                new ShardingSphereColumn("item_order_url", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("item_no", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_store", new ShardingSphereTable("t_store", Arrays.asList(
                new ShardingSphereColumn("item_no", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("user_rn", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("store_no", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        return Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new ShardingSphereSchema(tables, Collections.emptyMap()));
    }
}
