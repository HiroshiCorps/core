/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.result;

public class ResultSetElement {

    private final boolean signed;
    private Object value;

    public ResultSetElement(Object value, boolean isSigned) {
        this.value = value;
        this.signed = isSigned;
    }

    public boolean isSigned() {
        return this.signed;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}