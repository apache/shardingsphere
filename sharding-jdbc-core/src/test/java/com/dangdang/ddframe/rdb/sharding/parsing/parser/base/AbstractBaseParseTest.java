/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.base;

import com.dangdang.ddframe.rdb.common.jaxb.helper.ParserJAXBHelper;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Asserts;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import lombok.AccessLevel;
import lombok.Getter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractBaseParseTest {
    
    @Getter(AccessLevel.PROTECTED)
    private final String sql;

    @Getter(AccessLevel.PROTECTED)
    private final String[] parameters;

    @Getter(AccessLevel.PROTECTED)
    private final Set<DatabaseType> types;
    
    @Getter(AccessLevel.PROTECTED)
    private final Tables expectedTables;
    
    @Getter(AccessLevel.PROTECTED)
    private final Conditions expectedConditions;
    
    @Getter(AccessLevel.PROTECTED)
    private final Iterator<OrderItem> expectedOrderByColumns;
    
    @Getter(AccessLevel.PROTECTED)
    private final Iterator<OrderItem> expectedGroupByColumns;
    
    @Getter(AccessLevel.PROTECTED)
    private final Iterator<AggregationSelectItem> expectedAggregationSelectItems;
    
    @Getter(AccessLevel.PROTECTED)
    private final Limit expectedLimit;
    
    protected AbstractBaseParseTest(
            final String testCaseName, final String sql, final String[] parameters, final Set<DatabaseType> types, 
            final Tables expectedTables, final Conditions expectedConditions, final SQLStatement expectedSQLStatement) {
        this.sql = sql;
        this.parameters = parameters;
        this.types = types;
        this.expectedTables = expectedTables;
        this.expectedConditions = expectedConditions;
        if (expectedSQLStatement instanceof SelectStatement) {
            expectedOrderByColumns = ((SelectStatement) expectedSQLStatement).getOrderByItems().iterator();
            expectedGroupByColumns = ((SelectStatement) expectedSQLStatement).getGroupByItems().iterator();
            expectedAggregationSelectItems = ((SelectStatement) expectedSQLStatement).getAggregationSelectItems().iterator();
            expectedLimit = ((SelectStatement) expectedSQLStatement).getLimit();
        } else {
            expectedOrderByColumns = null;
            expectedGroupByColumns = null;
            expectedAggregationSelectItems = null;
            expectedLimit = null;
        }
    }
    
    protected static Collection<Object[]> dataParameters(final String path) {
        Collection<Object[]> result = new ArrayList<>();
        for (File each : new File(AbstractBaseParseTest.class.getClassLoader().getResource(path).getPath()).listFiles()) {
            result.addAll(dataParameters(each));
        }
        return result;
    }
    
    private static Collection<Object[]> dataParameters(final File file) {
        Asserts asserts = loadAsserts(file);
        Object[][] result = new Object[asserts.getAsserts().size()][7];
        for (int i = 0; i < asserts.getAsserts().size(); i++) {
            result[i] = getDataParameter(asserts.getAsserts().get(i));
        }
        return Arrays.asList(result);
    }
    
    private static Asserts loadAsserts(final File file) {
        try {
            return (Asserts) JAXBContext.newInstance(Asserts.class).createUnmarshaller().unmarshal(file);
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static Object[] getDataParameter(final Assert assertObj) {
        final Object[] result = new Object[7];
        result[0] = assertObj.getId();
        result[1] = assertObj.getSql();
        result[2] = ParserJAXBHelper.getParameters(assertObj);
        result[3] = ParserJAXBHelper.getDatabaseTypes(assertObj);
        result[4] = ParserJAXBHelper.getTables(assertObj);
        result[5] = ParserJAXBHelper.getConditions(assertObj);
        result[6] = ParserJAXBHelper.getSelectStatement(assertObj);
        return result;
    }
}
