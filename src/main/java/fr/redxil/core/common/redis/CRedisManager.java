/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.redis;

import fr.redxil.api.common.redis.RedisManager;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.List;
import java.util.Map;

public class CRedisManager implements RedisManager {

    private final String host, port, user, password;
    private final int database;
    private RedissonClient connection;

    public CRedisManager(String host, String port, int database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        initConnection();
    }

    @Override
    public String getRedisString(String key) {
        return (String) getRedissonClient().getBucket(key).get();
    }

    @Override
    public void setRedisString(String key, String value) {
        getRedissonClient().getBucket(key).set(value);
    }

    @Override
    public long getRedisLong(String key) {
        return getRedissonClient().getAtomicLong(key).get();
    }

    @Override
    public void setRedisLong(String key, long value) {
        getRedissonClient().getAtomicLong(key).set(value);
    }

    @Override
    public Object getRedisObject(String key) {
        return getRedissonClient().getBucket(key).get();
    }

    @Override
    public void setRedisObject(String key, Object s) {
        getRedissonClient().getBucket(key).set(s);
    }

    @Override
    public void setRedisDouble(String key, double v) {
        getRedissonClient().getAtomicDouble(key).set(v);
    }

    @Override
    public double getRedisDouble(String key) {
        return getRedissonClient().getAtomicDouble(key).get();
    }

    @Override
    public boolean getRedisBoolean(String s) {
        return Boolean.parseBoolean(getRedisString(s));
    }

    @Override
    public void setRedisBoolean(String s, boolean b) {
        setRedisString(s, Boolean.toString(b));
    }

    @Override
    public boolean containsKey(String s) {
        return getRedissonClient().getBucket(s).get() != null;
    }

    @Override
    public <V> RList<V> getRedisList(String s) {
        return getRedissonClient().getList(s);
    }

    @Override
    public <V> void setRedisList(String s, List<V> list) {
        if (list instanceof RList)
            if (((RList<V>) list).getName().equals(s)) return;
        RList<V> rList = getRedisList(s);
        rList.clear();
        rList.addAll(list);
    }

    @Override
    public <K, V> RMap<K, V> getRedisMap(String s) {
        return getRedissonClient().getMap(s);
    }

    @Override
    public <K, V> void setRedisMap(String s, Map<K, V> map) {
        if (map instanceof RMap)
            if (((RMap<K, V>) map).getName().equals(s)) return;
        RMap<K, V> rMap = getRedissonClient().getMap(s);
        rMap.clear();
        rMap.putAll(map);
    }

    @Override
    public void clone(String from, String to) {
        getRedissonClient().getBucket(to).set(getRedissonClient().getBucket(from).get());
    }

    @Override
    public void initConnection() {

        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());
        config.setThreads(8);
        config.setNettyThreads(8);

        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database);

        if (user != null)
            singleServerConfig.setUsername(user);

        if (password != null)
            singleServerConfig.setPassword(password);

        connection = Redisson.create(config);

    }

    @Override
    public void closeConnection() {
        getRedissonClient().shutdown();
    }

    @Override
    public RedissonClient getRedissonClient() {
        return connection;
    }

}
