package org.apache.shardingsphere.agent.plugin.tracing.jaeger.constant;

import io.opentracing.tag.StringTag;

public class JaegerConstants {
    
    public static final String ROOT_SPAN = "jaeger_root_span";
    
    /**
     * Component name of ShardingSphere's open tracing tag.
     */
    public static final String COMPONENT_NAME = "ShardingSphere";
    
    public static final String DB_TYPE_VALUE = "shardingsphere-proxy";
    
    /**
     * Error log tag keys.
     */
    public static final class ErrorLogTagKeys {
    
        public static final String EVENT = "event";
    
        public static final String EVENT_ERROR_TYPE = "error";
    
        public static final String ERROR_KIND = "error.kind";
    
        public static final String MESSAGE = "message";
    }
    
    /**
     * ShardingSphere tags.
     */
    public static final class ShardingSphereTags {
        
        /**
         * The tag to record the bind variables of SQL.
         */
        public static final StringTag DB_BIND_VARIABLES = new StringTag("db.bind_vars");
        
        /**
         * The tag to record the connection count.
         */
        public static final StringTag CONNECTION_COUNT = new StringTag("connection.count");
    }
}
