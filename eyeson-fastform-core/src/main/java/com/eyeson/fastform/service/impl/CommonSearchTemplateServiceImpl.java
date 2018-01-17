package com.eyeson.fastform.service.impl;

import com.eyeson.fastform.common.StringUtil;
import com.eyeson.fastform.dao.FastCommonMapper;
import com.eyeson.fastform.service.CommonSearchTemplateService;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;


/**
 * 版权：融贯资讯 <br/>
 * 作者：jing.zhao@rogrand.com <br/>
 * 生成日期：2016-5-16 <br/>
 * 描述：表单引擎模板服务类
 */
@Service("commonSearchTemplateService")
public class CommonSearchTemplateServiceImpl implements CommonSearchTemplateService {

    private static final String SQLID_PREFIX = "sqlid_";

    private static final String BUTTONID_PREFIX = "buttonid_";

    private static final String BUTTONID_QUERY_PREFIX = "buttonid_query_";

    private static final String OPERATE_PREFIX = "operate_";

    private static final String OPERATE_QUERY_PREFIX = "operate_query_";

    private static final String QUERYID_PREFIX = "queryid_";

    private static Map<String, Map<String, Object>> templateMap = new HashMap<String, Map<String, Object>>();
    private static Map<String, Map<String, Object>> templateButtonMap = new HashMap<String, Map<String, Object>>();
    private static Map<String, Map<String, Object>> templateOperateMap = new HashMap<String, Map<String, Object>>();

    private static final String SUFFIX = ".xml";

    @Autowired
    private FastCommonMapper commonMapper;

    @Value("${commom.search.xml.path}")
    private String xmlPath;

    private Log logger = LogFactory.getLog(getClass());

    /**
     * 描述：重新载入模板 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-7 <br/>
     */
    @Override
    public void reloadTemplate() {
        //logger.info("开始重新加载通用查询模板！");
        if (StringUtil.isEmpty(xmlPath)) {
            logger.warn("通用查询定时任务扫描路径为空，无法执行！");
            return;
        }
        Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> map1 = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> map2 = new HashMap<String, Map<String, Object>>();
        File file = new File(xmlPath);
        File[] fileList = file.listFiles();
        if (fileList != null && fileList.length != 0) {
            for (File tempFile : fileList) {
                try {
                    if (tempFile != null && tempFile.exists()
                            && tempFile.isFile() && tempFile.getPath() != null
                            && tempFile.getPath().endsWith(SUFFIX)) {
                        SAXReader reader = new SAXReader();
                        Document document = reader.read(tempFile);
                        String xmlString = document.asXML();
                        xmlString = xmlString.replaceAll("<if", "?<if").replaceAll("<set", "#<set")
                                .replaceAll("\n", " ");
                        document = DocumentHelper.parseText(xmlString);
                        // xml转json
                        JSON json = XmlToJson(document);
                        // 提取ID
                        JSONObject jsonObject = JSONObject.fromObject(json);
                        String key = (String) jsonObject.get("@menuId");
                        Map<String, Object> resultMap = new HashMap<String, Object>();
                        Map<String, Object> resultMap1 = new HashMap<String, Object>();
                        Map<String, Object> resultMap2 = new HashMap<String, Object>();
                        Map<String, Object> tempMap = parseJSON2Map(jsonObject
                                .toString());
                        resultMap.put("jsonStr", jsonObject.toString());
                        resultMap.put("tempMap", tempMap);
                        resultMap1.put("buttonMap", tempMap.get("buttons"));
                        resultMap2.put("operateMap", tempMap.get("operate"));
                        paresSqls(resultMap);
                        //paresButtons(resultMap);
                        paresQuery(resultMap);
                        //paresOperate(resultMap);
                        paresTemplateButtonsMap(resultMap1);
                        paresTemplateOperateMap(resultMap2);
                        map.put(key, resultMap);
                        map1.put(key, resultMap1);
                        map2.put(key, resultMap2);
                        //logger.info("读取模板成功：" + tempFile.getName());
                    }
                } catch (Exception e) {
                    logger.error("模板文件加载失败，xml格式不对：" + tempFile.getName());
                    e.printStackTrace();
                }

            }
        }
        this.templateMap = map;
        this.templateButtonMap = map1;
        this.templateOperateMap = map2;
        //logger.info("重新加载通用查询模板完毕！");
    }

