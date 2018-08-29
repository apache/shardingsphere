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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.zookeeper.KeeperException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper connection check.
 *
 * @author lidongbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Connection {
    
    //is need reset
    private static final Map<Integer, Boolean> EXCEPTION_RESETS = new ConcurrentHashMap<>();

    static {
        EXCEPTION_RESETS.put(KeeperException.Code.SESSIONEXPIRED.intValue(), true);
        EXCEPTION_RESETS.put(KeeperException.Code.SESSIONMOVED.intValue(), true);
        EXCEPTION_RESETS.put(KeeperException.Code.CONNECTIONLOSS.intValue(), false);
        EXCEPTION_RESETS.put(KeeperException.Code.OPERATIONTIMEOUT.intValue(), false);
    }
    
    /**
     * Need retry.
     *
     * @param keeperException keeper exception
     * @return need retry
     */
    public static boolean needRetry(final KeeperException keeperException) {
        return EXCEPTION_RESETS.containsKey(keeperException.code().intValue());
    }
    
    /**
     * Need reset.
     *
     * @param keeperException keeper exception
     * @return need reset
     * @throws KeeperException zookeeper exception
     */
    public static boolean needReset(final KeeperException keeperException) throws KeeperException {
        int code = keeperException.code().intValue();
        if (!EXCEPTION_RESETS.containsKey(code)) {
            throw keeperException;
        }
        return EXCEPTION_RESETS.get(code);
    }
}
