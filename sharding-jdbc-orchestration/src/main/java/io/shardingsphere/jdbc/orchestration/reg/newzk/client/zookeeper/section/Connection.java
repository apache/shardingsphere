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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section;

import org.apache.zookeeper.KeeperException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author lidongbo
 */
public class Connection {
    //is need reset
    private static final Map<Integer, Boolean> EXCEPTION_RESETS = new ConcurrentHashMap<>();

    static {
        EXCEPTION_RESETS.put(KeeperException.Code.SESSIONEXPIRED.intValue(), true);
        EXCEPTION_RESETS.put(KeeperException.Code.SESSIONMOVED.intValue(), true);
        EXCEPTION_RESETS.put(KeeperException.Code.CONNECTIONLOSS.intValue(), false);
        EXCEPTION_RESETS.put(KeeperException.Code.OPERATIONTIMEOUT.intValue(), false);
    }
    
    /**
     * need reset.
     *
     * @param e e
     * @return need reset
     * @throws KeeperException Zookeeper Exception
     */
    public static boolean needReset(final KeeperException e) throws KeeperException {
        int code = e.code().intValue();
        if (!EXCEPTION_RESETS.containsKey(code)) {
            throw e;
        }
        return EXCEPTION_RESETS.get(code);
    }
}
