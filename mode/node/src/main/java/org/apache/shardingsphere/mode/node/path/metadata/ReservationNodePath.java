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

package org.apache.shardingsphere.mode.node.path.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Reservation node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReservationNodePath {
    
    private static final String ROOT_NODE = "/reservation";
    
    private static final String WORKER_ID_NODE = "worker_id";
    
    /**
     * Get worker id reservation path.
     *
     * @param workerId worker id
     * @return worker id reservation path
     */
    public static String getWorkerIdReservationPath(final int workerId) {
        return String.join("/", ROOT_NODE, WORKER_ID_NODE, String.valueOf(workerId));
    }
}
