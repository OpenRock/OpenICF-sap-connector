/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015-2016 ForgeRock AS.
 */

package samples.r3

import com.sap.conn.jco.JCoTable
import com.sap.conn.jco.JCoFunction

import org.identityconnectors.common.logging.Log

class R3UserActivityGroup {
    // BAPI_USER_ACTGROUPS_ASSIGN : Change entire activity group assignment
    //See http://www.sapdatasheet.org/abap/func/bapi_user_actgroups_assign.html
        
    // BAPI_USER_ACTGROUPS_DELETE : Delete Local Role Assignments for a User
    //See http://www.sapdatasheet.org/abap/func/bapi_user_actgroups_delete.html
    
    private static final String END_DATE = "9999-12-31"
    private static final String NAME = "AGR_NAME" // Name of the Activity Group/Role
    private static final String FROM = "FROM_DAT" // valid from ("YYYY-MM-DD")
    private static final String TO = "TO_DAT" // valid to ("YYYY-MM-DD")
    
    public static void setValues(JCoFunction function, List<Map> values) {
        if ((values != null) && (!values.empty)){
            def today = new Date().format('yyyy-MM-dd');
            JCoTable table = function.getTableParameterList().getTable("ACTIVITYGROUPS")
            values.each{
                if ((it[NAME] != null) && (!"".equals(it[NAME]))){
                    table.appendRow()
                    table.setValue(NAME,it[NAME])
                    table.setValue(FROM, it[FROM] == null ? today : it[FROM])
                    table.setValue(TO, it[TO] == null ? END_DATE : it[TO])
                }
            }
        }
    }
}

