package org.apache.shardingsphere.distsql.parser.subject;

public enum DistSQLSubjectTypeEnum {
    
    DEFAULT,
    RESOURCE,
    SHARDING,
    READWRITE_SPLITTING,
    DB_DISCOVERY,
    ENCRYPT,
    SHADOW,
    SINGLE_TABLE,
    SCALING;
    
    /**
     * Returns the dist subject of the specified variable name.
     *
     * @param subjectName subject name
     * @return subject constant
     */
    public static DistSQLSubjectTypeEnum getValueOf(final String subjectName) {
        try {
            return valueOf(subjectName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException(String.format("Unsupported subject `%s`", subjectName));
        }
    }
}