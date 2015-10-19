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
package org.forgerock.openicf.connectors.sap;

import com.sap.conn.jco.JCoFunction;
import java.util.ArrayList;
import java.util.Arrays;
import org.identityconnectors.framework.common.objects.filter.AndFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterVisitor;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.NotFilter;
import org.identityconnectors.framework.common.objects.filter.OrFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;

/**
 *
 * A SapHrFilterVisitor converts a {@link org.identityconnectors.framework.common.objects.filter.Filter} to
 * input search criteria(s) for the SAP HR BAPI_EMPLOYEE_GETDATA BAPI.
 */
public class SapHrFilterVisitor implements FilterVisitor<Void, JCoFunction> {
    
    public static final SapHrFilterVisitor INSTANCE = new SapHrFilterVisitor();
    
    /**
     * Only these attributes can be used as a search criteria
     */
    private static final ArrayList<String> ALLOWED = new ArrayList<String>(Arrays.asList(
            "BLDING_NO", //Building Number
            "COSTCENTER", //Cost Center
            "DATE", //Date
            "EMPLOYEE_ID",
            "FSTNAME_M", //First Name
            "FST_NAME_K", //First Name (Katakana)
            "FST_NAME_R", //First Name (Romaji)
            "JOBTXT", //Job (short)
            "JOBTXT_LG", //Job (long)
            "LASTNAME_M", //Last Name
            "LST_NAME_K", //Last Name (Katakana)
            "LST_NAME_R", //Last Name (Romaji)
            "ORGTXT", //Organizational Unit (short)
            "ORGTXT_LG", //Organizational Unit (long)
            "PHONE_NO", //Telephone Number
            "POSTXT", //Position (short)
            "POSTXT_LG", //Position (long)
            "ROOM_NO", //Room Number
            "USERID", //Communications ID
            "LIPLATE_NO" //License Plate Number
    ));

    @Override
    public Void visitAndFilter(JCoFunction p, AndFilter filter) {
        filter.getLeft().accept(this, p);
        filter.getRight().accept(this, p);
        return null;
    }

    @Override
    public Void visitContainsFilter(JCoFunction p, ContainsFilter filter) {
        String param = filter.getName().toUpperCase();
        if (!ALLOWED.contains(param)){
            throw new UnsupportedOperationException(param + "is not supported as a search criteria");
        }
        p.getImportParameterList().setValue(param,'*'+filter.getValue()+'*');
        return null;
    }

    @Override
    public Void visitContainsAllValuesFilter(JCoFunction p, ContainsAllValuesFilter filter) {
        throw new UnsupportedOperationException("ContainsAllValuesFilter is not supported.");
    }

    @Override
    public Void visitEqualsFilter(JCoFunction p, EqualsFilter filter) {
        String param = filter.getName().toUpperCase();
        if (!ALLOWED.contains(param)){
            throw new UnsupportedOperationException(param + "is not supported as a search criteria");
        }
        p.getImportParameterList().setValue(param,filter.getAttribute().getValue().get(0).toString());
        return null;
    }

    @Override
    public Void visitExtendedFilter(JCoFunction p, Filter filter) {
        throw new UnsupportedOperationException("Filter type is not supported: "+ filter.getClass());
    }

    @Override
    public Void visitGreaterThanFilter(JCoFunction p, GreaterThanFilter filter) {
        throw new UnsupportedOperationException("GreaterThanFilter is not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitGreaterThanOrEqualFilter(JCoFunction p, GreaterThanOrEqualFilter filter) {
        throw new UnsupportedOperationException("GreaterThanOrEqualFilter is not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitLessThanFilter(JCoFunction p, LessThanFilter filter) {
        throw new UnsupportedOperationException("LessThanFilter is not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitLessThanOrEqualFilter(JCoFunction p, LessThanOrEqualFilter filter) {
        throw new UnsupportedOperationException("LessThanOrEqualFilter is not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitNotFilter(JCoFunction p, NotFilter filter) {
        throw new UnsupportedOperationException("NotFilter is not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitOrFilter(JCoFunction p, OrFilter filter) {
        throw new UnsupportedOperationException("OrFilter is not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitStartsWithFilter(JCoFunction p, StartsWithFilter filter) {
        String param = filter.getName().toUpperCase();
        if (!ALLOWED.contains(param)){
            throw new UnsupportedOperationException(param + "is not supported as a search criteria");
        }
        p.getImportParameterList().setValue(param,filter.getValue()+'*');
        return null;
    }

    @Override
    public Void visitEndsWithFilter(JCoFunction p, EndsWithFilter filter) {
        String param = filter.getName().toUpperCase();
        if (!ALLOWED.contains(param)){
            throw new UnsupportedOperationException(param + "is not supported as a search criteria");
        }
        p.getImportParameterList().setValue(param,'*'+filter.getValue());
        return null;
    }
    
}
