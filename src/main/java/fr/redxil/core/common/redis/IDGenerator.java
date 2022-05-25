/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.redis;

import fr.redxil.api.common.API;
import fr.redxil.core.common.data.IDDataValue;

import java.util.HashMap;

public class IDGenerator {

    public static HashMap<String, Long> idMap = new HashMap<>();

    public static int generateINTID(IDDataValue idv) {
        return Long.valueOf(generateLONGID(idv)).intValue();
    }

    public static long generateLONGID(IDDataValue idv) {
        if (API.getInstance().isOnlineMod())
            return API.getInstance().getRedisManager().getRedissonClient().getIdGenerator(idv.getLocation()).nextId();
        else {
            if (idMap.containsKey(idv.getLocation())) {
                long newID = idMap.get(idv.getLocation()) + 1;
                idMap.replace(idv.getLocation(), newID);
                return newID;
            } else {
                idMap.put(idv.getLocation(), 1L);
                return 1L;
            }
        }
    }

    public static void resetID(IDDataValue idv) {
        API.getInstance().getRedisManager().getRedissonClient().getBucket(idv.getLocation()).delete();
    }

}
