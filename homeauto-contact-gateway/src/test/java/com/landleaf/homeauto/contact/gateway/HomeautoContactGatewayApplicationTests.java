package com.landleaf.homeauto.contact.gateway;

import com.landleaf.homeauto.contact.gateway.entity.Test1;
import com.landleaf.homeauto.contact.gateway.mapper.Test1Mapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class HomeautoContactGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
    @Autowired
    Test1Mapper test1Mapper;

    @Test
    public void test3() {

        Test1 test = new Test1();
        test.setAge(23);
        test.setId(12L);
        test1Mapper.insert(test);

        Test1 test2 = test1Mapper.selectById(test.getId());
        log.info("按id查询:"+test2);


    }

}
