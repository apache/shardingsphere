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

package org.apache.shardingsphere.core.parse.integrate.jaxb.predicate;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.value.ExpectedPredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.value.ExpectedPredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.value.ExpectedPredicateInRightValue;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.value.ExpectedPredicateRightValue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedPredicateSegment extends ExpectedBaseSegment {

    @XmlElement(name = "column-segment")
    private ExpectedColumnSegment column = new ExpectedColumnSegment();

    @XmlElement(name = "predicate-between-right-value")
    private ExpectedPredicateBetweenRightValue betweenRightValue = new ExpectedPredicateBetweenRightValue();

    @XmlElement(name = "predicate-in-right-value")
    private ExpectedPredicateInRightValue inRightValue = new ExpectedPredicateInRightValue();

    @XmlElement(name = "predicate-compare-right-value")
    private ExpectedPredicateCompareRightValue compareRightValue = new ExpectedPredicateCompareRightValue();

    /**
     * find expected right value type
     * @param expectedPredicateRightValue
     * @return right value
     */
    public <T extends ExpectedPredicateRightValue> T findExpectedRightValue(final Class<T> expectedPredicateRightValue) {
        if (expectedPredicateRightValue.isAssignableFrom(ExpectedPredicateCompareRightValue.class)) {
            return (T) compareRightValue;
        }
        if (expectedPredicateRightValue.isAssignableFrom(ExpectedPredicateInRightValue.class)) {
            return (T) inRightValue;
        }
        if (expectedPredicateRightValue.isAssignableFrom(ExpectedPredicateBetweenRightValue.class)) {
            return (T) betweenRightValue;
        }
        return null;
    }
}
