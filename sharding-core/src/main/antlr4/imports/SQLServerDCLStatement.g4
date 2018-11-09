grammar SQLServerDCLStatement;

import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

grant
    : grantGeneral | grantDW
    ;
    
grantGeneral
    : GRANT (ALL PRIVILEGES? | permissionOnColumns ( COMMA permissionOnColumns)*)
    (ON (ID COLON COLON)? ID)? TO ids (WITH GRANT OPTION)? (AS ID)?
    ;

permissionOnColumns
    : permission columnList?
    ;

permission
    : ID *?
    ;

grantDW
    : GRANT permission (COMMA permission)* (ON (classType COLON COLON)? ID)?   
    TO ids (WITH GRANT OPTION)?
    ;

classType
    : LOGIN | DATABASE | OBJECT | ROLE | SCHEMA | USER  
    ;

revoke
    : revokeGeneral | revokeDW
    ;

revokeGeneral
    : REVOKE (GRANT OPTION FOR)? ((ALL PRIVILEGES?)? | permissionOnColumns)
    (ON (ID COLON COLON)? ID)? (TO | FROM) ids (CASCADE)? (AS ID)?
    ;

revokeDW
    : REVOKE permissionWithClass (FROM | TO)? ids CASCADE?
    ;

permissionWithClass
    : permission (COMMA permission)* (ON (classType COLON COLON)? ID)?
    ;

deny
    : DENY permissionWithClass TO ids CASCADE? (AS ID)?
    ;

createUser2
    : CREATE USER userName
    (
         windowsPrincipal (WITH  optionsList (COMMA optionsList)*)?
         | userName WITH PASSWORD EQ_ STRING (COMMA  optionsList)*
         | ID FROM EXTERNAL PROVIDER
    )
    ;

createUser3
    : CREATE USER userName
    (
        ID (( FOR | FROM ) LOGIN ID)?
        | userName ( FOR | FROM ) LOGIN ID
    )
    ;

createUser4
    : CREATE USER userName
    (
         WITHOUT LOGIN (WITH  limitedOptionsList  (COMMA limitedOptionsList)*)?
        | (FOR | FROM ) CERTIFICATE ID
        | (FOR | FROM) ASYMMETRIC KEY ID
    )
    ;

optionsList
    : DEFAU_SCHEMA EQ_ schemaName
    | DEFAU_LANGUAGE EQ_ ( NONE | ID)
    | SID EQ_ ID
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)
    ;

limitedOptionsList
    :  DEFAU_SCHEMA EQ_ schemaName | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS EQ_ (ON | OFF)?
    ;