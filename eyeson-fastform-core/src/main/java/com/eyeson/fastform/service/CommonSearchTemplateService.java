package com.eyeson.fastform.service;

import java.util.List;
import java.util.Map;

/**
 * 版权：融贯资讯 <br/>
 * 作者：jing.zhao@rogrand.com <br/>
 * 生成日期：2016-3-7 <br/>
 * 描述：通用查询服务类
 */
public interface CommonSearchTemplateService {

    /**
     * 描述：重新载入模板 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-7 <br/>
     */
    public void reloadTemplate();

    public String getTempJson(String menuId);

    public Map<String, Object> getTempMap(String menuId);

    public Map<String, Object> getTempSql(String sqlId, String menuId);

    public Map<String, Object> getTempButton(String buttonId, String menuId);

    public Map<String, Object> getTempOperate(String operateId, String menuId);

    public Map<String, Object> getTempOperateQuery(String menuId, String operateQueryId);

    public Map<String, Object> getTempButtonQuery(String menuId, String operateQueryId);

    public Map<String, Object> getTempQuery(String menuId, String queryId);

    public List<Map<String, Object>> getTempQueryList(String menuId);

    Map<String, Object> getTemplateButtonMap(String menuId);

    Map<String, Object> getTemplateOperateMap(String menuId);

}
