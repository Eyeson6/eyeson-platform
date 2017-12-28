package com.eyeson; /**
 * @author HelloWood
 * @create 2016-11-30 22:10
 * @email hellowoodes@gmail.com
 **/

import com.eyeson.mapper.CommonMapper;
import com.eyeson.mapper.UserMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class SpringController {

    private  final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringController.class);

    @Value("${com.neo.title}")
    private String title;
    @Value("${com.neo.description}")
    private String description;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommonMapper commonMapper;

    @RequestMapping("/")
    @ResponseBody
    List<Integer> home() {
        System.out.println("121211123");
        logger.info("日志不不在！");
        List<Integer> list =  userMapper.getAll();
        System.out.println(list.size()+list.size());
        Map map = new HashMap();
        map.put("sql", "SELECT id FROM test1");
        List<Map> list1 = commonMapper.getAll(map);
        System.out.println(list1.size());
        return list;
    }


//    public static void main(String[] args) throws Exception {
//        SpringApplication.run(SpringController.class, args);
//    }
}

