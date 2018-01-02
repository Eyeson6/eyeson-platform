package com.eyeson.fastform.process;

import com.eyeson.fastform.common.KkmyException;
import com.eyeson.fastform.common.PageParam;
import com.eyeson.fastform.spring.AopTargetUtils;
import com.eyeson.fastform.spring.SpringContextHolder;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Service("serviceStep")
public class ServiceStep implements Step {

    private final static Logger logger = LoggerFactory
            .getLogger(ServiceStep.class);

    @Override
    public PageParam execute(PageParam map, JSONObject stepObj, String menuId)
            throws KkmyException {
        logger.info("开始执行service流程:" + stepObj);
        String serviceName = (String) stepObj.get("@ref");
        String serviceMethod = (String) stepObj.get("@method");
        return executeService(map, serviceName, serviceMethod);
    }

    /**
     * 根据配置执行spring-srevice方法
     * <p>
     * 描述：通用查询接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-26 <br/>
     *
     * @param pageParam     上下文
     * @param serviceName   服务类名称
     * @param serviceMethod 服务方法名称
     * @return
     * @throws KkmyException
     */
    public PageParam executeService(PageParam pageParam, String serviceName,
                                    String serviceMethod) throws KkmyException {
        return invokeSpringMethod(pageParam, serviceName, serviceMethod);
    }

    /**
     * 描述：基于反射执行spring-service方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-26 <br/>
     *
     * @param pageParam     上下文
     * @param serviceName   服务类名称
     * @param serviceMethod 服务方法名称
     * @return
     * @throws KkmyException
     */
    private PageParam invokeSpringMethod(PageParam pageParam,
                                         String serviceName, String serviceMethod) throws KkmyException {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        Object service = ac.getBean(serviceName);
        try {
            service = AopTargetUtils.getTarget(service);
            Class c = service.getClass();
            Method m = c.getMethod(serviceMethod, PageParam.class);
            m.invoke(service, pageParam);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new KkmyException("service配置不正确.serviceName" + serviceName
                    + ",serviceMethod:" + serviceMethod);
        }
        return pageParam;
    }

}
