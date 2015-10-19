/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/CDDL-1.0
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://opensource.org/licenses/CDDL-1.0
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package samples.r3

import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoRepository

import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.spi.operations.CreateOp
import org.identityconnectors.framework.spi.operations.UpdateOp
import org.identityconnectors.framework.spi.operations.DeleteOp
import org.identityconnectors.framework.spi.operations.SyncOp
import org.identityconnectors.framework.spi.operations.AuthenticateOp
import org.identityconnectors.framework.spi.operations.ResolveUsernameOp
import org.identityconnectors.framework.spi.operations.ScriptOnResourceOp
import org.identityconnectors.framework.common.objects.OperationalAttributes
import org.identityconnectors.common.security.GuardedString
    
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.connectors.sap.SapConfiguration

import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.MULTIVALUED
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.REQUIRED
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_CREATABLE
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_UPDATEABLE


// The connector injects the following variables in the script:
// destination: the SAP Jco destination
// repository: the SAP functions repository
// configuration: the connector's configuration
// operation: the operation type ("SCHEMA" here)
// builder: ICF object builder
// log: a handler to the Log facility

def operation = operation as OperationType
def configuration = configuration as SapConfiguration
def builder = builder as ICFObjectBuilder
def destination = destination as JCoDestination
def repository = repository as JCoRepository
def log = log as Log

log.info("Entering {0} Script", operation);

