grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;

grant
    : GRANT
    (
        (grantSystemPrivileges | grantObjectPrivilegeClause) (CONTAINER EQ_ (CURRENT | ALL))?
        | grantRolesToPrograms
    )
    ;
    
grantSystemPrivileges
    : systemObjects TO (grantees | granteeIdentifiedBy) (WITH (ADMIN | DELEGATE) OPTION)?
    ;
    
systemObjects
    : systemObject(COMMA systemObject)*
    ;
    
systemObject
    : ALL PRIVILEGES | roleName | ID *?
    ;
    
grantees
    : grantee (COMMA grantee)*
    ;
    
grantee
    : userName | roleName | PUBLIC 
    ;
    
granteeIdentifiedBy
    : userNames IDENTIFIED BY STRING (COMMA STRING)*
    ;
    
grantObjectPrivilegeClause
    : grantObjectPrivilege (COMMA grantObjectPrivilege)* onObjectClause
    TO grantees (WITH HIERARCHY OPTION)?(WITH GRANT OPTION)?
    ;
    
grantObjectPrivilege
    : objectPrivilege columnList? 
    ;
    
objectPrivilege
    : ID *? | ALL PRIVILEGES?
    ;
    
onObjectClause
    : ON 
    (
       tableName 
       | USER userName ( COMMA userName)*
       | (DIRECTORY | EDITION | MINING MODEL | JAVA (SOURCE | RESOURCE) | SQL TRANSLATION PROFILE) tableName 
    )
    ;
    
grantRolesToPrograms
    : roleNames TO programUnits
    ;
    
programUnits
    : programUnit (COMMA programUnit)*
    ;
    
programUnit
    : (FUNCTION | PROCEDURE | PACKAGE) tableName
    ;
    
revoke
    : REVOKE
     (
         (revokeSystemPrivileges | revokeObjectPrivileges) (CONTAINER EQ_ (CURRENT | ALL))?
         | revokeRolesFromPrograms 
     )
    ;
    
revokeSystemPrivileges
    : systemObjects FROM
    ;
    
revokeObjectPrivileges
    : objectPrivilege (COMMA objectPrivilege)* onObjectClause FROM grantees (CASCADE CONSTRAINTS | FORCE)?
    ;
    
revokeRolesFromPrograms
    : (roleNames | ALL) FROM programUnits
    ;
    
createUser
    : CREATE USER userName IDENTIFIED 
    (BY ID | (EXTERNALLY | GLOBALLY) ( AS STRING)?)
    ( 
        DEFAULT TABLESPACE ID
        | TEMPORARY TABLESPACE ID
        | (QUOTA (sizeClause | UNLIMITED) ON ID)
        | PROFILE ID
        | PASSWORD EXPIRE
        | ACCOUNT (LOCK | UNLOCK)
        | ENABLE EDITIONS
        | CONTAINER EQ_ (CURRENT | ALL)
    )*
    ;
    
sizeClause
    : NUMBER ID?
    ;
    
alterUser
    : ALTER USER
    ( 
        userName
        ( 
            IDENTIFIED (BY ID (REPLACE STRING)? | (EXTERNALLY | GLOBALLY) ( AS STRING)?)
            | DEFAULT TABLESPACE ID
            | TEMPORARY TABLESPACE ID
            | QUOTA (sizeClause | UNLIMITED) ON ID
            | PROFILE ID
            | PASSWORD EXPIRE
            | ACCOUNT (LOCK | UNLOCK)
            | ENABLE EDITIONS (FOR ids)? FORCE?
            | CONTAINER EQ_ (CURRENT | ALL)
            | DEFAULT ROLE (roleNames| ALL (EXCEPT roleNames)?| NONE)
            | ID
        )*
        | userNames proxyClause
    ) 
    ;
    
containerDataClause
    : (
          SET CONTAINER_DATA EQ_ ( ALL | DEFAULT | idList )
          | (ADD |REMOVE) CONTAINER_DATA EQ_ idList
    )
    (FOR tableName)?
    ;
    
proxyClause
    : (GRANT | REVOKE) CONNECT THROUGH ( ENTERPRISE USERS | userName dbUserProxyClauses?)
    ;
    
dbUserProxyClauses
    : (WITH (ROLE (ALL EXCEPT)? roleNames | NO ROLES))?
    (AUTHENTICATION REQUIRED )?
    ;
    
dropUser
    : DROP USER userName CASCADE? 
    ;
    
createRole
    : CREATE ROLE roleName
    ( 
        NOT IDENTIFIED
        | IDENTIFIED (BY ID | USING tableName | EXTERNALLY | GLOBALLY)
    )? 
    (CONTAINER EQ_ (CURRENT | ALL))? 
    ;
    
alterRole
    : ALTER ROLE roleName
    (NOT IDENTIFIED | IDENTIFIED (BY ID | USING tableName | EXTERNALLY | GLOBALLY))
    (CONTAINER EQ_ (CURRENT | ALL))? 
    ;
    
dropRole
    : DROP ROLE roleName 
    ;
