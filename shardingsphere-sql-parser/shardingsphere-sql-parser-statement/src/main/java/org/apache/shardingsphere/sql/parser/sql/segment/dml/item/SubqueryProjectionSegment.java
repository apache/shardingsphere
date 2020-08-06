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

package org.apache.shardingsphere.sql.parser.sql.segment.dml.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasSegment;

import java.util.Optional;

/**
 * Subquery projection segment.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class SubqueryProjectionSegment implements ProjectionSegment, AliasAvailable {
    
    private final SubquerySegment subquery;
    
    @Setter
    private AliasSegment alias;
    
    @Override
    public Optional<String> getAlias() {
        return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
    }
    
    @Override
    public int getStartIndex() {
        return subquery.getStartIndex();
    }
    
    @Override
    public int getStopIndex() {
        return subquery.getStopIndex();
        // TODO
        // return null == alias ? alias.getStopIndex() : column.getStopIndex();
    }
}