builder.schema({
    objectClass {
        type ObjectClass.ACCOUNT_NAME
        attribute OperationalAttributes.PASSWORD_NAME, GuardedString.class, EnumSet.of(REQUIRED)
        attribute OperationalAttributes.ENABLE_NAME, boolean.class
        attribute OperationalAttributes.ENABLE_DATE_NAME, String.class
        attribute OperationalAttributes.DISABLE_DATE_NAME, String.class
        attribute OperationalAttributes.PASSWORD_EXPIRED_NAME, boolean.class
        attribute OperationalAttributes.LOCK_OUT_NAME, boolean.class
        attributes {
            //Documentation extract taken from SAP system, Copyright (c) SAP AG
            // LOGONDATA: Structure with Logon Data
            // Field          DataType length  Description
            //GLTGV           DATS     000008  Valid from
            //GLTGB           DATS     000008  Valid through
            //USTYP           CHAR     000001  User Type
            //CLASS           CHAR     000012  User group
            //ACCNT           CHAR     000012  Account number
            //TZONE           CHAR     000006  Time Zone
            //LTIME           TIMS     000006  Last Logon Time
            //BCODE           RAW      000008  Initial password
            //CODVN           CHAR     000001  Password Code Vers.
            //PASSCODE        RAW      000020  Password Hash Val.(SAH1, 160 Bit)
            //CODVC           CHAR     000001  Password Code Vers.
            //PWDSALTEDHASH   CHAR     000255  Password Hash Value
            //CODVS           CHAR     000001  Password Code Vers.
            //SECURITY_POLICY CHAR     000040  Security Policy
            "LOGONDATA" Map.class
            // DEFAULTS: Structure with User Defaults
            //Field       Data Type length  Description
            //STCOD       CHAR      000020  Start menu
            //SPLD        CHAR      000004  Output Device
            //SPLG        CHAR      000001
            //SPDB        CHAR      000001  Print immediately
            //SPDA        CHAR      000001  Delete After Output
            //DATFM       CHAR      000001  Date format
            //DCPFM       CHAR      000001  Decimal Format
            //LANGU       LANG      000001  Logon Language
            //CATTKENNZ   CHAR      000001  CATT: Test Status
            //KOSTL       CHAR      000008  Cost center
            //START_MENU  CHAR      000030  Start menu
            //TIMEFM      CHAR      000001  Time Format (12/24h)
            "DEFAULTS" Map.class
            // ADDRESS: Structure with User Address Data
            //Field        Data Type length Description
            //PERS_NO           CHAR 000010 Person number
            //ADDR_NO           CHAR 000010 Address number
            //TITLE_P           CHAR 000030 Title
            //FIRSTNAME         CHAR 000040 First name
            //LASTNAME          CHAR 000040 Last name
            //BIRTH_NAME        CHAR 000040 Name at Birth
            //MIDDLENAME        CHAR 000040 2nd forename
            //SECONDNAME        CHAR 000040 2nd family name
            //FULLNAME          CHAR 000080 Full Name
            //FULLNAME_X        CHAR 000001 Converted
            //TITLE_ACA         CHAR 000020 Academic Title
            //TITLE_ACA2        CHAR 000020 2nd academic title
            //PREFIX1           CHAR 000020 Name prefix
            //PREFIX2           CHAR 000020 2nd name prefix
            //TITLE_SPPL        CHAR 000020 Name supplement
            //NICKNAME          CHAR 000040 Nickname/name used
            //INITIALS          CHAR 000010 Initials
            //NAMEFORMAT        CHAR 000002 Format name
            //NAMCOUNTRY        CHAR 000003 Country for format
            //LANGU_P           LANG 000001 Language Key
            //LANGUP_ISO        CHAR 000002 Lang. (ISO 639)
            //SORT1_P           CHAR 000020 Search Term 1
            //SORT2_P           CHAR 000020 Search Term 2
            //DEPARTMENT        CHAR 000040 Department
            //FUNCTION          CHAR 000040 Function
            //BUILDING_P        CHAR 000010 Building code
            //FLOOR_P           CHAR 000010 Floor
            //ROOM_NO_P         CHAR 000010 Room Number
            //INITS_SIG         CHAR 000010 Short name
            //INHOUSE_ML        CHAR 000010 Internal mail
            //COMM_TYPE         CHAR 000003 Comm. Method
            //TITLE             CHAR 000030 Title
            //NAME              CHAR 000040 Name
            //NAME_2            CHAR 000040 Name 2
            //NAME_3            CHAR 000040 Name 3
            //NAME_4            CHAR 000040 Name 4
            //C_O_NAME          CHAR 000040 c/o
            //CITY              CHAR 000040 City
            //DISTRICT          CHAR 000040 District
            //CITY_NO           CHAR 000012 City Code
            //DISTRCT_NO        CHAR 000008 District
            //CHCKSTATUS        CHAR 000001 Test stat./City file
            //POSTL_COD1        CHAR 000010 Postal Code
            //POSTL_COD2        CHAR 000010 PO box postal code
            //POSTL_COD3        CHAR 000010 Company postal code
            //PO_BOX            CHAR 000010 PO Box
            //PO_BOX_CIT        CHAR 000040 PO Box City
            //PBOXCIT_NO        CHAR 000012 City Code
            //DELIV_DIS         CHAR 000015 Delivery District
            //TRANSPZONE        CHAR 000010 Transportation zone
            //STREET            CHAR 000060 Street
            //STREET_NO         CHAR 000012 Street Code
            //STR_ABBR          CHAR 000002 Street Abbreviation
            //HOUSE_NO          CHAR 000010 House Number
            //HOUSE_NO2         CHAR 000010 Supplement
            //STR_SUPPL1        CHAR 000040 Street 2
            //STR_SUPPL2        CHAR 000040 Street 3
            //STR_SUPPL3        CHAR 000040 Street 4
            //LOCATION          CHAR 000040 Street 5
            //BUILDING          CHAR 000010 Building code
            //FLOOR             CHAR 000010 Floor
            //ROOM_NO           CHAR 000010 Room Number
            //COUNTRY           CHAR 000003 Country Key
            //COUNTRYISO        CHAR 000002 ISO code
            //LANGU             LANG 000001 Language Key
            //LANGU_ISO         CHAR 000002 Lang. (ISO 639)
            //REGION            CHAR 000003 Region
            //SORT1             CHAR 000020 Search Term 1
            //SORT2             CHAR 000020 Search Term 2
            //TIME_ZONE         CHAR 000006 Time zone
            //TAXJURCODE        CHAR 000015 Tax Jurisdiction
            //ADR_NOTES         CHAR 000050 Notes
            //TEL1_NUMBR        CHAR 000030 Telephone
            //TEL1_EXT          CHAR 000010 Extension
            //FAX_NUMBER        CHAR 000030 Fax
            //FAX_EXTENS        CHAR 000010 Extension
            //E_MAIL            CHAR 000241 E-Mail Address
            //BUILD_LONG        CHAR 000020 Building Code
            //REGIOGROUP        CHAR 000008 Reg. Struct. Grp.
            //HOME_CITY         CHAR 000040 Different City
            //HOMECITYNO        CHAR 000012 City Code
            //PCODE1_EXT        CHAR 000010 Postl Code Extension
            //PCODE2_EXT        CHAR 000010 Postl Code Extension
            //PCODE3_EXT        CHAR 000010 Postl Code Extension
            //PO_W_O_NO         CHAR 000001 PO Box w/o no.
            //PO_BOX_REG        CHAR 000003 PO Box Region
            //POBOX_CTRY        CHAR 000003 PO box country
            //PO_CTRYISO        CHAR 000002 ISO code
            //DONT_USE_S        CHAR 000004 Undeliverable
            //DONT_USE_P        CHAR 000004 Undeliverable
            //HOUSE_NO3         CHAR 000010 House Number Range
            //LANGU_CR_P        LANG 000001 Creation language
            //LANGUCPISO        CHAR 000002 Lang. (ISO 639)
            //PO_BOX_LOBBY      CHAR 000040 PO Box Lobby
            //DELI_SERV_TYPE    CHAR 000004 Type of Delivry Service
            //DELI_SERV_NUMBER  CHAR 000010 Number of Delivery Service
            //COUNTY_CODE       CHAR 000008 County code
            //COUNTY            CHAR 000040 County
            //TOWNSHIP_CODE     CHAR 000008 Township code
            //TOWNSHIP          CHAR 000040 Township
            "ADDRESS" Map.class
            // COMPANY: Company to which a user is assigned
            "COMPANY" Map.class
            // REF_USER: User Name of the Reference User
            "REF_USER" Map.class
            // ALIAS: User Name Alias
            "ALIAS" Map.class
            // UCLASS: License-Related User Classification
            //Field        Data Type length Description
            //LIC_TYPE          CHAR 000002 Contractual User Type ID
            //SPEC_VERS         CHAR 000002 Special Version ID
            //COUNTRY_SURCHARGE DEC  000003 Country Surcharge
            //SUBSTITUTE_FROM   DATS 000008 From Date
            //SUBSTITUTE_UNTIL  DATS 000008 To date
            //SYSID             CHAR 000008 SAP System ID
            //CLIENT            CLNT 000003 Client
            //BNAME_CHARGEABLE  CHAR 000012 Chargeable User
            "UCLASS" Map.class
            // LASTMODIFIED: User: Last Change (Date and Time)
            //Field   Data Type length Description
            //MODDATE      DATS 000008 Modification date
            //MODTIME      TIMS 000006 Modification time
            //MODIFIER     CHAR 000012 Changed By
            "LASTMODIFIED" Map.class
            // ISLOCKED: User Lock
            //Field       Data Type length Description
            //WRNG_LOGON       CHAR 000001 Status of User Lock
            //LOCAL_LOCK       CHAR 000001 Status of User Lock
            //GLOB_LOCK        CHAR 000001 Status of User Lock
            //NO_USER_PW       CHAR 000001 Status of User Lock
            "ISLOCKED" Map.class
            // IDENTITY: Person Assignment of an Identity
            // ADMINDATA: User: Administration Data
            //Field   Data Type length Description
            //ANAME        CHAR 000012 Creator of User Master Record
            //ERDAT        DATS 000008 Creation Date of User Master
            //TRDAT        DATS 000008 Last Logon Date
            "ADMINDATA" Map.class
            // PROFILES : User: Profile Transfer Structure
            //Field      Data Type length Description
            //BAPIPROF        CHAR 000012 Profile
            //BAPIPTEXT       CHAR 000060 Text
            //BAPITYPE        CHAR 000001 Type
            //BAPIAKTPS       CHAR 000001 Version
            "PROFILES" Map.class, MULTIVALUED
            // ACTIVITYGROUPS: Activity Groups
            //Field      Data Type length Description
            //AGR_NAME        CHAR 000030 Role
            //FROM_DAT        DATS 000008 From Date
            //TO_DAT          DATS 000008 To Date
            //AGR_TEXT        CHAR 000080 Short Description
            //ORG_FLAG        CHAR 000001 Indirect User-Role Assignment
            "ACTIVITYGROUPS" Map.class, MULTIVALUED
            // ADDTEL: Telephone Numbers
            //Field      Data Type length Description
            //COUNTRY         CHAR 000003 Country
            //COUNTRYISO      CHAR 000002 ISO code
            //STD_NO          CHAR 000001 Standard No.
            //TELEPHONE       CHAR 000030 Telephone
            //EXTENSION       CHAR 000010 Extension
            //TEL_NO          CHAR 000030 Telephone number
            //CALLER_NO       CHAR 000030 Caller number
            //STD_RECIP       CHAR 000001 SMS-Enab.
            //R_3_USER        CHAR 000001 Mobile phone
            //HOME_FLAG       CHAR 000001 Home address
            //CONSNUMBER      NUMC 000003 Sequence Number
            //ERRORFLAG       CHAR 000001 Errors occurred
            //FLG_NOUSE       CHAR 000001 Do Not Use Communication Number
            //VALID_FROM      CHAR 000014 Valid From
            //VALID_TO        CHAR 000014 Valid Through
            "ADDTEL" Map.class, MULTIVALUED
        }
        disable SyncOp.class, AuthenticateOp.class, ResolveUsernameOp.class, ScriptOnResourceOp
    }
    objectClass {
        type 'profile'
        attributes {
            //Field      Data Type length Description
            //BAPIPROF   CHAR 000012 Profile
            "BAPIPROF" String.class
            //BAPIPTEXT  CHAR 000060 Text
            "BAPIPTEXT" String.class
            //BAPITYPE   CHAR 000001 Type
            "BAPITYPE" String.class
            //BAPIAKTPS  CHAR 000001 Version
            "BAPIAKTPS" String.class
        }
        disable CreateOp.class, UpdateOp.class, DeleteOp.class, SyncOp.class, AuthenticateOp.class, ResolveUsernameOp.class, ScriptOnResourceOp
    }
    objectClass {
        type 'activity_group'
        attributes {
            //Dictionary fields for SAP Table AGR_DEFINE
            //Field       Data Type length Description
            //MANDT       CLNT 000003 Client ID
            "MANDT" String.class
            //AGR_NAME    CHAR 000030 Role
            "AGR_NAME" String.class
            //PARENT_AGR  CHAR 000030 Parent Role
            //CREATE_USR  CHAR 000012 User Name
            "CREATE_USR" String.class
            //CREATE_DAT  DATS 000008 Date
            "CREATE_DAT" String.class
            //CREATE_TIM  TIMS 000006 Time
            "CREATE_TIM" String.class
            //CREATE_TMP  DEC	 000015
            "CREATE_TMP" String.class
            //CHANGE_USR  CHAR 000012 User Name
            "CHANGE_USR" String.class
            //CHANGE_DAT  DATS 000008 Date
            "CHANGE_DAT" String.class
            //CHANGE_TIM  TIMS 000006 Time
            "CHANGE_TIM" String.class
            //CHANGE_TMP  DEC	 000015
            "CHANGE_TMP" String.class
            //ATTRIBUTES  CHAR 000010 Attributes
            "ATTRIBUTES" String.class
        }
        disable CreateOp.class, UpdateOp.class, DeleteOp.class, SyncOp.class, AuthenticateOp.class, ResolveUsernameOp.class, ScriptOnResourceOp
    }
    objectClass {
        type 'role'
        attributes {
            //FieldData Type length Description
            //AGR_NAME  CHAR 000030 Role
            "AGR_NAME" String.class
            //TEXT      CHAR 000080 Short Description
            "TEXT" String.class
        }
        disable CreateOp.class, UpdateOp.class, DeleteOp.class, SyncOp.class, AuthenticateOp.class, ResolveUsernameOp.class, ScriptOnResourceOp
    }
})