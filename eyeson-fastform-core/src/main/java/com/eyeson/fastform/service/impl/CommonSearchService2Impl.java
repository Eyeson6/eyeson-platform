package com.eyeson.fastform.service.impl;

import com.eyeson.fastform.bean.SelectBean;
import com.eyeson.fastform.common.*;
import com.eyeson.fastform.dao.FastCommonMapper;
import com.eyeson.fastform.process.BaseSqlStep;
import com.eyeson.fastform.process.Step;
import com.eyeson.fastform.service.CommonSearchService;
import com.eyeson.fastform.service.CommonSearchTemplateService;
import com.eyeson.fastform.spring.AopTargetUtils;
import com.eyeson.fastform.spring.SpringContextHolder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 版权：融贯资讯 <br/>
 * 作者：jing.zhao@rogrand.com <br/>
 * 生成日期：2016-3-7 <br/>
 * 描述：通用查询服务类
 */
@Service("commonSearchService2")
public class CommonSearchService2Impl implements CommonSearchService {

    private static Map<String, Map<String, Object>> templateMap = new HashMap<String, Map<String, Object>>();

    private static final String SUFFIX = ".xml";

    private static final String PROCESS_BREAK_KEY = "mustBreak";
    private static final String BREAK_STEP_KEY = "breakStep";


    @Autowired
    private FastCommonMapper commonMapper;

    @Value("${commom.search.xml.path}")
    private String xmlPath;

    private Log logger = LogFactory.getLog(getClass());

    @Autowired
    private CommonSearchTemplateService commonSearchTemplateService;

    /**
     * 描述：通用查询接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param paraMap
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public PageResult search(String menuId, PageParam pageParam)
            throws KkmyException {
        String queryId = pageParam.get("queryId") == null ? null : pageParam.get("queryId").toString();
        Map<String, Object> queryMap = (Map<String, Object>) commonSearchTemplateService.getTempQuery(menuId, queryId);

        if (queryMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = queryMap.get("process");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }
        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, menuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        }

        PageResult pageResult = new PageResult();
        if (pageParam != null) {
            pageResult.setPageList((List) pageParam.get("pageList"));
            pageResult.setPageParam(pageParam);
        }

        Object footerProcessObject = queryMap.get("footerProcess");
        if (footerProcessObject != null) {
            if (footerProcessObject instanceof Collection) {
                JSONArray json = new JSONArray();
                json.addAll((Collection) footerProcessObject);
                pageParam = doProcess(json, menuId, pageParam);
            } else {
                pageParam = doProcess((JSONArray) footerProcessObject, menuId, pageParam);
            }

            if (pageParam != null) {
                List<Map<String, Object>> footerList = (List<Map<String, Object>>) pageParam.get("pageList");
                for (Map<String, Object> map : footerList) {
                    map.put("footer", true);
                }
                pageResult.setFooter((List) pageParam.get("pageList"));
                pageResult.setPageParam(pageParam);
            }
        }

        return pageResult;
    }

    /**
     * 描述：form页面通用查询接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param paraMap
     * @return
     * @throws KkmyException
     */
    @Override
    public PageResult searchDynamicViewPage(String menuId, PageParam pageParam) throws KkmyException {
        String queryId = pageParam.get("dynamicViewQueryId") == null ? "" : pageParam.get("dynamicViewQueryId").toString();
        String operate = pageParam.get("operate") == null ? "" : pageParam.get("operate").toString();

        Map<String, Object> queryMap = new HashMap<String, Object>();
        if (StringUtil.isEmpty(operate)) {
            queryMap = commonSearchTemplateService.getTempButtonQuery(menuId, queryId);
        } else {
            queryMap = commonSearchTemplateService.getTempOperateQuery(menuId, queryId);
        }

        if (queryMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = queryMap.get("process");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }
        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, menuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        }

