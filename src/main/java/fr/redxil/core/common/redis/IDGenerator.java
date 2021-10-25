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

public class IDGenerator {

    public static int generateINTID(IDDataValue idv) {
        return (int) API.get().getRedisManager().getRedissonClient().getIdGenerator(idv.getLocation()).nextId();
    }

    public static long generateLONGID(IDDataValue idv) {
        return API.get().getRedisManager().getRedissonClient().getIdGenerator(idv.getLocation()).nextId();
    }

    public static void resetID(IDDataValue idv) {
        API.get().getRedisManager().getRedissonClient().getBucket(idv.getLocation()).delete();
    }

}
