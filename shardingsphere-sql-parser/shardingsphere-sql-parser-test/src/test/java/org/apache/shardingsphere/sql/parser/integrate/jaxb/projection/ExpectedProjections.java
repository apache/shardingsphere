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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.projection;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.LinkedList;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedProjections {
    
    @XmlAttribute(name = "start-index")
    private Integer startIndex;
    
    @XmlAttribute(name = "stop-index")
    private Integer stopIndex;
    
    @XmlAttribute(name = "distinct-row")
    private boolean distinctRow;
    
    @XmlElement(name = "shorthand-projection")
    private Collection<ExpectedShorthandProjection> shorthandProjections = new LinkedList<>();
    
    @XmlElement(name = "aggregation-projection")
    private Collection<ExpectedAggregationProjection> aggregationProjections = new LinkedList<>();
    
    @XmlElement(name = "aggregation-distinct-projection")
    private Collection<ExpectedAggregationDistinctProjection> aggregationDistinctProjections = new LinkedList<>();
    
    @XmlElement(name = "column-projection")
    private Collection<ExpectedColumnProjection> columnProjections = new LinkedList<>();
    
    @XmlElement(name = "expression-projection")
    private Collection<ExpectedExpressionProjection> expressionProjections = new LinkedList<>();
    
    @XmlElement(name = "top-projection")
    private Collection<ExpectedTopProjection> topProjections = new LinkedList<>();
    
    /**
     * Get size.
     * 
     * @return size
     */
    public int getSize() {
        return aggregationProjections.size() + shorthandProjections.size() + aggregationDistinctProjections.size() + columnProjections.size() + expressionProjections.size() + topProjections.size();
    }
    
    /**
     * Find expected projections.
     * 
     * @param expectedProjectionType expected projections type
     * @param <T> type of projection type
     * @return expected projections
     */
    public <T extends ExpectedProjection> Collection<ExpectedProjection> findExpectedProjections(final Class<T> expectedProjectionType) {
        Collection<ExpectedProjection> result = new LinkedList<>();
        if (expectedProjectionType.equals(ExpectedShorthandProjection.class)) {
            result.addAll(shorthandProjections);
        }
        if (expectedProjectionType.equals(ExpectedAggregationProjection.class)) {
            result.addAll(aggregationProjections);
        }
        if (expectedProjectionType.equals(ExpectedColumnProjection.class)) {
            result.addAll(columnProjections);
        }
        if (expectedProjectionType.equals(ExpectedAggregationDistinctProjection.class)) {
            result.addAll(aggregationDistinctProjections);
        }
        if (expectedProjectionType.equals(ExpectedExpressionProjection.class)) {
            result.addAll(expressionProjections);
        }
        if (expectedProjectionType.equals(ExpectedTopProjection.class)) {
            result.addAll(expressionProjections);
        }
        return result;
    }
}
