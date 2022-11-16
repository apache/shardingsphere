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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;

/**
 * Column projection segment.
 */
@Getter
@Setter
@ToString
public final class DatetimeProjectionSegment implements ProjectionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final ExpressionSegment left;
    
    private final ExpressionSegment right;
    
    private final String text;
    
    public DatetimeProjectionSegment(final int startIndex, final int stopIndex, final ExpressionSegment left, final ExpressionSegment right, final String text) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.left = left;
        this.right = right;
        this.text = text;
    }
    
    public DatetimeProjectionSegment(final int startIndex, final int stopIndex, final ExpressionSegment left, final String text) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.left = left;
        this.right = null;
        this.text = text;
    }
}
