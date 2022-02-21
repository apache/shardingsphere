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

package org.apache.shardingsphere.infra.binder.segment.select.projection.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.sql.common.constant.ParameterMarkerType;

import java.util.Optional;

/**
 * ParameterMarker projection.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class ParameterMarkerProjection implements Projection {

    private final int parameterMarkerIndex;
    
    private final ParameterMarkerType parameterMarkerType;

    private final String alias;

    @Override
    public String getExpression() {
        return String.valueOf(parameterMarkerIndex);
    }

    @Override
    public String getColumnLabel() {
        return getAlias().orElse(String.valueOf(parameterMarkerIndex));
    }

    @Override
    public Optional<String> getAlias() {
        return Optional.ofNullable(alias);
    }

    /**
     * Get expression with alias.
     *
     * @return expression with alias
     */
    public String getExpressionWithAlias() {
        return getExpression() + (null == alias ? "" : " AS " + alias);
    }
}
