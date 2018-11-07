grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;
/**
 * each statement has a url, 
 * each base url : https://docs.oracle.com/database/121/SQLRF/.
 * no begin statement in oracle
 */
//statements_9014.htm#SQLRF01603
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
    : ALL PRIVILEGES
    | roleName
    | ID *?
    ;

grantees
    : grantee (COMMA grantee)*
    ;
    
grantee
    : userName 
    | roleName 
    | PUBLIC 
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
    : ID *?
    | ALL PRIVILEGES?
    ;
    
onObjectClause
    : ON 
    (
    	schemaName? ID 
       | USER userName ( COMMA userName)*
       | (DIRECTORY | EDITION | MINING MODEL | JAVA (SOURCE | RESOURCE) | SQL TRANSLATION PROFILE) schemaName? ID 
    )
    ;
    
grantRolesToPrograms
    : roleNames TO programUnits
    ;

programUnits
    : programUnit (COMMA programUnit)*
    ;
    
programUnit
    : (FUNCTION | PROCEDURE | PACKAGE) schemaName? ID
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
    : objectPrivilege (COMMA objectPrivilege)* onObjectClause
    FROM grantees
    (CASCADE CONSTRAINTS | FORCE)?
    ;
    
revokeRolesFromPrograms
    : (roleNames | ALL) FROM programUnits
    ;
    
createUser
    : CREATE USER userName IDENTIFIED 
    (BY STRING | (EXTERNALLY | GLOBALLY) ( AS STRING)?)
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

proxyClause
    : (GRANT | REVOKE) CONNECT THROUGH ( ENTERPRISE USERS | userName dbUserProxyClauses?)
    ;
    
dbUserProxyClauses
    : (WITH 
        (
           ROLE  (ALL EXCEPT)? roleNames 
           | NO ROLES
        )
    )?
    (AUTHENTICATION REQUIRED )?
    ;