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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DataReminder<K> {

    static <K> DataReminder<K> generateReminder(String location, K base) {
        if (API.getInstance().isOnlineMod())
            return new OnlineReminder<>(location);
        else return new OfflineReminder<>(location, base);
    }

    static <K> DataReminder<List<K>> generateListReminder(String location) {
        if (API.getInstance().isOnlineMod())
            return new OnlineListReminder<>(location);
        else return new OfflineReminder<>(location, new ArrayList<>());
    }

    static <K, V> DataReminder<Map<K, V>> generateMapReminder(String location) {
        if (API.getInstance().isOnlineMod())
            return new OnlineMapReminder<>(location);
        else return new OfflineReminder<>(location, new HashMap<>());
    }

    String getLocation();

    K getData();

    DataReminder<K> setData(K data);

    class OnlineReminder<K> implements DataReminder<K> {

        final String location;

        private OnlineReminder(String location) {
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

    class OnlineListReminder<K> implements DataReminder<List<K>> {

        final String location;

        private OnlineListReminder(String location) {
            this.location = location;
        }

        @Override
        public String getLocation() {
            return this.location;
        }

        @Override
        public List<K> getData() {
            return API.getInstance().getRedisManager().getRedissonClient().getList(location);
        }

        @Override
        public DataReminder<List<K>> setData(List<K> data) {
            List<K> list = getData();
            list.clear();
            list.addAll(data);
            return this;
        }

    }

    class OnlineMapReminder<K, V> implements DataReminder<Map<K, V>> {

        final String location;

        private OnlineMapReminder(String location) {
            this.location = location;
        }

        @Override
        public String getLocation() {
            return this.location;
        }

        @Override
        public Map<K, V> getData() {
            return API.getInstance().getRedisManager().getRedissonClient().getMap(location);
        }

        @Override
        public DataReminder<Map<K, V>> setData(Map<K, V> data) {
            Map<K, V> list = getData();
            list.clear();
            list.putAll(data);
            return this;
        }

    }

}
