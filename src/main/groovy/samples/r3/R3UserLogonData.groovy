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

import com.sap.conn.jco.JCoFunction

// A simple class used to set the ABAP Structure BAPILOGOND values
// See: http://www.sapdatasheet.org/abap/tabl/bapilogond.html
class R3UserLogonData {
    private static final Set<String> FIELDS = new HashSet<String>(Arrays.asList(
            "GLTGV",        //User valid from ("YYYY-MM-DD")
            "GLTGB",        //User valid to ("YYYY-MM-DD")
            // User type, See: http://help.sap.com/saphelp_nw70/helpdata/en/52/67119e439b11d1896f0000e8322d00/content.htm
            "USTYP"        //User type: Dialog (A), System (B), Communications (C), Service (S),Reference (L)
            ))
        
    public static void setValues(JCoFunction function, Map<String,String> values, boolean update){
        if (values != null && values.size() != 0){
            values.each{
                k, v ->
                k = k.toUpperCase()
                if (FIELDS.contains(k)) {
                    function.getImportParameterList().getStructure("LOGONDATA").setValue(k,v)
                    if (update) function.getImportParameterList().getStructure("LOGONDATAX").setValue(k,"X")
                }
            }
        }
    }
	
}

