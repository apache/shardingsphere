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

package org.apache.shardingsphere.sqlfederation.compiler.rel.operator.physical;

import lombok.Getter;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.adapter.enumerable.PhysType;
import org.apache.calcite.adapter.enumerable.PhysTypeImpl;
import org.apache.calcite.linq4j.tree.Blocks;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.util.SqlString;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.SQLDialectFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Enumerable scan.
 */
@Getter
public final class EnumerableScan extends TableScan implements EnumerableRel {
    
    private final SqlString sqlString;
    
    private final RelDataType pushDownRowType;
    
    public EnumerableScan(final RelOptCluster cluster, final RelTraitSet traitSet, final RelOptTable table, final RelNode pushDownRelNode, final String databaseType) {
        super(cluster, traitSet.replace(EnumerableConvention.INSTANCE), Collections.emptyList(), table);
        sqlString = createSQLString(pushDownRelNode, databaseType);
        pushDownRowType = pushDownRelNode.getRowType();
    }
    
    public EnumerableScan(final RelOptCluster cluster, final RelTraitSet traitSet, final RelOptTable table, final SqlString sqlString, final RelDataType pushDownRowType) {
        super(cluster, traitSet.replace(EnumerableConvention.INSTANCE), Collections.emptyList(), table);
        this.sqlString = sqlString;
        this.pushDownRowType = pushDownRowType;
    }
    
    @Override
    public RelNode copy(final RelTraitSet traitSet, final List<RelNode> inputs) {
        return new EnumerableScan(getCluster(), traitSet, table, sqlString, pushDownRowType);
    }
    
    @Override
    public RelWriter explainTerms(final RelWriter relWriter) {
        return super.explainTerms(relWriter).item("sql", sqlString.getSql().replaceAll(System.lineSeparator(), " ")).item("dynamicParameters", sqlString.getDynamicParameters());
    }
    
    @Override
    public RelDataType deriveRowType() {
        return pushDownRowType;
    }
    
    @Override
    public Result implement(final EnumerableRelImplementor implementor, final Prefer pref) {
        PhysType physType = PhysTypeImpl.of(implementor.getTypeFactory(), getPushDownRowType(), pref.preferArray());
        int[] paramIndexes = null == sqlString.getDynamicParameters() ? new int[]{} : getParamIndexes(sqlString.getDynamicParameters());
        return implementor.result(physType, Blocks.toBlock(Expressions.call(Objects.requireNonNull(table.getExpression(SQLFederationTable.class)), "implement", implementor.getRootExpression(),
                Expressions.constant(sqlString.getSql().replace("u&'\\", "'\\u")), Expressions.constant(paramIndexes))));
    }
    
    private SqlString createSQLString(final RelNode scanContext, final String databaseType) {
        SqlDialect sqlDialect = SQLDialectFactory.getSQLDialect(databaseType);
        return new RelToSqlConverter(sqlDialect).visitRoot(scanContext).asStatement().toSqlString(sqlDialect);
    }
    
    private int[] getParamIndexes(final Collection<Integer> dynamicParameters) {
        int[] result = new int[dynamicParameters.size()];
        int index = 0;
        for (Integer each : dynamicParameters) {
            result[index++] = each;
        }
        return result;
    }
}