        PageResult pageResult = new PageResult();
        if (pageParam != null) {
            pageResult.setPageList((List) pageParam.get("pageList"));
            pageResult.setPageParam(pageParam);
        }
        return pageResult;
    }

    /**
     * 描述： 根据流程配置动态执行流程<br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-26 <br/>
     *
     * @param processMap 流程配置
     * @param menuId     菜单ID
     * @param contextMap 请求上下文
     * @return
     * @throws KkmyException
     */
    private PageParam doProcess(JSONArray processMap, String menuId,
                                PageParam contextMap) throws KkmyException {
        Iterator it = processMap.iterator();
        while (it.hasNext()) {
            JSONObject stepObj = (JSONObject) it.next();
            String channelString = stepObj.getString("@channel");
            ApplicationContext ac = SpringContextHolder.getApplicationContext();
            Step step = (Step) ac.getBean(channelString + "Step");
            contextMap = step.execute(contextMap, stepObj, menuId);
            if (contextMap != null && contextMap.get(PROCESS_BREAK_KEY) != null && Boolean.parseBoolean(contextMap.get(PROCESS_BREAK_KEY).toString())) {
                contextMap.put(BREAK_STEP_KEY, stepObj);
                break;
            }
        }
        parsebreakMsg(contextMap, processMap);
        return contextMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> chart(String menuId, PageParam pageParam)
            throws KkmyException {
        // 定义返回参数
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<String> listLabelsResult = new ArrayList<String>();
        resultMap.put("labelsList", listLabelsResult);
        pageParam.setPage(1l);
        pageParam.setRows(30);
        PageResult pageResult = this.search(menuId, pageParam);
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        // Map<String, Object> chartMap = (Map<String, Object>)
        // tempMap.get("chart");
        Object chartObject = tempMap.get("chart");
        List<Map<String, Object>> chartList = null;
        if (chartObject instanceof JSONArray) {
            chartList = (List<Map<String, Object>>) JSONArray.toCollection(
                    (JSONArray) chartObject, HashMap.class);
            resultMap.put("mutiChart", true);
        } else if (chartObject instanceof List) {
            chartList = (List<Map<String, Object>>) chartObject;
            resultMap.put("mutiChart", true);
        } else {
            chartList = new ArrayList<Map<String, Object>>();
            chartList.add((Map<String, Object>) chartObject);
        }
        if (pageParam.get("index") == null) {
            throw new KkmyException("图表配置不正确");
        }
        Integer chartIndex = Integer
                .parseInt(pageParam.get("index").toString());
        if (chartIndex == null || chartIndex >= chartList.size()) {
            throw new KkmyException("图表配置不正确");
        }
        Map<String, Object> chartMap = chartList.get(chartIndex);
        String lables = (String) chartMap.get("@labels");
        Object object = chartMap.get("data");
        if (object instanceof JSONArray) {
            dataList = (List<Map<String, Object>>) JSONArray.toCollection(
                    (JSONArray) object, HashMap.class);
        } else if (object instanceof List) {
            dataList = (List<Map<String, Object>>) object;
        } else {
            dataList = new ArrayList<Map<String, Object>>();
            dataList.add((Map<String, Object>) chartMap.get("data"));
        }
        // 预先准备返回值中的value集合
        for (Map<String, Object> dataMap : dataList) {
            if (dataMap.get("@name") != null) {
                resultMap.put("valueList_" + dataMap.get("@name"),
                        new ArrayList<String>());
            }

        }

        List<Map<String, Object>> searchResultlist = pageResult.getPageList();
        for (Map<String, Object> map : searchResultlist) {
            for (Map<String, Object> dataMap : dataList) {
                List<String> list = (List<String>) resultMap.get("valueList_"
                        + dataMap.get("@name"));
                Object dataobj = map.get(dataMap.get("@name"));
                if (!CommonSearchUtils.isNum(dataobj)) {
                    throw new KkmyException("数据不正确，统计图数据只能为数字类型");
                }
                list.add(obj2string(dataobj));
            }
            Object labelObject = map.get(lables);
            listLabelsResult.add(obj2string(labelObject));

        }
        return resultMap;
    }

    private String obj2string(Object object) {
        if (object instanceof Timestamp) {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(object);
        } else {
            return object.toString();
        }

    }

    /**
     * 描述：通用添加接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param paraMap
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void insert(String menuId, PageParam pageParam) throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> insertMap = (Map<String, Object>) tempMap
                .get("insert");

        if (insertMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = insertMap.get("process");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }

        pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        PageResult pageResult = new PageResult();
        if (pageParam != null) {
            pageResult.setPageList((List) pageParam.get("pageList"));
            pageResult.setPageParam(pageParam);
        }
    }

    /**
     * 描述：通用修改接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param paraMap
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void update(String menuId, PageParam pageParam) throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> updateMap = (Map<String, Object>) tempMap
                .get("update");

        if (updateMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = updateMap.get("process");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }
        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, menuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        }
        PageResult pageResult = new PageResult();
        if (pageParam != null) {
            pageResult.setPageList((List) pageParam.get("pageList"));
            pageResult.setPageParam(pageParam);
        }
    }

    /**
     * 描述：按钮提交操作 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param paraMap
     * @return
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void buttonExecute(String menuId, PageParam pageParam, String buttonId) throws KkmyException {
//		String operate=pageParam.get("operate")+"";
        String operate = pageParam.get("operate") == null ? "" : pageParam.get("operate").toString();
        Map<String, Object> buttonMap = new HashMap<String, Object>();
        if (StringUtil.isEmpty(operate)) {
            buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);
        } else {
            buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);
        }
        //Map<String, Object> buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);

        if (buttonMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = buttonMap.get("process");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }

        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, menuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        }
        Object commonResult = pageParam.get("commonResult");
        if (commonResult != null && (Boolean) commonResult) {
            Object dynamicMsg = buttonMap.get("@successMessage");
            if (dynamicMsg != null) {
                pageParam.put("checkMessage", dynamicMsg);
            }
        }
    }

    /**
     * 描述：通用查询动态检查 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-20 <br/>
     *
     * @param menuId
     * @param pageParam
     * @param buttonId
     * @throws KkmyException
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void dynamicCheck(String menuId, PageParam pageParam, String buttonId) throws KkmyException {
        Map<String, Object> buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);

        if (buttonMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = buttonMap.get("preprocess");
        if (processObj == null) {
            return;
        }

        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, menuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        }

    }


    @Override
    public void operateCheck(String menuId, PageParam pageParam, String buttonId) throws KkmyException {
        Map<String, Object> buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);

        if (buttonMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = buttonMap.get("preprocess");
        if (processObj == null) {
            return;
        }

        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, menuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        }
    }


    private void parsebreakMsg(PageParam pageParam, JSONArray processObj) {
        if (pageParam != null && pageParam.get(BREAK_STEP_KEY) != null) {
            JSONObject breakStep = (JSONObject) pageParam.get(BREAK_STEP_KEY);
            pageParam.put("commonResult", false);
            String message = breakStep.getString("breakMessage");
            if (!StringUtil.isEmpty(message)) {
                pageParam.put("checkMessage", message);
            } else {
                pageParam.put("checkMessage", "不允许这样操作！");
            }
        } else {
            pageParam.put("commonResult", true);
            pageParam.put("checkMessage", "操作成功！");
        }
    }


    /**
     * 描述：反射调用静态方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @return
     */
    private Object invokeUtilMethod(String methodStirng, String methodParam,
                                    Map<String, Object> paraMap) {
        try {
            if (StringUtil.isEmpty(methodParam)
                    || StringUtil.isEmpty(methodStirng)) {
                return null;
            }

            // 动态计算参数个数
            String[] params = methodParam.split(",");
            Class c = Class.forName(CommonSearchUtils.class.getName());
            Class[] paramClass = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                String arg = params[i];
                if ((!StringUtil.isEmpty(arg)) && arg.startsWith("${")
                        && arg.endsWith("}")) {
                    arg = arg.replaceAll("\\$\\{", "").replaceAll("\\}", "");
                    String argValue = (String) paraMap.get(arg);
                    params[i] = argValue;
                }
                paramClass[i] = Object.class;

            }
            // 得到方法
            Method m = c.getMethod(methodStirng, paramClass);
            // 执行方法
            Object object = m.invoke(c, params);
            return object;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 描述：〈根据name和menuId获取select控件的初始化值〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月10日 <br/>
     *
     * @param parameter
     * @return
     * @throws KkmyException
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SelectBean> searchSelect(PageParam param, String type)
            throws KkmyException {
        String menuId = param.get("menuId").toString();
        String queryId = param.get("queryId") + "";
        String buttonId = null;
        if (param.get("buttonId") != null) {
            buttonId = param.get("buttonId").toString();
        }
        String operate = null;
        if (param.get("operate") != null) {
            operate = param.get("operate").toString();
        }

        String querys = null;
        if (param.get("querys") != null) {
            querys = param.get("querys").toString();
        }

        String name = param.get("name").toString();
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);

        List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
        if ("main".equals(type)) {
            Map<String, Object> queryMap = (Map<String, Object>) commonSearchTemplateService.getTempQuery(menuId, queryId);
            if (queryMap == null) {
                throw new KkmyException("没有找到查询配置");
            }

            conditions = (List<Map<String, Object>>) queryMap.get("conditions");
        } else if (!StringUtil.isEmpty(buttonId)) {
            //Map buttonMap = (Map) commonSearchTemplateService.getTempButton(buttonId, menuId);
            Map<String, Object> buttonMap = new HashMap<String, Object>();
            if (StringUtil.isEmpty(operate)) {
                buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);
            } else {
                buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);
            }

            if (StringUtil.isEmpty(querys)) {
                conditions = (List<Map<String, Object>>) buttonMap.get("conditions");
            } else {//form 上的table上的下拉框
                conditions = (List<Map<String, Object>>) buttonMap.get("querys");
                for (Map<String, Object> map : conditions) {
                    conditions = (List<Map<String, Object>>) map.get("model");
                }
            }
        } else if ("add".equals(type)) {
            Map insert = (Map) tempMap.get("insert");
            conditions = (List<Map<String, Object>>) insert.get("conditions");
        } else if ("fastSave".equals(type)) {
            Map<String, Object> queryMap = (Map<String, Object>) commonSearchTemplateService.getTempQuery(menuId, queryId);
            if (queryMap == null) {
                throw new KkmyException("没有找到查询配置");
            }

            conditions = (List<Map<String, Object>>) queryMap.get("model");
        } else {
            Map update = (Map) tempMap.get("update");
            conditions = (List<Map<String, Object>>) update.get("conditions");
        }

        if (conditions == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }

        List<SelectBean> beans = new ArrayList<SelectBean>();
        for (Map<String, Object> map : conditions) {
            if (map.get("@name").toString().equals(name)) {
                beans = getOptionList(param, map);
                break;
            }
        }
        return beans;
    }

    /**
     * 描述：根据当前上下文和condition配置获取下拉选项的集合 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-8-17 <br/>
     *
     * @param row
     * @param condition
     * @return
     * @throws KkmyException
     */
    private List<SelectBean> getOptionList(Map<String, Object> row, Map<String, Object> condition) throws KkmyException {
        List<SelectBean> beans = new ArrayList<SelectBean>();
        List<Map<String, Object>> options = (List<Map<String, Object>>) condition
                .get("option");
        if (options != null) {
            for (Map<String, Object> option : options) {
                SelectBean bean = new SelectBean();
                bean.setId(option.get("@id").toString());
                bean.setText(option.get("@text").toString());
                beans.add(bean);
            }
        } else {
            String sql = (String) condition.get("sql");
            if (!StringUtil.isEmpty(sql)) {
                List<String> paramNameList = new BaseSqlStep().matchSqlParam(sql);
                for (String paramName : paramNameList) {
                    String value = new BaseSqlStep().getParamValue(paramName, row);
                    sql = sql
                            .replaceAll("\\%\\$\\{" + paramName + "\\}\\%",
                                    "'%" + value + "%'")
                            .replaceAll("\\%\\$\\{" + paramName + "\\}",
                                    "'%" + value + "'")
                            .replaceAll("\\$\\{" + paramName + "\\}\\%",
                                    "'" + value + "%'")
                            .replaceAll("\\$\\{" + paramName + "\\}",
                                    "'" + value + "'");
                }

                List<Map> list = commonMapper.queryList(sql);
                for (Map option : list) {
                    SelectBean bean = new SelectBean();
                    bean.setId(option.get("id").toString());
                    bean.setText(option.get("text").toString());
                    beans.add(bean);
                }
            }
        }
        return beans;
    }

    /**
     * 描述：〈根据name和menuId联动控件的值〉 <br/>
     * 作者：qun.liu@rogrand.com <br/>
     * 生成日期：2016年6月15日 <br/>
     *
     * @param parameter
     * @return
     * @throws KkmyException
     */
    @SuppressWarnings("unchecked")
    @Override
    public List searchLinkage(PageParam pageParam) throws KkmyException {
        String queryId = pageParam.get("queryId") + "";
        String menuId = pageParam.get("menuId") == null ? "" : pageParam.get("menuId").toString();
        String popupMenuId = pageParam.get("popupMenuId") == null ? "" : pageParam.get("popupMenuId").toString();

//		String buttonId = pageParam.get("buttonId") == null ? "" : pageParam.get("buttonId").toString();
//		String value = pageParam.get("value") == null ? "" : pageParam.get("value").toString();
//		String name = pageParam.get("name") == null ? "" : pageParam.get("name").toString();
//		Map<String, Object> tempMap = commonSearchTemplateService.getTempMap(popupMenuId);
        List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
        Map<String, Object> queryMap = (Map<String, Object>) commonSearchTemplateService.getTempQuery(popupMenuId, queryId);
        if (queryMap == null) {
            throw new KkmyException("没有找到查询配置");
        }

        conditions = (List<Map<String, Object>>) queryMap.get("process");
        if (conditions == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }
        Object processObj = queryMap.get("process");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }
        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, popupMenuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, popupMenuId, pageParam);
        }
		/*PageResult pageResult = new PageResult();
		if (pageParam != null) {
			pageResult.setPageList((List) pageParam.get("pageList"));
			pageResult.setPageParam(pageParam);
		}
		return pageResult;	*/
        if (pageParam != null) {
            return (List) pageParam.get("pageList");
        } else {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void delete(PageParam pageParam) throws KkmyException {
        String menuId = (String) pageParam.get("menuId");
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> insertMap = (Map<String, Object>) tempMap
                .get("delete");

        if (insertMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = insertMap.get("process");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }

        pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        PageResult pageResult = new PageResult();
        if (pageParam != null) {
            pageResult.setPageList((List) pageParam.get("pageList"));
            pageResult.setPageParam(pageParam);
        }

    }

    /**
     * 描述：〈查询字段是否已存在〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月28日 <br/>
     *
     * @param table 表
     * @param colum 字段
     * @param value 值
     * @return
     */
    @Override
    public boolean searchRepeat(String table, String colum, Object value) {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from ").append(table).append(" where ")
                .append(colum).append("=").append("'").append(value)
                .append("'");
        Long recordCount = commonMapper.queryCount(sql.toString());
        if (recordCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 描述：〈查询字段是否已存在 不包含本条记录〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月28日 <br/>
     *
     * @param table 表
     * @param colum 字段
     * @param value 值
     * @return
     */
    @Override
    public boolean searchRepeat(String table, String colum, Object value,
                                String key, String kvalue) {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from ").append(table).append(" where ")
                .append(colum).append("=").append("'").append(value)
                .append("'").append(" and ").append(key).append("!=")
                .append("'").append(kvalue).append("'");
        Long recordCount = commonMapper.queryCount(sql.toString());
        if (recordCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 描述：〈查询单条数据〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月28日 <br/>
     *
     * @param menuId 模板ID
     * @param key    查询的唯一标识
     * @return
     * @throws KkmyException
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map query(String menuId, String key) throws KkmyException {
        PageParam pageParam = new PageParam();
        pageParam.put("key", key);
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> updateMap = (Map<String, Object>) tempMap
                .get("update");

        if (updateMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = updateMap.get("queryProcess");
        if (processObj == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }

        pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        return pageParam;
    }

    /**
     * 描述：〈查询表头统计项〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年4月12日 <br/>
     *
     * @param string
     * @param merchant
     * @return
     * @throws KkmyException
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map queryCount(String string, PageParam pageParam)
            throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(string);
        // ////////////////处理sql标签////////////////////////////////////
        Map<String, Object> sqlMap;
        String sql;
        List<String> sqlList = null;
        int sqlArrayLength = 0;
        StringBuffer sb;
        int index = 0;
        try {
            Map map = (Map) tempMap.get("count");
            sqlMap = (JSONObject) map.get("sql");
            Object object = sqlMap.get("#text");
            if (object instanceof JSONArray) {
                sqlList = (List<String>) JSONArray
                        .toCollection((JSONArray) object);
                // sqlArray = ((String [])sqlMap.get("#text"));
            } else {
                sqlList = new ArrayList<String>();
                sqlList.add((String) sqlMap.get("#text"));
            }
            sqlArrayLength = sqlList.size();
            sql = sqlList.get(index++);
            sb = new StringBuffer(sql);
        } catch (Exception e) {
            throw new KkmyException("sql配置不正确，查询失败");
        }
        // /////////////////处理if条件////////////////////////////////
        if (sqlMap.containsKey("if")) {
            try {
                Object ifObject = sqlMap.get("if");
                List<Map<String, Object>> ifList = null;
                if (ifObject instanceof JSONArray) {
                    ifList = (List<Map<String, Object>>) sqlMap.get("if");
                } else {
                    ifList = new ArrayList<Map<String, Object>>();
                    ifList.add((Map<String, Object>) sqlMap.get("if"));
                }
                for (Map<String, Object> ifMap : ifList) {
                    String methodString = (String) ifMap.get("@method");
                    String methodParam = (String) ifMap.get("@args");
                    boolean flag = (Boolean) invokeUtilMethod(methodString,
                            methodParam, pageParam);
                    if (flag) {
                        sb.append(" " + ifMap.get("#text"));
                    }
                    if (index < sqlArrayLength) {
                        sb.append(sqlList.get(index++));
                    }
                }

                // /////////////////////将参数注入到sql////////////////////////
                sql = sb.toString();
                for (String currKey : pageParam.keySet()) {
                    // String conditionName = (String)
                    // conditionMap.get("@name");
                    Object conditionValue = pageParam.get(currKey);
                    try {
                        sql = sql
                                .replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
                                        "'%" + conditionValue.toString() + "%'")
                                .replaceAll("\\%\\$\\{" + currKey + "\\}",
                                        "'%" + conditionValue.toString() + "'")
                                .replaceAll("\\$\\{" + currKey + "\\}\\%",
                                        "'" + conditionValue.toString() + "%'")
                                .replaceAll("\\$\\{" + currKey + "\\}",
                                        "'" + conditionValue.toString() + "'");
                    } catch (Exception e) {
                    }
                }
                sql = sql.replaceAll("\n", "").replaceAll("\t", "")
                        .replaceAll("\\?", "");
            } catch (Exception e) {
                throw new KkmyException("查询条件配置错误，查询失败");
            }
        }
        try {
            Map map = commonMapper.queryRecord(sql);
            return map;
        } catch (Exception e) {
            throw new KkmyException("执行sql错误，查询失败");
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

    @Override
    public void test(PageParam pageParam) {
        // TODO Auto-generated method stub
        System.out.println("执行方法成功");
    }

    public void setCreatTime(PageParam pageParam) {
        // TODO Auto-generated method stub
        pageParam.put("create_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        pageParam.put("update_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        System.out.println("执行方法成功");
    }

    /**
     * 描述：〈获取初始值设置方法〉 <br/>
     * 作者：chenhui.yan@rogrand.com <br/>
     * 生成日期：2016年6月16日 <br/>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> defaultprocess(PageParam pageParam, Map<String, Object> tempMap, String type)
            throws KkmyException {
        List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
        if ("main".equals(type)) {
            Map<String, Object> queryMap = (Map<String, Object>) tempMap
                    .get("queryMap");
            if (queryMap == null) {
                throw new KkmyException("没有找到查询配置");
            }
            conditions = (List<Map<String, Object>>) queryMap.get("conditions");
        } else if ("add".equals(type)) {
            Map insert = (Map) tempMap.get("insert");
            if (insert == null) {
                throw new KkmyException("没有找到查询配置");
            }
            conditions = (List<Map<String, Object>>) insert.get("conditions");
        } else if ("edit".equals(type)) {
            Map update = (Map) tempMap.get("update");
            if (update == null) {
                throw new KkmyException("没有找到查询配置");
            }
            conditions = (List<Map<String, Object>>) update.get("conditions");
        } else {
            conditions = (List<Map<String, Object>>) tempMap.get("conditions");
        }

        if (conditions == null) {
            throw new KkmyException("没有找到查询的流程配置");
        }
        for (Map<String, Object> map : conditions) {
            //初始化默认值
            if (map.get("@defaultprocess") != null && !map.get("@defaultprocess").toString().equals("")) {

                String defaults = map.get("@defaultprocess").toString();
                String serviceName = defaults.substring(0, defaults.indexOf("."));
                String serviceMethod = defaults.substring(defaults.indexOf(".") + 1, defaults.length());
                pageParam = invokeSpringMethod(pageParam, serviceName, serviceMethod);
                map.put("@defaultvalue", pageParam.get("defaultvalue"));
            }
            if (map.get("@defaultvalue") != null && !map.get("@defaultvalue").toString().equals("")) {
                pageParam.put(map.get("@name").toString(), map.get("@defaultvalue"));
            }

            //判断页面跳转所带参数
//			if(pageParam.get("urlKeys")!=null&& map.get("@name").equals(pageParam.get("urlKeys")) ){
//				map.put("@defaultvalue", pageParam.get("urlValues"));
//			}
        }
        return tempMap;
    }

    /**
     * 描述：〈根据name和menuId联动控件的值〉 <br/>
     * 作者：qun.liu@rogrand.com <br/>
     * 生成日期：2016年6月23日 <br/>
     *
     * @param parameter
     * @return
     * @throws KkmyException
     */
    @Override
    public PageParam valideteProcess(PageParam pageParam) throws KkmyException {
        String process = pageParam.get("process") == null ? "" : pageParam.get("process").toString();
        String value = pageParam.get("value") == null ? "" : pageParam.get("value").toString();
        if (!StringUtil.isEmpty(process)) {
            String serviceName = process.substring(0, process.indexOf("."));
            String serviceMethod = process.substring(process.indexOf(".") + 1);
            pageParam.put("value", value);
            pageParam = invokeSpringMethod(pageParam, serviceName, serviceMethod);
        }
        return pageParam;
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    private PageParam invokeSpringMethod(PageParam pageParam,
                                         String serviceName, String serviceMethod) throws KkmyException {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        Object service = ac.getBean(serviceName);
//		Class c = service.getClass();
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

    @Override
    public Map query(String menuId, String key, String buttonId, String operate)
            throws KkmyException {
        PageParam pageParam = new PageParam();
        pageParam.put("key", key);
        Map<String, Object> buttonMap = new HashMap<String, Object>();

        if (StringUtil.isEmpty(operate)) {
            buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);
        } else {
            buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);
        }

        if (buttonMap == null) {
            throw new KkmyException("没有找到查询配置");
        }
        Object processObj = buttonMap.get("queryProcess");
        if (processObj == null) {
            return pageParam;
        }
        if (processObj instanceof Collection) {
            JSONArray json = new JSONArray();
            json.addAll((Collection) processObj);
            pageParam = doProcess(json, menuId, pageParam);
        } else {
            pageParam = doProcess((JSONArray) processObj, menuId, pageParam);
        }
        return pageParam;
    }

    //自定义初始值测试
    public PageParam defaultprocesstest(PageParam pageParam) {
        pageParam.put("defaultvalue", "77777");
        return pageParam;
    }

    //自定义初始值测试
    public PageParam checkTest(PageParam pageParam) {
        pageParam.put("mustBreak", true);
        return pageParam;
    }

    //自定义初始值测试
    public PageParam validateProcess(PageParam pageParam) {
        if (pageParam.get("value").equals("1")) {
            pageParam.put("valideteFlag", "1");
        } else {
            pageParam.put("valideteFlag", "0");
        }
        return pageParam;
    }

    //自定义初始值测试
    public String testsql(PageParam pageParam) {
        return "1=1";
    }

    public void convertValueToKey(Map row, Map condition) throws KkmyException {
        if (row == null || condition == null || condition.get("@name") == null || row.get(condition.get("@name")) == null) {
            return;
        }
        String input = (String) condition.get("@input");
        if (!StringUtil.isEmpty(input) && (input.equals("select") || input.equals("checkbox") || input.equals("radio"))) {
            String name = (String) condition.get("@name");
            String value = (String) row.get(name);
            List<SelectBean> list = getOptionList(row, condition);
            for (SelectBean bean : list) {
                if (bean.getText().equals(value)) {
                    row.put(name, bean.getId());
                }
            }
        }
    }


}