    /**
     * 描述：将sql标签按sqlid存放 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-19 <br/>
     *
     * @param resultMap
     */
    @SuppressWarnings("unchecked")
    private void paresSqls(Map<String, Object> resultMap) {
        Map<String, Object> tempMap = (Map<String, Object>) resultMap.get("tempMap");
        Map<String, Object> sqlsmap = (Map<String, Object>) tempMap.get("sqls");
        if (sqlsmap == null) {
            return;
        }
        Object sqlObject = sqlsmap.get("sql");
        if (sqlObject == null) {
            return;
        }
        List<Map<String, Object>> sqlList = null;
        if (sqlObject instanceof JSONArray) {
            sqlList = (List<Map<String, Object>>) sqlObject;
        } else {
            sqlList = new ArrayList<Map<String, Object>>();
            sqlList.add((Map<String, Object>) sqlObject);
        }
        for (Map<String, Object> sqlmap : sqlList) {
            String sqlId = (String) sqlmap.get("@id");
            if (!StringUtil.isEmpty(sqlId) && tempMap.get(SQLID_PREFIX + sqlId) == null) {
                tempMap.put(SQLID_PREFIX + sqlId, sqlmap);
            }
        }

    }

    /**
     * 描述：将按钮标签按buttonId存放  <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-17 <br/>
     *
     * @param resultMap
     */
    @SuppressWarnings("unchecked")
    private void paresButtons(Map<String, Object> resultMap) {
        Map<String, Object> tempMap = (Map<String, Object>) resultMap.get("tempMap");
        Object buttonsObject = tempMap.get("buttons");
        if (buttonsObject == null) {
            return;
        }
        List<Map<String, Object>> buttonList = null;
        if (buttonsObject instanceof JSONArray) {
            buttonList = (List<Map<String, Object>>) buttonsObject;
        } else if (buttonsObject instanceof List) {
            buttonList = (List<Map<String, Object>>) buttonsObject;
        } else {
            buttonList = new ArrayList<Map<String, Object>>();
//			buttonList.add((Map<String, Object>) buttonsObject);
        }
        for (Map<String, Object> buttonmap : buttonList) {
            String buttonId = (String) buttonmap.get("@id");
            if (!StringUtil.isEmpty(buttonId) && tempMap.get(BUTTONID_PREFIX + buttonId) == null) {
                tempMap.put(BUTTONID_PREFIX + buttonId, buttonmap);
            }

            Object queryObject = buttonmap.get("querys");
            if (queryObject != null) {
                List<Map<String, Object>> buttonsQueryList = null;
                if (queryObject instanceof JSONArray) {
                    buttonsQueryList = (List<Map<String, Object>>) queryObject;
                } else if (queryObject instanceof List) {
                    buttonsQueryList = (List<Map<String, Object>>) queryObject;
                } else {
                    buttonsQueryList = new ArrayList<Map<String, Object>>();
//					buttonList.add((Map<String, Object>) buttonsObject);
                }
                for (Map<String, Object> buttonsquery : buttonsQueryList) {
                    String buttonsQueryId = (String) buttonsquery.get("@id");
                    if (!StringUtil.isEmpty(buttonsQueryId) && tempMap.get(BUTTONID_QUERY_PREFIX + buttonsQueryId) == null) {
                        tempMap.put(BUTTONID_QUERY_PREFIX + buttonsQueryId, buttonsquery);
                    }
                }
            }

        }

    }

