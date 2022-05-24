/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.utils;

import fr.redxil.api.common.API;
import org.redisson.api.RBucket;

public class DataReminder<K> {

    final String location;
    K data;

    private DataReminder(String location, K base) {
        this.location = location;
        this.data = base;
    }

    public static <K> DataReminder<K> generateReminder(String location, K base) {
        return new DataReminder<>(location, base);
    }

    public K getData() {
        if (API.getInstance().isOnlineMod()) {
            RBucket<K> object = API.getInstance().getRedisManager().getRedissonClient().getBucket(location);
            return object.get();
        } else return data;
    }

    public DataReminder<K> setData(K data) {
        if (API.getInstance().isOnlineMod()) {
            RBucket<K> object = API.getInstance().getRedisManager().getRedissonClient().getBucket(location);
            object.set(data);
        } else this.data = data;
        return this;
    }

}
