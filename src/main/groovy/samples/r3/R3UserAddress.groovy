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

import static org.identityconnectors.common.security.SecurityUtil.decrypt

// A simple class used to set the ABAP Structure BAPIADDR3 values
// See: http://www.sapdatasheet.org/abap/tabl/bapiaddr3.html
class R3UserAddress {
    private static final Set<String> FIELDS = new HashSet<String>(Arrays.asList(
            "FIRSTNAME",
            "LASTNAME",
            "BIRTH_NAME",
            "MIDDLENAME",
            "SECONDNAME",
            "FULLNAME",
            "TEL1_NUMBR", //First telephone no.: dialling code+number
            "E_MAIL",
            "TITLE_ACA1",
            "TITLE_ACA2",
            "PREFIX1",
            "PREFIX2",
            "TITLE_SPPL",
            "NICKNAME",
            "INITIALS",
            "DEPARTMENT",
            "FUNCTION",
            "BUILDING_P"
            ))
    
    public static void setValues(JCoFunction function, Map<String,String> values, boolean update){
        if (values != null && values.size() != 0){
            values.each{
                k, v ->
                k = k.toUpperCase()
                if (FIELDS.contains(k)) {
                    function.getImportParameterList().getStructure("ADDRESS").setValue(k,v)
                    if (update) function.getImportParameterList().getStructure("ADDRESSX").setValue(k,"X")
                }
            }
        }
    }
}

