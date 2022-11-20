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

package org.apache.shardingsphere.sql.parser.sql.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Direction type enum.
 */
@RequiredArgsConstructor
@Getter
public enum DirectionType {
    
    NEXT("NEXT"),
    
    PRIOR("PRIOR"),
    
    FIRST("FIRST"),
    
    LAST("LAST"),
    
    ABSOLUTE_COUNT("ABSOLUTE"),
    
    RELATIVE_COUNT("RELATIVE"),
    
    COUNT(""),
    
    ALL("ALL"),
    
    FORWARD("FORWARD"),
    
    FORWARD_COUNT("FORWARD"),
    
    FORWARD_ALL("FORWARD ALL"),
    
    BACKWARD("BACKWARD"),
    
    BACKWARD_COUNT("BACKWARD"),
    
    BACKWARD_ALL("BACKWARD ALL");
    
    private static final Set<DirectionType> ALL_DIRECTION_TYPES = new HashSet<>(Arrays.asList(ALL, FORWARD_ALL, BACKWARD_ALL));
    
    private static final Collection<DirectionType> FORWARD_COUNT_DIRECTION_TYPES =
            new HashSet<>(Arrays.asList(DirectionType.NEXT, DirectionType.COUNT, DirectionType.FORWARD, DirectionType.FORWARD_COUNT));
    
    private static final Collection<DirectionType> BACKWARD_COUNT_DIRECTION_TYPES = new HashSet<>(Arrays.asList(DirectionType.PRIOR, DirectionType.BACKWARD, DirectionType.BACKWARD_COUNT));
    
    private final String name;
    
    /**
     * Is all direction type.
     * 
     * @param directionType direction type
     * @return is all direction type or not
     */
    public static boolean isAllDirectionType(final DirectionType directionType) {
        return ALL_DIRECTION_TYPES.contains(directionType);
    }
    
    /**
     * Is forward count direction type.
     *
     * @param directionType direction type
     * @return is forward count direction type or not
     */
    public static boolean isForwardCountDirectionType(final DirectionType directionType) {
        return FORWARD_COUNT_DIRECTION_TYPES.contains(directionType);
    }
    
    /**
     * Is backward count direction type.
     *
     * @param directionType direction type
     * @return is backward count direction type or not
     */
    public static boolean isBackwardCountDirectionType(final DirectionType directionType) {
        return BACKWARD_COUNT_DIRECTION_TYPES.contains(directionType);
    }
}
