package org.apache.shardingsphere.test.integration.env.database;

/**
 * Embedded database resource.
 */
public interface EmbeddedDatabaseResource {

    /**
     * start embedded database resource.
     */
    void start();

    /**
     * stop embedded database resource.
     */
    void stop();
}
