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

package org.apache.shardingsphere.transaction.base.seata.at;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Seata xid context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SeataXIDContext {
    
    private static final TransmittableThreadLocal<String> XID = new TransmittableThreadLocal<>();
    
    /**
     * Judge whether xid is empty or not.
     *
     * @return whether xid is empty or not
     */
    public static boolean isEmpty() {
        return null == XID.get();
    }
    
    /**
     * Get xid.
     * 
     * @return xid
     */
    public static String get() {
        return XID.get();
    }
    
    /**
     * Set xid.
     * 
     * @param xid xid
     */
    public static void set(final String xid) {
        XID.set(xid);
    }
    
    /**
     * Remove xid.
     */
    public static void remove() {
        XID.remove();
    }
}
