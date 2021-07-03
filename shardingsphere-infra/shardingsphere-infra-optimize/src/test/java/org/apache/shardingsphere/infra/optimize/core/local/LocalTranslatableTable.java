/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.optimize.core.local;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.QueryableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Source;

import lombok.Data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for table that reads CSV files.
 */
@Data
public class LocalTranslatableTable extends AbstractTable implements QueryableTable, TranslatableTable {
    
    private final Source source;
    
    private final RelProtoDataType protoRowType;
    
    private List<LocalFieldType> fieldTypes;

    /**
     * Construct method.
     * @param source source
     * @param protoRowType rel proto data type
     */
    LocalTranslatableTable(final Source source, final RelProtoDataType protoRowType) {
        this.source = source;
        this.protoRowType = protoRowType;
    }

    /**
     * Get row type.
     * @param typeFactory rel data type factory
     * @return row type
     */
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        if (protoRowType != null) {
            return protoRowType.apply(typeFactory);
        }
        if (fieldTypes == null) {
            fieldTypes = new ArrayList<>();
            return LocalEnumerator.deduceRowType((JavaTypeFactory) typeFactory, source, fieldTypes);
        } else {
            return LocalEnumerator.deduceRowType((JavaTypeFactory) typeFactory, source, null);
        }
    }

    /**
     * Get name of table type.
     * @return name of table type
     */
    public String toString() {
        return "LocalTranslatableTable";
    }

    /**
     * Create calcite enumerable object.
     * @param root data context
     * @param fields field array
     * @return calcite enumerable object
     */
    public Enumerable<Object> project(final DataContext root, final int[] fields) {
        final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
        return new AbstractEnumerable<Object>() {
            public Enumerator<Object> enumerator() {
                return new LocalEnumerator<>(source, cancelFlag, fieldTypes, fields);
            }
        };
    }

    /**
     * Get table expression.
     * @param schema parent schema
     * @param tableName table name
     * @param clazz class
     * @return table expression
     */
    public Expression getExpression(final SchemaPlus schema, final String tableName, final Class clazz) {
        return Schemas.tableExpression(schema, getElementType(), tableName, clazz);
    }

    /**
     * Get element type.
     * @return element type
     */
    public Type getElementType() {
        return Object[].class;
    }

    /**
     * Generate an unsupported operation exception.
     * @param queryProvider query provider
     * @param schema schema
     * @param tableName table name
     * @param <T> generics type
     * @return throw an unsupported operation exception
     */
    public <T> Queryable<T> asQueryable(final QueryProvider queryProvider, final SchemaPlus schema, final String tableName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get rel node.
     * @param context  rel context
     * @param relOptTable rel opt table
     * @return local table scan.
     */
    public RelNode toRel(final RelOptTable.ToRelContext context, final RelOptTable relOptTable) {
        final int fieldCount = relOptTable.getRowType().getFieldCount();
        final int[] fields = LocalEnumerator.identityList(fieldCount);
        return new LocalTableScan(context.getCluster(), relOptTable, this, fields);
    }
}