    /**
     * 描述：将按钮标签按menuid存放 templateButtonMap <br/>
     * 作者：chenhui.yan@rogrand.com <br/>
     * 生成日期：2016-8-15 <br/>
     *
     * @param resultMap
     */
    @SuppressWarnings("unchecked")
    private void paresTemplateButtonsMap(Map<String, Object> resultMap) {
        Object buttonsObject = resultMap.get("buttonMap");
        if (buttonsObject == null) {
            return;
        }
        List<Map<String, Object>> buttonList = null;
        if (buttonsObject instanceof JSONArray) {
            buttonList = (List<Map<String, Object>>) buttonsObject;
        } else if (buttonsObject instanceof List) {
            buttonList = (List<Map<String, Object>>) buttonsObject;
        } else {
            buttonList = new ArrayList<Map<String, Object>>();
        }
        for (Map<String, Object> buttonmap : buttonList) {
            String buttonId = (String) buttonmap.get("@id");
            if (!StringUtil.isEmpty(buttonId) && resultMap.get(BUTTONID_PREFIX + buttonId) == null) {
                resultMap.put(BUTTONID_PREFIX + buttonId, buttonmap);
            }
            Object queryObject = buttonmap.get("querys");
            if (queryObject != null) {
                List<Map<String, Object>> buttonsQueryList = null;
                if (queryObject instanceof JSONArray) {
                    buttonsQueryList = (List<Map<String, Object>>) queryObject;
                } else if (queryObject instanceof List) {
                    buttonsQueryList = (List<Map<String, Object>>) queryObject;
                } else {
                    buttonsQueryList = new ArrayList<Map<String, Object>>();
                }
                for (Map<String, Object> buttonsquery : buttonsQueryList) {
                    String buttonsQueryId = (String) buttonsquery.get("@id");
                    if (!StringUtil.isEmpty(buttonsQueryId) && resultMap.get(BUTTONID_QUERY_PREFIX + buttonsQueryId) == null) {
                        resultMap.put(BUTTONID_QUERY_PREFIX + buttonsQueryId, buttonsquery);
                    }
                }
            }
        }
    }

    /**
     * 描述：将按钮标签按menuid存放 templateOperateMap <br/>
     * 作者：chenhui.yan@rogrand.com <br/>
     * 生成日期：2016-8-16 <br/>
     *
     * @param resultMap
     */
    @SuppressWarnings("unchecked")
    private void paresTemplateOperateMap(Map<String, Object> resultMap) {
        Object buttonsObject = resultMap.get("operateMap");
        if (buttonsObject == null) {
            return;
        }
        List<Map<String, Object>> operateList = null;
        if (buttonsObject instanceof JSONArray) {
            operateList = (List<Map<String, Object>>) buttonsObject;
        } else if (buttonsObject instanceof List) {
            operateList = (List<Map<String, Object>>) buttonsObject;
        } else {
            operateList = new ArrayList<Map<String, Object>>();
        }
        for (Map<String, Object> operatemap : operateList) {
            String operateId = (String) operatemap.get("@id");
            if (!StringUtil.isEmpty(operateId) && resultMap.get(OPERATE_PREFIX + operateId) == null) {
                resultMap.put(OPERATE_PREFIX + operateId, operatemap);
            }

            Object queryObject = operatemap.get("querys");
            if (queryObject != null) {
                List<Map<String, Object>> operateQueryList = null;
                if (queryObject instanceof JSONArray) {
                    operateQueryList = (List<Map<String, Object>>) queryObject;
                } else if (queryObject instanceof List) {
                    operateQueryList = (List<Map<String, Object>>) queryObject;
                } else {
                    operateQueryList = new ArrayList<Map<String, Object>>();
                }
                for (Map<String, Object> operatequery : operateQueryList) {
                    String operateQueryId = (String) operatequery.get("@id");
                    if (!StringUtil.isEmpty(operateQueryId) && resultMap.get(OPERATE_QUERY_PREFIX + operateQueryId) == null) {
                        resultMap.put(OPERATE_QUERY_PREFIX + operateQueryId, operatequery);
                    }
                }
            }

        }
    }

