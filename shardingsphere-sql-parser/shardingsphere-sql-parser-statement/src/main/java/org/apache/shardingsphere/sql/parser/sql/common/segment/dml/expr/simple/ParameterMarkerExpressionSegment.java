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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.common.constant.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;

import java.util.Optional;

/**
 * Parameter marker expression segment.
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class ParameterMarkerExpressionSegment implements SimpleExpressionSegment, ProjectionSegment, AliasAvailable, ParameterMarkerSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final int parameterMarkerIndex;
    
    private final ParameterMarkerType parameterMarkerType;
    
    @Setter
    private AliasSegment alias;
    
    public ParameterMarkerExpressionSegment(final int startIndex, final int stopIndex, final int parameterMarkerIndex) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.parameterMarkerIndex = parameterMarkerIndex;
        this.parameterMarkerType = ParameterMarkerType.QUESTION;
    }

    @Override
    public Optional<String> getAlias() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
}
