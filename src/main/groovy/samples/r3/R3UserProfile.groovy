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
 * Copyright 2015 ForgeRock AS.
 */

package samples.r3

import com.sap.conn.jco.JCoFunction
import com.sap.conn.jco.JCoTable

class R3UserProfile {
    // BAPI_USER_PROFILES_ASSIGN : Change Local Profile Assignments for a User
    //See http://www.sapdatasheet.org/abap/func/bapi_user_profiles_assign.html
    
    // BAPI_USER_PROFILES_DELETE : Delete Local Profile Assignments for a User
    //See http://www.sapdatasheet.org/abap/func/bapi_user_profiles_delete.html
    
    private static final String NAME = "BAPIPROF"
    
    public static void setValues(JCoFunction function, List<Map> values) {
        if ((values != null) && (!values.empty)){
            JCoTable table = function.getTableParameterList().getTable("PROFILES")
            values.each{
                if ((it[NAME] != null) && (!"".equals(it[NAME]))){
                    table.appendRow()
                    table.setValue(NAME,it[NAME])
                }
            }
        }
    }
}

