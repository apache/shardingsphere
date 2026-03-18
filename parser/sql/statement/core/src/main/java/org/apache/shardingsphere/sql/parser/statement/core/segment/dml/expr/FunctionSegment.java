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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Function segment.
 */
@RequiredArgsConstructor
@Getter
public final class FunctionSegment implements ComplexExpressionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String functionName;
    
    private final String text;
    
    private final Collection<ExpressionSegment> parameters = new LinkedList<>();
    
    @Setter
    private OwnerSegment owner;
    
    @Setter
    private WindowItemSegment window;
    
    /**
     * Get window.
     *
     * @return window
     */
    public Optional<WindowItemSegment> getWindow() {
        return Optional.ofNullable(window);
    }
}
