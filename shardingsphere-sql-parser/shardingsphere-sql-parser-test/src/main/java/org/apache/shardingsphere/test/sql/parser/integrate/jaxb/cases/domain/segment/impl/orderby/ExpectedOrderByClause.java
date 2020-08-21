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

package org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.orderby;

import lombok.Getter;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.orderby.item.impl.ExpectedColumnOrderByItem;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.orderby.item.impl.ExpectedExpressionOrderByItem;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.orderby.item.impl.ExpectedIndexOrderByItem;

import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Expected order by clause.
 */
@Getter
public final class ExpectedOrderByClause extends AbstractExpectedSQLSegment {
    
    @XmlElement(name = "column-item")
    private final List<ExpectedColumnOrderByItem> columnItems = new LinkedList<>();
    
    @XmlElement(name = "index-item")
    private final List<ExpectedIndexOrderByItem> indexItems = new LinkedList<>();
    
    @XmlElement(name = "expression-item")
    private final List<ExpectedExpressionOrderByItem> expressionItems = new LinkedList<>();
    
    /**
     * Get item size.
     * 
     * @return item size
     */
    public int getItemSize() {
        return columnItems.size() + indexItems.size() + expressionItems.size();
    }
}
