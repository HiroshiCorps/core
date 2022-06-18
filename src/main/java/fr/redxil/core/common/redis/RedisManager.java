/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.redis;

import fr.redline.pms.utils.IpInfo;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.List;
import java.util.Map;

public class RedisManager {

    private final IpInfo ipInfo;
    private final String user, password;
    private final int database;
    private RedissonClient connection;

    public RedisManager(IpInfo ipInfo, int database, String user, String password) {
        this.ipInfo = ipInfo;
        this.database = database;
        this.user = user;
        this.password = password;
        initConnection();
    }


    public String getRedisString(String key) {
        return (String) getRedissonClient().getBucket(key).get();
    }


    public void setRedisString(String key, String value) {
        getRedissonClient().getBucket(key).set(value);
    }


    public Long getRedisLong(String key) {
        return getRedissonClient().getAtomicLong(key).get();
    }


    public void setRedisLong(String key, long value) {
        getRedissonClient().getAtomicLong(key).set(value);
    }


    public Object getRedisObject(String key) {
        return getRedissonClient().getBucket(key).get();
    }


    public void setRedisObject(String key, Object s) {
        getRedissonClient().getBucket(key).set(s);
    }


    public void setRedisDouble(String key, double v) {
        getRedissonClient().getAtomicDouble(key).set(v);
    }


    public Double getRedisDouble(String key) {
        return getRedissonClient().getAtomicDouble(key).get();
    }


    public Boolean getRedisBoolean(String s) {
        return Boolean.parseBoolean(getRedisString(s));
    }


    public void setRedisBoolean(String s, boolean b) {
        setRedisString(s, Boolean.toString(b));
    }


    public boolean containsKey(String s) {
        return getRedissonClient().getBucket(s).get() != null;
    }


    public <V> RList<V> getRedisList(String s) {
        return getRedissonClient().getList(s);
    }


    public <V> void setRedisList(String s, List<V> list) {
        if (list instanceof RList)
            if (((RList<V>) list).getName().equals(s)) return;
        RList<V> rList = getRedisList(s);
        rList.clear();
        rList.addAll(list);
    }


    public <K, V> RMap<K, V> getRedisMap(String s) {
        return getRedissonClient().getMap(s);
    }


    public <K, V> void setRedisMap(String s, Map<K, V> map) {
        if (map instanceof RMap)
            if (((RMap<K, V>) map).getName().equals(s)) return;
        RMap<K, V> rMap = getRedissonClient().getMap(s);
        rMap.clear();
        rMap.putAll(map);
    }


    public void clone(String from, String to) {
        getRedissonClient().getBucket(to).set(getRedissonClient().getBucket(from).get());
    }


    public void initConnection() {

        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());
        config.setThreads(8);
        config.setNettyThreads(8);

        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + ipInfo.getIp() + ":" + ipInfo.getPort())
                .setDatabase(database);

        if (user != null)
            singleServerConfig.setUsername(user);

        if (password != null)
            singleServerConfig.setPassword(password);

        connection = Redisson.create(config);

    }


    public void closeConnection() {
        getRedissonClient().shutdown();
    }


    public RedissonClient getRedissonClient() {
        return connection;
    }

}
