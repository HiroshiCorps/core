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

public interface DataReminder<K> {

    static <K> DataReminder<K> generateReminder(String location, K base) {
        if (API.getInstance().isOnlineMod())
            return new OnlineReminder<>(location, base);
        else return new OfflineReminder<>(location, base);
    }

    String getLocation();

    K getData();

    DataReminder<K> setData(K data);

    class OnlineReminder<K> implements DataReminder<K> {

        final String location;

        private OnlineReminder(String location, K base) {
            this.location = location;
        }

        @Override
        public String getLocation() {
            return this.location;
        }

        @Override
        public K getData() {
            RBucket<K> object = API.getInstance().getRedisManager().getRedissonClient().getBucket(location);
            return object.get();
        }

        @Override
        public DataReminder<K> setData(K data) {
            RBucket<K> object = API.getInstance().getRedisManager().getRedissonClient().getBucket(location);
            object.set(data);
            return this;
        }

    }

    class OfflineReminder<K> implements DataReminder<K> {

        final String location;
        K data;

        private OfflineReminder(String location, K base) {
            this.location = location;
            this.data = base;
        }

        @Override
        public String getLocation() {
            return this.location;
        }

        @Override
        public K getData() {
            return data;
        }

        @Override
        public DataReminder<K> setData(K data) {
            this.data = data;
            return this;
        }

    }

}
