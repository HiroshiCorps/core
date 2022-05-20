/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.utils;

public enum DataBaseType {
    SQL("SQL"),
    REDIS("REDIS");

    public final String sqlBase;

    DataBaseType(String sqlBase) {
        this.sqlBase = sqlBase;
    }
}
