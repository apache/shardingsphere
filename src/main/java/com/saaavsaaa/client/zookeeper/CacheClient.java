package com.saaavsaaa.client.zookeeper;

/**
 * Created by aaa on 18-4-19.
 */
public class CacheClient extends UsualClient {
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
}
