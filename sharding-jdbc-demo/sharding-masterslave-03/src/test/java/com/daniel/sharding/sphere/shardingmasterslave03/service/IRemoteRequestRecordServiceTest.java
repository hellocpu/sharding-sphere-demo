package com.daniel.sharding.sphere.shardingmasterslave03.service;


import com.daniel.sharding.sphere.shardingmasterslave03.ShardingMasterslave03ApplicationTests;
import com.daniel.sharding.sphere.shardingmasterslave03.entity.Merchant;
import com.daniel.sharding.sphere.shardingmasterslave03.entity.RemoteRequestRecord;
import com.daniel.sharding.sphere.shardingmasterslave03.entity.enums.ApiTypeEnum;
import com.daniel.sharding.sphere.shardingmasterslave03.entity.enums.MerchantDataExchangeTypeEnum;
import com.daniel.sharding.sphere.shardingmasterslave03.entity.enums.MerchantStatusEnum;
import com.daniel.sharding.sphere.shardingmasterslave03.entity.enums.RequestParamTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
public class IRemoteRequestRecordServiceTest extends ShardingMasterslave03ApplicationTests {

    @Autowired
    private IMerchantService merchantService;

    @Autowired
    private IRemoteRequestRecordService remoteRequestRecordService;


    @Test
    public void insert() {
        Merchant merchant = new Merchant();
        merchant.setOrgId(121L);
        merchant.setMid(RandomStringUtils.random(20, true, true));
        merchant.setKeyt(RandomStringUtils.random(32, true, true).toUpperCase());
        merchant.setIpRestricted(false);
        merchant.setStatus(MerchantStatusEnum.EXPIRED);
        merchant.setBaseUrl("http://localhost:8070/vendor");
        merchant.setDataExchangeType(MerchantDataExchangeTypeEnum.STANDARD);
        merchant.setCreateTime(new Date());
        merchantService.insert(merchant);
    }

    @Test
    public void insert2() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        for (int j = 0; j < 10; j++) {
            cachedThreadPool.execute(() -> {
                for (int i = 0; i < 100; i++) {
                    Merchant merchant = merchantService.selectById(1L);
                    RemoteRequestRecord record = new RemoteRequestRecord();
                    record.setMerchantId(merchant.getId());
                    record.setMid(merchant.getMid());
                    record.setApiType(i % 2 == 0 ? ApiTypeEnum.TRADE : ApiTypeEnum.PLAN);
                    record.setPostTime(new Date());
                    record.setUrl(merchant.getBaseUrl());
                    record.setParamType(RequestParamTypeEnum.JSON);
                    record.setCreateTime(new Date());

                    try {
                        TimeUnit.MICROSECONDS.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    remoteRequestRecordService.insert(record);
                }
            });
        }

        try {
            TimeUnit.MINUTES.sleep(1L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}