    /**
     * 描述：将操作标签、操作中的table按id存放  <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-17 <br/>
     *
     * @param resultMap
     */
    private void paresOperate(Map<String, Object> resultMap) {
        Map<String, Object> tempMap = (Map<String, Object>) resultMap.get("tempMap");
        Object operateObject = tempMap.get("operate");
        if (operateObject == null) {
            return;
        }
        List<Map<String, Object>> operateList = null;
        if (operateObject instanceof JSONArray) {
            operateList = (List<Map<String, Object>>) operateObject;
        } else if (operateObject instanceof List) {
            operateList = (List<Map<String, Object>>) operateObject;
        } else {
            operateList = new ArrayList<Map<String, Object>>();
//			buttonList.add((Map<String, Object>) buttonsObject);
        }
        for (Map<String, Object> operatemap : operateList) {
            String operateId = (String) operatemap.get("@id");
            if (!StringUtil.isEmpty(operateId) && tempMap.get(OPERATE_PREFIX + operateId) == null) {
                tempMap.put(OPERATE_PREFIX + operateId, operatemap);
            }

            Object queryObject = operatemap.get("querys");
            if (queryObject != null) {
                List<Map<String, Object>> operateQueryList = null;
                if (queryObject instanceof JSONArray) {
                    operateQueryList = (List<Map<String, Object>>) queryObject;
                } else if (queryObject instanceof List) {
                    operateQueryList = (List<Map<String, Object>>) queryObject;
                } else {
                    operateQueryList = new ArrayList<Map<String, Object>>();
//					buttonList.add((Map<String, Object>) buttonsObject);
                }
                for (Map<String, Object> operatequery : operateQueryList) {
                    String operateQueryId = (String) operatequery.get("@id");
                    if (!StringUtil.isEmpty(operateQueryId) && tempMap.get(OPERATE_QUERY_PREFIX + operateQueryId) == null) {
                        tempMap.put(OPERATE_QUERY_PREFIX + operateQueryId, operatequery);
                    }
                }
            }

        }
    }

    /**
     * 描述：将查询配置按queryId存放  <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-20 <br/>
     *
     * @param resultMap
     */
    @SuppressWarnings("unchecked")
    private void paresQuery(Map<String, Object> resultMap) {
        Map<String, Object> tempMap = (Map<String, Object>) resultMap.get("tempMap");
        Object queryObject = tempMap.get("query");
        if (queryObject == null) {
            return;
        }
        List<Map<String, Object>> queryList = null;
        if (queryObject instanceof JSONArray) {
            queryList = (List<Map<String, Object>>) queryObject;
        } else if (queryObject instanceof List) {
            queryList = (List<Map<String, Object>>) queryObject;
        } else if (queryObject instanceof JSONObject) {
            JSONArray array = JSONArray.fromObject(queryObject);
            queryList = (List<Map<String, Object>>) array;

        } else {
            queryList = new ArrayList<Map<String, Object>>();
//			buttonList.add((Map<String, Object>) buttonsObject);
        }
        tempMap.put("queryList", queryList);
        for (Map<String, Object> querymap : queryList) {
            String queryId = (String) querymap.get("@queryId");
            if (tempMap.get(QUERYID_PREFIX + queryId) == null) {
                tempMap.put(QUERYID_PREFIX + queryId, querymap);
            }
        }

    }

