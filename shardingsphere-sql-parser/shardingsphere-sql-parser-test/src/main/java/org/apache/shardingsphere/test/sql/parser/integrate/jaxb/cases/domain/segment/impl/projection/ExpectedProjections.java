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

package org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.ExpectedSQLSegment;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection.impl.aggregation.ExpectedAggregationDistinctProjection;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection.impl.aggregation.ExpectedAggregationProjection;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection.impl.column.ExpectedColumnProjection;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection.impl.expression.ExpectedExpressionProjection;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection.impl.shorthand.ExpectedShorthandProjection;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection.impl.subquery.ExpectedSubqueryProjection;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.projection.impl.top.ExpectedTopProjection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Setter
public final class ExpectedProjections extends AbstractExpectedSQLSegment {
    
    @Getter
    @XmlAttribute(name = "distinct-row")
    private boolean distinctRow;
    
    @XmlElement(name = "shorthand-projection")
    private final Collection<ExpectedShorthandProjection> shorthandProjections = new LinkedList<>();
    
    @XmlElement(name = "column-projection")
    private final Collection<ExpectedColumnProjection> columnProjections = new LinkedList<>();
    
    @XmlElement(name = "aggregation-projection")
    private final Collection<ExpectedAggregationProjection> aggregationProjections = new LinkedList<>();
    
    @XmlElement(name = "aggregation-distinct-projection")
    private final Collection<ExpectedAggregationDistinctProjection> aggregationDistinctProjections = new LinkedList<>();
    
    @XmlElement(name = "expression-projection")
    private final Collection<ExpectedExpressionProjection> expressionProjections = new LinkedList<>();
    
    @XmlElement(name = "top-projection")
    private final Collection<ExpectedTopProjection> topProjections = new LinkedList<>();
    
    @XmlElement(name = "subquery-projection")
    private final Collection<ExpectedSubqueryProjection> subqueryProjections = new LinkedList<>();
    
    /**
     * Get size.
     * 
     * @return size
     */
    public int getSize() {
        return shorthandProjections.size() + columnProjections.size() + aggregationProjections.size() + aggregationDistinctProjections.size() 
                + expressionProjections.size() + topProjections.size() + subqueryProjections.size();
    }
    
    /**
     * Get expected projections.
     *
     * @return expected projections
     */
    public List<ExpectedProjection> getExpectedProjections() {
        List<ExpectedProjection> result = new LinkedList<>();
        result.addAll(shorthandProjections);
        result.addAll(columnProjections);
        result.addAll(aggregationProjections);
        result.addAll(aggregationDistinctProjections);
        result.addAll(expressionProjections);
        result.addAll(topProjections);
        result.addAll(subqueryProjections);
        result.sort(Comparator.comparingInt(ExpectedSQLSegment::getStartIndex));
        return result;
    }
}
