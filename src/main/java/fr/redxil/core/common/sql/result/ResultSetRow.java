/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.result;

import java.util.HashMap;

public class ResultSetRow {

    private final HashMap<String, ResultSetElement> columns;

    public ResultSetRow(HashMap<String, ResultSetElement> columns) {
        this.columns = columns;
    }

    public HashMap<String, ResultSetElement> getColumns() {
        return this.columns;
    }

}
