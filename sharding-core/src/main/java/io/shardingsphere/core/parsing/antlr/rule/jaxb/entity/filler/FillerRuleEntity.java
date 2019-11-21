/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.filler;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Filler rule entity for JAXB.
 *
 * @author zhangliang
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public final class FillerRuleEntity {
    
    @XmlAttribute(name = "sql-segment-class", required = true)
    private String sqlSegmentClass;
    
    @XmlAttribute(name = "filler-class", required = true)
    private String fillerClass;
}
