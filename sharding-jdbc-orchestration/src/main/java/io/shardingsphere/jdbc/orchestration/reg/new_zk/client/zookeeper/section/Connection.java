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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section;

import org.apache.zookeeper.KeeperException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author lidongbo
 */
public class Connection {
    //is need reset
    private static final Map<Integer, Boolean> exceptionResets = new ConcurrentHashMap<>();

    static {
        exceptionResets.put(KeeperException.Code.SESSIONEXPIRED.intValue(), true);
        exceptionResets.put(KeeperException.Code.SESSIONMOVED.intValue(), true);
        exceptionResets.put(KeeperException.Code.CONNECTIONLOSS.intValue(), false);
        exceptionResets.put(KeeperException.Code.OPERATIONTIMEOUT.intValue(), false);
    }

    public static boolean needReset(KeeperException e) throws KeeperException {
        int code = e.code().intValue();
        if (!exceptionResets.containsKey(code)){
            throw e;
        }
        return exceptionResets.get(code);
    }
}
