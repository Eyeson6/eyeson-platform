package com.eyeson.fastform.task;

import com.eyeson.fastform.service.CommonSearchTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Blues Zhao on 上午9:49 2018/1/2.
 */
@Component
public class SchedulerTasks {

    @Autowired
    private CommonSearchTemplateService commonSearchTemplateService;

    @Scheduled(cron = "*/6 * * * * ?")
    private void process() {
        commonSearchTemplateService.reloadTemplate();
    }

}