package com.c88.game.adapter;

import com.c88.affiliate.api.feign.AffiliateMemberClient;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.client.MemberBalanceClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(
        basePackages = {"com.c88.game.adapter.mapper"}
)
@EnableFeignClients(basePackageClasses = {MemberFeignClient.class, MemberBalanceClient.class, AffiliateMemberClient.class})
@SpringBootApplication
@EnableDiscoveryClient
public class GameAdapterBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameAdapterBootApplication.class, args);
    }

}