    /**
     * 将json转为map 描述：〈描述〉 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-7 <br/>
     *
     * @param jsonStr
     * @return
     */
    public Map<String, Object> parseJSON2Map(String jsonStr) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 最外层解析
        JSONObject json = JSONObject.fromObject(jsonStr);
        for (Object k : json.keySet()) {
            Object v = json.get(k);
            // 如果内层还是数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Iterator<JSONObject> it = ((JSONArray) v).iterator();
                while (it.hasNext()) {
                    JSONObject json2 = it.next();
                    list.add(parseJSON2Map(json2.toString()));
                }
                map.put(k.toString(), list);
            } else {
                map.put(k.toString(), v);
            }
        }
        return map;
    }

    /**
     * 描述：xml转json对象 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-7 <br/>
     *
     * @param document
     */
    private JSON XmlToJson(Document document) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        String text = document.asXML();
        JSON json = xmlSerializer.read(text);
        return json;
    }

    @Override
    public String getTempJson(String menuId) {
        if (templateMap.get(menuId) == null) {
            return null;
        }
        return (String) templateMap.get(menuId).get("jsonStr");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempMap(String menuId) {
        if (templateMap.get(menuId) == null) {
            return null;
        }
        return (Map<String, Object>) templateMap.get(menuId).get("tempMap");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempSql(String sqlId, String menuId) {
        // TODO Auto-generated method stub
        Map<String, Object> tempMap = getTempMap(menuId);
        if (tempMap != null) {
            return (Map<String, Object>) tempMap.get(SQLID_PREFIX + sqlId);
        }
        return null;
    }

    /**
     * 获取对应button
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempButton(String buttonId, String menuId) {
        // TODO Auto-generated method stub
        //Map<String, Object> tempMap = getTempMap(menuId);
        Map<String, Object> buttonMap = getTemplateButtonMap(menuId);
        if (buttonMap != null) {
            return (Map<String, Object>) buttonMap.get(BUTTONID_PREFIX + buttonId);
        }
        return null;
    }

    /**
     * 获取对应Operate
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempOperate(String operateId, String menuId) {
        // TODO Auto-generated method stub
        //Map<String, Object> tempMap = getTempMap(menuId);
        Map<String, Object> operateMap = getTemplateOperateMap(menuId);
        if (operateMap != null) {
            return (Map<String, Object>) operateMap.get(OPERATE_PREFIX + operateId);
        }
        return null;
    }

    /**
     * 获取operate中的query
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempOperateQuery(String menuId, String operateQueryId) {
        // TODO Auto-generated method stub
        //Map<String, Object> tempMap = getTempMap(menuId);
        Map<String, Object> operateMap = getTemplateOperateMap(menuId);
        if (operateMap != null) {
            if (StringUtil.isEmpty(operateQueryId)) {
                for (String key : operateMap.keySet()) {
                    if (key.contains(OPERATE_QUERY_PREFIX)) {
                        return (Map<String, Object>) operateMap.get(key);
                    }
                }
            }
            return (Map<String, Object>) operateMap.get(OPERATE_QUERY_PREFIX + operateQueryId);
        }
        return null;
    }

    /**
     * 获取button中的query
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempButtonQuery(String menuId, String operateQueryId) {
        // TODO Auto-generated method stub
        //Map<String, Object> tempMap = getTempMap(menuId);
        Map<String, Object> buttonMap = getTemplateButtonMap(menuId);
        if (buttonMap != null) {
            if (StringUtil.isEmpty(operateQueryId)) {
                for (String key : buttonMap.keySet()) {
                    if (key.contains(BUTTONID_QUERY_PREFIX)) {
                        return (Map<String, Object>) buttonMap.get(key);
                    }
                }
            }
            return (Map<String, Object>) buttonMap.get(BUTTONID_QUERY_PREFIX + operateQueryId);
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTempQuery(String menuId, String queryId) {
        // TODO Auto-generated method stub
        Map<String, Object> tempMap = getTempMap(menuId);
        if (tempMap != null) {
            if (StringUtil.isEmpty(queryId)) {
                for (String key : tempMap.keySet()) {
                    if (key.contains(QUERYID_PREFIX)) {
                        return (Map<String, Object>) tempMap.get(key);
                    }
                }
            }
            return (Map<String, Object>) tempMap.get(QUERYID_PREFIX + queryId);
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> getTempQueryList(String menuId) {
        // TODO Auto-generated method stub
        Map<String, Object> tempMap = getTempMap(menuId);
        return (List<Map<String, Object>>) tempMap.get("queryList");
    }

    /**
     * 根据menuId获取ButtonMap
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTemplateButtonMap(String menuId) {

        if (templateButtonMap.get(menuId) == null) {
            return null;
        }
        return (Map<String, Object>) templateButtonMap.get(menuId);
    }

    /**
     * 根据menuId获取OperateMap
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getTemplateOperateMap(String menuId) {

        if (templateOperateMap.get(menuId) == null) {
            return null;
        }
        return (Map<String, Object>) templateOperateMap.get(menuId);
    }
}
