package com.eyeson.fastform.action;

import com.eyeson.fastform.bean.SelectBean;
import com.eyeson.fastform.common.*;
import com.eyeson.fastform.file.JsGridReportBase;
import com.eyeson.fastform.service.CommonSearchService;
import com.eyeson.fastform.service.CommonSearchTemplateService;
import com.eyeson.fastform.spring.SpringContextHolder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 版权：xuan.zhou融贯资讯 <br/>
 * 作者：@rogrand.com <br/>
 * 生成日期：2013-11-27 <br/>
 * 描述：商户信息 Controller
 */
@Controller("CommonSearchController")
@RequestMapping("/commonSearch")
public class commonSearchController extends BaseController {

    private final static Logger logger = LoggerFactory
            .getLogger(commonSearchController.class);

    @Autowired
    @Qualifier("commonSearchService")
    private CommonSearchService commonSearchService;

    @Autowired
    private CommonSearchTemplateService commonSearchTemplateService;
//	@Autowired
//	private FileManager fileManager;

    private PageParam modelPublic;

    protected ModelAndView getView(HttpServletRequest request, Map map) {
        String vw = takeView(request);
        if (map.get("tempVersion") != null) {
            String version = (String) map.get("tempVersion");
            if (!StringUtil.isEmpty(version) && version.equals("2")) {
                vw = vw.replaceFirst("commonSearch", "commonSearch" + version);
            }
        }
        return new ModelAndView(vw, map);
    }

    @RequestMapping("/test")
    public void test(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PageParam model = BeanUtil.wrapPageBean(request);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("success", true);
        ResponseUtils.renderJson(response, BeanUtil.toJsonString(map));
    }

    @RequestMapping("/main")
    public ModelAndView main(HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        Map<String, Object> model = new HashMap<String, Object>();
        String menuId = request.getParameter("menuId");
        String queryId = request.getParameter("queryId");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        if (templateMap == null) {
            throw new KkmyException("该页面不存在!");
        }
        String version = (String) templateMap.get("@version");
        if (!(StringUtil.isEmpty(version) || version.equals("1"))) {
            //多页面查询判断
            Map<String, Object> queryMap = (Map<String, Object>) commonSearchTemplateService.getTempQuery(menuId, queryId);
            if (queryMap == null) {
                throw new KkmyException("该页面不存在!");
            }
            if (queryMap instanceof Collection) {
                JSONArray json = new JSONArray();
                json.addAll((Collection) queryMap);
                templateMap.put("queryMap", json);
            } else {
                templateMap.put("queryMap", queryMap);
            }
            ApplicationContext ac = SpringContextHolder.getApplicationContext();
            CommonSearchService searchService = null;
            if (StringUtil.isEmpty(version)) {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService");
            } else {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService" + version);
            }
            searchService.defaultprocess(pageParam, templateMap, "main");
        }


        List models = (List) templateMap.get("model");
        List drections = (List) templateMap.get("directions");
        request.setAttribute("sizeD", drections.size());
        request.setAttribute("templateMap", templateMap);
        if (!(StringUtil.isEmpty(version) || version.equals("1"))) {
            model.put("tempVersion", version);
        }
        handleRequestInternal(request, response);
        modelPublic = pageParam;
        return getView(request, model);
    }

    @RequestMapping("/chart")
    public ModelAndView chart(HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        String menuId = request.getParameter("menuId");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> chartMap = commonSearchService.chart(menuId,
                pageParam);

        List models = (List) templateMap.get("model");
        List drections = (List) templateMap.get("directions");
        model.put("sizeD", drections.size());
        model.put("templateMap", templateMap);
        model.put("chartMap", chartMap);

        if (pageParam.get("index") != null) {
            request.setAttribute("index",
                    Integer.valueOf(pageParam.get("index").toString()));
        }
        model.put("tempVersion", templateMap.get("@version"));
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    @RequestMapping("/page")
    public void page(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PageParam merchant = BeanUtil.wrapPageBean(request);
        PageResult pageResult = commonSearchService.search(
                merchant.get("menuId").toString(), merchant);
        ResponseUtils.renderJson(response,
                EasyuiUtil.toDataGridData(pageResult));
    }

    @RequestMapping("/queryCount")
    public void queryCount(HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        PageParam merchant = BeanUtil.wrapPageBean(request);
        Map map = commonSearchService.queryCount(merchant.get("menuId")
                .toString(), merchant);
        ResponseUtils.renderJson(response, BeanUtil.toJsonString(map));
    }

    @RequestMapping("/add")
    public ModelAndView add(HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        Map<String, Object> model = new HashMap<String, Object>();
        String menuId = request.getParameter("menuId");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        //获取初始值
        String version = (String) templateMap.get("@version");
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        CommonSearchService searchService = null;
        if (StringUtil.isEmpty(version)) {
            searchService = (CommonSearchService) ac
                    .getBean("commonSearchService");
        } else {
            searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
        }
        searchService.defaultprocess(pageParam, templateMap, "add");
        request.setAttribute("templateMap", templateMap);
        model.put("tempVersion", version);
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    @RequestMapping("/addSave")
    public void addSave(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PageParam model = BeanUtil.wrapPageBean(request);
        String object = model.get("object").toString();
        JSONObject json = JSONObject.fromObject(object);
        model.putAll(commonSearchService.parseJSON2Map(model.get("object")
                .toString()));
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(json.getString("menuId"));
        Map map = (Map) templateMap.get("insert");
        List<Map> conditions = (List<Map>) map.get("conditions");
        boolean flag = false;
        for (Map condition : conditions) {
            if (null != condition.get("@repeat")
                    && "false".equals(condition.get("@repeat"))) {
                flag = commonSearchService.searchRepeat(condition.get("@table")
                        .toString(), condition.get("@name").toString(), model
                        .get(condition.get("@name")));
                if (flag) {
                    ResponseUtils.renderText(
                            response,
                            condition.get("@title").toString() + ":"
                                    + model.get(condition.get("@name"))
                                    + "  已存在,请修改后再提交");
                    return;
                }

                // return responseText(
                // response,
                // condition.get("@title").toString() + ":"
                // + model.get(condition.get("@name"))
                // + "  已存在,请修改后再提交");
            }
        }
        commonSearchService.insert(json.getString("menuId"), model);
        ResponseUtils.renderText(response, "记录添成功");
        // return responseText(response, "记录添成功");
    }

    @RequestMapping("/delete")
    public void delete(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PageParam model = BeanUtil.wrapPageBean(request);
        commonSearchService.delete(model);
        ResponseUtils.renderText(response, "记录删除成功");
    }

    @RequestMapping("/select")
    public void select(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PageParam param = BeanUtil.wrapPageBean(request);
        List<SelectBean> beans = commonSearchService
                .searchSelect(param, "main");
        ResponseUtils.renderJson(response, BeanUtil.toJsonString(beans));
        // return responseText(response, JSON.toJSONString(beans));
    }

    @RequestMapping("/selectAdd")
    public void selectAdd(HttpServletRequest request,
                          HttpServletResponse response) throws Exception {
        PageParam param = BeanUtil.wrapPageBean(request);
        List<SelectBean> beans = commonSearchService.searchSelect(param, "add");
        ResponseUtils.renderJson(response, BeanUtil.toJsonString(beans));
    }

    @RequestMapping("/selectEdit")
    public void selectEdit(HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        PageParam param = BeanUtil.wrapPageBean(request);
        List<SelectBean> beans = commonSearchService
                .searchSelect(param, "edit");
        ResponseUtils.renderJson(response, BeanUtil.toJsonString(beans));
    }

    @RequestMapping("/exportExcel")
    public ModelAndView exportExcel(HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        try {
            PageParam param = BeanUtil.wrapPageBean(request);
            param.setRows(SystemConst.EXPORT_MAX_ROWS);
            PageResult pageResult = commonSearchService.search(
                    param.get("menuId").toString(), param);
            response.setContentType("application/msexcel;charset=GBK");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String title = "通用查询结果" + format.format(new Date());
            Map<String, Object> templateMap = commonSearchTemplateService
                    .getTempMap(param.get("menuId").toString());
            List models = (List) templateMap.get("model");
            String header = "";
            String fild = "";
            for (Object object : models) {
                Map map = (Map) object;
                header = header + map.get("@title") + ",";
                fild = fild + map.get("@name") + ",";
            }
            String[] hearders = header.split(",");// 表头数组
            String[] fields = fild.split(",");// 导出对象属性数组
            TableData td = ExcelUtils.createTableData(pageResult.getPageList(),
                    ExcelUtils.createTableHeader(hearders), fields);
            JsGridReportBase report;
            report = new JsGridReportBase(request, response);
            report.exportToExcel(title, "", td);
        } catch (Exception e) {
            logger.error("导出Excel时出错，错误信息：" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/importe")
    public ModelAndView importe(HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        String menuId = request.getParameter("menuId");
        String buttonId = request.getParameter("buttonId");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        request.setAttribute("templateMap", templateMap);
        request.setAttribute("buttonId", buttonId);
        model.put("tempVersion", templateMap.get("@version"));
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    @RequestMapping("/importExcel")
    public void importExcel(HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        try {
            PageParam param = BeanUtil.wrapPageBean(request);
            String object = param.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            String menuId = json.getString("menuId");
            String res = json.getString("importFile");
            String temp = "tempDirectory";
            File destFile = new File(res);
            Map<String, Object> templateMap = commonSearchTemplateService
                    .getTempMap(menuId);
            FileInputStream fs = new FileInputStream(destFile);
            List<Map> rows = ExcelUtils.readExcelM(fs, templateMap);
            for (Map map : rows) {
                param.putAll(map);
                Map insert = (Map) templateMap.get("insert");
                List<Map> conditions = (List<Map>) insert.get("conditions");
                boolean flag = false;
                for (Map condition : conditions) {
                    if (null != condition.get("@repeat")
                            && "false".equals(condition.get("@repeat"))) {
                        flag = commonSearchService.searchRepeat(
                                condition.get("@table").toString(), condition
                                        .get("@name").toString(), param
                                        .get(condition.get("@name")));
                        if (!flag)
                            commonSearchService.insert(
                                    json.getString("menuId"), param);
                    }
                }
            }
            destFile.delete();
        } catch (Exception e) {
            logger.error("导入Excel时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "导入Excel时出错,请联系管理员");
        }
    }

    @RequestMapping("/edit")
    public ModelAndView edit(HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        Map<String, Object> model = new HashMap<String, Object>();
        String menuId = request.getParameter("menuId");
        String key = request.getParameter("key");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);

        try {
            String version = (String) templateMap.get("@version");
            ApplicationContext ac = SpringContextHolder.getApplicationContext();
            CommonSearchService searchService = null;
            if (StringUtil.isEmpty(version)) {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService");
            } else {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService" + version);
            }
            Map map = searchService.query(menuId, key);

            //获取初始值
            searchService.defaultprocess(pageParam, templateMap, "edit");
            request.setAttribute("model", map);
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            logger.error("查询列表时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        }
        request.setAttribute("templateMap", templateMap);
        model.put("tempVersion", templateMap.get("@version"));
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    @RequestMapping("/editSave")
    public void editSave(HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
        PageParam model = BeanUtil.wrapPageBean(request);
        String object = model.get("object").toString();
        JSONObject json = JSONObject.fromObject(object);
        model.putAll(commonSearchService.parseJSON2Map(model.get("object")
                .toString()));
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(json.getString("menuId"));
        Map map = (Map) templateMap.get("update");
        List<Map> conditions = (List<Map>) map.get("conditions");
        boolean flag = false;
        for (Map condition : conditions) {
            if (null != condition.get("@repeat")
                    && "false".equals(condition.get("@repeat"))) {
                flag = commonSearchService.searchRepeat(condition.get("@table")
                                .toString(), condition.get("@name").toString(), model
                                .get(condition.get("@name")),
                        map.get("key").toString(), json.get("key").toString());
                if (flag) {
                    ResponseUtils.renderText(
                            response,
                            condition.get("@title").toString() + ":"
                                    + model.get(condition.get("@name"))
                                    + "  已存在,请修改后再提交");
                    return;
                }

            }
        }
        commonSearchService.update(json.getString("menuId"), model);
        ResponseUtils.renderText(response, "记录修改成功");
    }

    @RequestMapping("/{version}/page")
    public void page(HttpServletRequest request, HttpServletResponse response,
                     @PathVariable String version) throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam merchant = BeanUtil.wrapBean(PageParam.class, request);
            PageParam p1 = new PageParam();
            p1 = modelPublic;
            p1.putAll(merchant);
            merchant.clear();
            merchant.putAll(p1);
            //merchant.putAll(modelPublic);
            //modelPublic.clear();
            if (request.getParameter("inputkey") != null && !StringUtil.isEmpty(request.getParameter("inputkey").toString())) {
                if (request.getParameter(merchant.get("inputkey").toString()) != null && !StringUtil.isEmpty(request.getParameter(merchant.get("inputkey").toString()).toString())) {
                    String inputvalue = URLDecoder.decode(merchant.get(merchant.get("inputkey")).toString(), "UTF-8");
                    merchant.put(merchant.get("inputkey").toString(), inputvalue);
                }
            }
            PageResult pageResult = searchService.search(merchant.get("menuId")
                    .toString(), merchant);
            ResponseUtils.renderJson(response,
                    EasyuiUtil.toDataGridData(pageResult));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            logger.error("查询列表时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        }

    }

    @RequestMapping("/{version}/dynamicViewPage")
    public void dynamicViewPage(HttpServletRequest request, HttpServletResponse response,
                                @PathVariable String version) throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam merchant = BeanUtil.wrapBean(PageParam.class, request);
            if (request.getParameter("inputkey") != null && !StringUtil.isEmpty(request.getParameter("inputkey").toString())) {
                if (request.getParameter(merchant.get("inputkey").toString()) != null && !StringUtil.isEmpty(request.getParameter(merchant.get("inputkey").toString()).toString())) {
                    String inputvalue = URLDecoder.decode(merchant.get(merchant.get("inputkey")).toString(), "UTF-8");
                    merchant.put(merchant.get("inputkey").toString(), inputvalue);
                }
            }
            PageResult pageResult = searchService.searchDynamicViewPage(merchant.get("menuId")
                    .toString(), merchant);
            ResponseUtils.renderJson(response,
                    EasyuiUtil.toDataGridData(pageResult));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            logger.error("查询列表时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        }

    }

    /**
     * 描述：打开工具窗口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-19 <br/>
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/tooltip")
    public ModelAndView tooltip(HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        Map<String, Object> model = new HashMap<String, Object>();
        String menuId = request.getParameter("menuId");
        String buttonId = request.getParameter("buttonId");
        String operate = request.getParameter("operate");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> buttonMap = new HashMap<String, Object>();
        if (StringUtil.isEmpty(operate)) {
            buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);
        } else {
            buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);
        }
        request.setAttribute("operate", operate);

        request.setAttribute("templateMap", templateMap);
        request.setAttribute("buttonMap", buttonMap);
        try {
            String version = (String) templateMap.get("@version");
            ApplicationContext ac = SpringContextHolder.getApplicationContext();
            CommonSearchService searchService = null;
            if (StringUtil.isEmpty(version)) {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService");
            } else {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService" + version);
            }
            //定义初始值
            searchService.defaultprocess(pageParam, buttonMap, "dynamicView");
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("打开窗口出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("打开窗口出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "打开窗口出错,请联系管理员");
        }

        String conditionName = request.getParameter("conditionName");
        if (!StringUtil.isEmpty(conditionName)) {
            model.put("conditionName", conditionName);
        }
        if (!StringUtil.isEmpty(buttonId)) {
            model.put("buttonId", buttonId);
        }

        model.put("tempVersion", templateMap.get("@version"));
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    /**
     * 描述：打开工具窗口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-19 <br/>
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/openPopup")
    public ModelAndView openPopup(HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        Map<String, Object> model = new HashMap<String, Object>();
        String menuId = request.getParameter("menuId");
        String buttonId = request.getParameter("buttonId");
        String operate = request.getParameter("operate");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> buttonMap = new HashMap<String, Object>();
        if (StringUtil.isEmpty(operate)) {
            buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);
        } else {
            buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);
        }
        request.setAttribute("operate", operate);

        request.setAttribute("templateMap", templateMap);
        request.setAttribute("buttonMap", buttonMap);
        try {
            String version = (String) templateMap.get("@version");
            ApplicationContext ac = SpringContextHolder.getApplicationContext();
            CommonSearchService searchService = null;
            if (StringUtil.isEmpty(version)) {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService");
            } else {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService" + version);
            }
            //定义初始值
            searchService.defaultprocess(pageParam, buttonMap, "dynamicView");
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("打开窗口出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("打开窗口出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "打开窗口出错,请联系管理员");
        }

        String conditionName = request.getParameter("conditionName");
        if (!StringUtil.isEmpty(conditionName)) {
            model.put("conditionName", conditionName);
        }
        if (!StringUtil.isEmpty(buttonId)) {
            model.put("buttonId", buttonId);
        }

        model.put("tempVersion", templateMap.get("@version"));
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    @RequestMapping("/{version}/queryCount")
    public void queryCount(HttpServletRequest request,
                           HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam merchant = BeanUtil.wrapBean(PageParam.class, request);
            Map map = searchService.queryCount(merchant.get("menuId")
                    .toString(), merchant);
            ResponseUtils.renderJson(response, BeanUtil.toJsonString(map));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }
    }

    @RequestMapping("/{version}/addSave")
    public void addSave(HttpServletRequest request,
                        HttpServletResponse response, @PathVariable String version)
            throws Exception {
        // return responseText(response, "记录添成功");
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam model = BeanUtil.wrapPageBean(request);
            String object = model.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            model.putAll(searchService.parseJSON2Map(model.get("object")
                    .toString()));
            Map<String, Object> templateMap = commonSearchTemplateService
                    .getTempMap(json.getString("menuId"));
            Map map = (Map) templateMap.get("insert");
            List<Map> conditions = (List<Map>) map.get("conditions");
            boolean flag = false;
            for (Map condition : conditions) {
                if (null != condition.get("@repeat")
                        && "false".equals(condition.get("@repeat"))) {
                    flag = searchService.searchRepeat(condition.get("@table")
                                    .toString(), condition.get("@name").toString(),
                            model.get(condition.get("@name")));
                    if (flag) {
                        ResponseUtils.renderText(response,
                                condition.get("@title").toString() + ":"
                                        + model.get(condition.get("@name"))
                                        + "  已存在,请修改后再提交");
                        return;
                    }
                    // return responseText(
                    // response,
                    // condition.get("@title").toString() + ":"
                    // + model.get(condition.get("@name"))
                    // + "  已存在,请修改后再提交");
                }
            }
            searchService.insert(json.getString("menuId"), model);
            ResponseUtils.renderText(response, "记录添成功");
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }

    }

    @RequestMapping("/{version}/delete")
    public void delete(HttpServletRequest request,
                       HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam model = BeanUtil.wrapPageBean(request);
            searchService.delete(model);
            ResponseUtils.renderText(response, "记录删除成功");
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }
    }

    @RequestMapping("/{version}/select")
    public void select(HttpServletRequest request,
                       HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam param = BeanUtil.wrapPageBean(request);
            String type = "main";
            if (param.get("type") != null) {
                type = param.get("type").toString();
            }
            List<SelectBean> beans = searchService.searchSelect(param, type);
            ResponseUtils.renderJson(response, BeanUtil.toJsonString(beans));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }

    }

    @RequestMapping("/{version}/selectAdd")
    public void selectAdd(HttpServletRequest request,
                          HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            //PageParam param = BeanUtil.wrapPageBean(request);
            PageParam param = BeanUtil.wrapBean(PageParam.class, request);
            List<SelectBean> beans = searchService.searchSelect(param, "add");
            ResponseUtils.renderJson(response, BeanUtil.toJsonString(beans));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }
    }

    @RequestMapping("/{version}/searchLinkage")
    public void searchLinkage(HttpServletRequest request,
                              HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            //PageParam model = BeanUtil.wrapPageBean(request);
            PageParam model = BeanUtil.wrapBean(PageParam.class, request);
            String object = model.get("object") != null ? model.get("object").toString() : "";
            if (!StringUtil.isEmpty(object)) {
                JSONObject json = JSONObject.fromObject(object);
//				model.putAll(commonSearchService.parseJSON2Map(model.get("object")
//						.toString()));
                model.putAll(BeanUtil.wrapBean(HashMap.class, model.get("object").toString()));
            }
            List list = searchService.searchLinkage(model);
            if (list != null && list.size() > 0 && list.get(0) != null) {
                ResponseUtils.renderJson(response, BeanUtil.toJsonString(list));
            } else {
                ResponseUtils.renderJson(response, BeanUtil.toJsonString(""));
            }
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }
    }

    @RequestMapping("/{version}/valideteProcess")
    public void valideteProcess(HttpServletRequest request,
                                HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            //PageParam model = BeanUtil.wrapPageBean(request);
            PageParam model = BeanUtil.wrapBean(PageParam.class, request);
            model = searchService.valideteProcess(model);
            String flag = (String) model.get("valideteFlag");
            ResponseUtils.renderJson(response, flag);
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }
    }

    @RequestMapping("/popupSelect")
    public ModelAndView popupSelect(HttpServletRequest request,
                                    HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        //PageParam pageParam = BeanUtil.wrapPageBean(request);
        PageParam pageParam = BeanUtil.wrapBean(PageParam.class, request);
        String menuId = request.getParameter("menuId");
        String inputkey = request.getParameter("inputkey");
        String inputvalue = request.getParameter("inputvalue");
        String popupMenuId = request.getParameter("popupMenuId");
        String selectId = request.getParameter("selectId");
        String type = request.getParameter("type");
        String tableid = request.getParameter("tableid");
        String value = request.getParameter("value");
        String editindex = request.getParameter("editindex");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(popupMenuId);
        List models = (List) templateMap.get("model");
        List drections = (List) templateMap.get("directions");
        model.put("sizeD", drections.size());
        model.put("templateMap", templateMap);
        model.put("selectId", selectId);
        model.put("value", value);
        model.put("type", type);
        model.put("tableid", tableid);
        model.put("editindex", editindex);
        if (!StringUtil.isEmpty(inputvalue)) {
            inputvalue = URLDecoder.decode(inputvalue, "UTF-8");
            model.put("inputvalue", inputvalue);
            if (!StringUtil.isEmpty(inputkey)) {
                model.put("inputkey", inputkey);
            }
        }

        if (pageParam.get("index") != null) {
            request.setAttribute("index",
                    Integer.valueOf(pageParam.get("index").toString()));
        }
        model.put("tempVersion", templateMap.get("@version"));
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    @RequestMapping("/{version}/selectEdit")
    public void selectEdit(HttpServletRequest request,
                           HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam param = BeanUtil.wrapPageBean(request);
            List<SelectBean> beans = searchService.searchSelect(param, "edit");
            ResponseUtils.renderJson(response, BeanUtil.toJsonString(beans));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            throw new KkmyException("模板配置不正确！");
        }
    }

    @RequestMapping("/{version}/exportExcel")
    public ModelAndView exportExcel(HttpServletRequest request,
                                    HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam param = BeanUtil.wrapBean(PageParam.class, request);
            param.setRows(SystemConst.EXPORT_MAX_ROWS);
            PageResult pageResult = searchService.search(param.get("menuId")
                    .toString(), param);
            response.setContentType("application/msexcel;charset=GBK");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String title = "通用查询结果" + format.format(new Date());
            String queryId = param.get("queryId") == null ? null : param.get("queryId").toString();
            Map<String, Object> queryMap = commonSearchTemplateService.getTempQuery(param.get("menuId")
                    .toString(), queryId);
            List models = (List) queryMap.get("model");
            String header = "";
            String fild = "";
            for (Object object : models) {
                Map map = (Map) object;
                header = header + map.get("@title") + ",";
                fild = fild + map.get("@name") + ",";
            }
            String[] hearders = header.split(",");// 表头数组
            String[] fields = fild.split(",");// 导出对象属性数组
            TableData td = ExcelUtils.createTableData(pageResult.getPageList(),
                    ExcelUtils.createTableHeader(hearders), fields);
            JsGridReportBase report;
            report = new JsGridReportBase(request, response);
            report.exportToExcel(title, "", td);
            return null;
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            throw new KkmyException("模板配置不正确！");
        } catch (Exception e) {
            logger.error("导出Excel时出错，错误信息：" + e.getMessage());
            throw new KkmyException("模板配置不正确！");
        }
    }

    @RequestMapping("/{version}/importExcel")
    public void importExcel(HttpServletRequest request,
                            HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam param = BeanUtil.wrapPageBean(request);
            String object = param.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            String menuId = json.getString("menuId");
            String buttonId = json.getString("buttonId");
            String res = json.getString("importFile");
            String temp = "tempDirectory";
            File destFile = new File(res);
            Map<String, Object> byttonMap = commonSearchTemplateService
                    .getTempButton(buttonId, menuId);
            FileInputStream fs = new FileInputStream(destFile);
            List<Map> rows = ExcelUtils.readExcelForButton(fs, byttonMap);
            for (Map map : rows) {
                List<Map> conditions = (List<Map>) byttonMap.get("conditions");
                boolean flag = false;
                for (Map condition : conditions) {
                    searchService.convertValueToKey(map, condition);
                    if (null != condition.get("@repeat")
                            && "false".equals(condition.get("@repeat"))) {
                        flag = searchService.searchRepeat(
                                condition.get("@table").toString(), condition
                                        .get("@name").toString(), map
                                        .get(condition.get("@name")));
                        if (flag) {
                            break;
                        }
                    }
                }
                param.putAll(map);
                if (!flag)
                    searchService.buttonExecute(menuId, param, buttonId);
            }
            destFile.delete();
            ResponseUtils.renderJson(response, "导入成功!");
        } catch (NoSuchBeanDefinitionException e) {
            e.printStackTrace();
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("导入Excel时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "导入Excel时出错,请联系管理员");
        }
    }

    @RequestMapping("/{version}/editSave")
    public void editSave(HttpServletRequest request,
                         HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam model = BeanUtil.wrapPageBean(request);
            String object = model.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            model.putAll(searchService.parseJSON2Map(model.get("object")
                    .toString()));
            Map<String, Object> templateMap = commonSearchTemplateService
                    .getTempMap(json.getString("menuId"));
            Map map = (Map) templateMap.get("update");
            List<Map> conditions = (List<Map>) map.get("conditions");
            boolean flag = false;
            for (Map condition : conditions) {
                if (null != condition.get("@repeat")
                        && "false".equals(condition.get("@repeat"))) {
                    flag = searchService.searchRepeat(condition.get("@table")
                                    .toString(), condition.get("@name").toString(),
                            model.get(condition.get("@name")), map.get("key")
                                    .toString(), json.get("key").toString());
                    if (flag) {
                        ResponseUtils.renderText(response,
                                condition.get("@title").toString() + ":"
                                        + model.get(condition.get("@name"))
                                        + "  已存在,请修改后再提交");
                        return;
                    }
                }
            }
            searchService.update(json.getString("menuId"), model);
            ResponseUtils.renderText(response, "记录修改成功");
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            logger.error("修改时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "修改时出错,请联系管理员");
        }
    }

    /**
     * 描述：自定义按钮保存方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-19 <br/>
     *
     * @param request
     * @param response
     * @param version
     * @throws Exception
     */
    @RequestMapping("/{version}/dynamicSave")
    public void dynamicSave(HttpServletRequest request,
                            HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam model = BeanUtil.wrapBean(PageParam.class, request);
            String object = model.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            String menuId = json.getString("menuId");
            String buttonId = null;
            String operate = null;
            if (json.containsKey("buttonId")) {
                buttonId = json.getString("buttonId");
            }
            if (json.containsKey("operate")) {
                operate = json.getString("operate");
            }
            model.putAll(BeanUtil.wrapBean(HashMap.class, model.get("object").toString()));
            //Map map = (Map) commonSearchTemplateService.getTempButton(buttonId, menuId);
            Map<String, Object> map = new HashMap<String, Object>();
            if (StringUtil.isEmpty(operate)) {
                map = commonSearchTemplateService.getTempButton(buttonId, menuId);
            } else {
                map = commonSearchTemplateService.getTempOperate(buttonId, menuId);
            }


            List<Map> conditions = (List<Map>) map.get("conditions");
            boolean flag = false;
            if (conditions != null) {
                for (Map condition : conditions) {
                    if (null != condition.get("@repeat")
                            && "false".equals(condition.get("@repeat"))) {
                        if (map.get("key") != null && map.get("key") != null) {
                            String currentKeyName = map.get("key")
                                    .toString();
//							String currentKey = json.get("key").toString();
                            String currentKey = json.get(currentKeyName).toString();
                            model.put("key", currentKey);
                            flag = searchService.searchRepeat(condition.get("@table")
                                            .toString(), condition.get("@name").toString(),
                                    model.get(condition.get("@name")), currentKeyName, currentKey);
                        } else {
                            flag = commonSearchService.searchRepeat(condition.get("@table")
                                    .toString(), condition.get("@name").toString(), model
                                    .get(condition.get("@name")));
                        }

                        if (flag) {
                            model.put("checkMessage", condition.get("@title").toString() + ":"
                                    + model.get(condition.get("@name"))
                                    + "  已存在,请修改后再提交");
                            ResponseUtils.renderJsonResult(response, condition.get("@title").toString() + ":"
                                    + model.get(condition.get("@name"))
                                    + "  已存在,请修改后再提交", false);
                            return;
                        }
                    }
                    if (null != condition.get("@validate") && null != condition.get("@validateprocess")) {
                        if (condition.get("@validateprocess").toString().indexOf(".") > 0) {
                            model.put("process", condition.get("@validateprocess"));
                            if (!StringUtil.isEmpty(model.get(condition.get("@name")).toString())) {
                                model.put("value", model.get(condition.get("@name")));
                                model = searchService.valideteProcess(model);
                                String type = (String) model.get("valideteFlag");
                                if (!"1".equals(type)) {
                                    ResponseUtils.renderJsonResult(response, condition.get("@title").toString() + ":"
                                            + model.get(condition.get("@name"))
                                            + "  错误,请修改后再提交", false);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            searchService.buttonExecute(menuId, model, buttonId);
            ResponseUtils.renderText(response, BeanUtil.toJsonString(model));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "查询列表时出错,请联系管理员", false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "修改时出错,请联系管理员", false);
        }
    }

    /**
     * 上传文件
     *
     * @param request  请求对象
     * @param response 响应对象
     * @return ModelAndView
     * @throws Exception 异常
     */
    @RequestMapping("/upload")
    public void upload(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String maxRequestSize = request.getParameter("maxRequestSize");
        String width = request.getParameter("width");
        String height = request.getParameter("height");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> map = multipartRequest.getFileMap();
        MultipartFile multipartFile = multipartRequest.getFile("file_path");
        File file = new File(getServletContext().getRealPath("/pictures") + "/"
                + multipartFile.hashCode());
        multipartFile.transferTo(file);
        String path1 = file.getAbsolutePath();
//		FileUpLoad fileUpLoad = FileUpLoad.createInstance(getServletContext(),
//				request);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("result", "success");
        model.put("path", path1);
        String json = BeanUtil.toJsonString(model);
        ResponseUtils.renderTextHtml(response, json);
    }

    /**
     * 描述：自定义按钮打开弹框方法，如果按钮配置中query操作则查询key对应数据，如果没有则不查数据 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-19 <br/>
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/dynamicView")
    public ModelAndView dynamicView(HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        //PageParam pageParam = BeanUtil.wrapPageBean(request);
        PageParam pageParam = BeanUtil.wrapBean(PageParam.class, request);
        Map<String, Object> model = new HashMap<String, Object>();
        String menuId = request.getParameter("menuId");
        String key = request.getParameter("key");
        String buttonId = request.getParameter("buttonId");
        String operate = request.getParameter("operate");
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> buttonMap = new HashMap<String, Object>();
        if (StringUtil.isEmpty(operate)) {
            buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);
        } else {
            buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);
        }
        request.setAttribute("operate", operate);
        request.setAttribute("key", key);

        Map<String, Object> queryMap = new HashMap<String, Object>();
        if (StringUtil.isEmpty(operate)) {
            queryMap = (Map<String, Object>) commonSearchTemplateService.getTempButtonQuery(menuId, "");
        } else {
            queryMap = (Map<String, Object>) commonSearchTemplateService.getTempOperateQuery(menuId, "");
        }
        if (queryMap != null) {
            if (queryMap instanceof Collection) {
                JSONArray json = new JSONArray();
                json.addAll((Collection) queryMap);
                templateMap.put("queryMap", json);
            } else {
                templateMap.put("queryMap", queryMap);
            }
        }

        request.setAttribute("templateMap", templateMap);
        request.setAttribute("buttonMap", buttonMap);
        try {
            String version = (String) templateMap.get("@version");
            ApplicationContext ac = SpringContextHolder.getApplicationContext();
            CommonSearchService searchService = null;
            if (StringUtil.isEmpty(version)) {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService");
            } else {
                searchService = (CommonSearchService) ac
                        .getBean("commonSearchService" + version);
            }
            if (buttonMap != null && buttonMap.get("@channel") != null && buttonMap.get("@channel").toString().indexOf("query") >= 0) {
                Map map = searchService.query(menuId, key, buttonId, operate);
                request.setAttribute("model", map);
            }
            //定义初始值
            searchService.defaultprocess(pageParam, buttonMap, "dynamicView");
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("打开窗口出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "查询列表时出错,请联系管理员");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("打开窗口出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJson(response, "打开窗口出错,请联系管理员");
        }

        model.put("tempVersion", templateMap.get("@version"));
        handleRequestInternal(request, response);
        return getView(request, model);
    }

    /**
     * 描述：自定义按钮保存方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-19 <br/>
     *
     * @param request
     * @param response
     * @param version
     * @throws Exception
     */
    @RequestMapping("/{version}/dynamicCheck")
    public void dynamicCheck(HttpServletRequest request,
                             HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
//			PageParam model = BeanUtil.wrapPageBean(request);
            PageParam model = BeanUtil.wrapBean(PageParam.class, request);
            String object = model.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            String menuId = json.getString("menuId");
            String buttonId = json.getString("buttonId");
            model.putAll(searchService.parseJSON2Map(model.get("object")
                    .toString()));

            searchService.dynamicCheck(menuId, model, buttonId);
            ResponseUtils.renderJson(response, BeanUtil.toJsonString(model));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "查询列表时出错,请联系管理员", false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "修改时出错,请联系管理员", false);
        }
    }

    /**
     * 描述：自定义操作保存方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-19 <br/>
     *
     * @param request
     * @param response
     * @param version
     * @throws Exception
     */
    @RequestMapping("/{version}/operateCheck")
    public void operateCheck(HttpServletRequest request,
                             HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
//			PageParam model = BeanUtil.wrapPageBean(request);
            PageParam model = BeanUtil.wrapBean(PageParam.class, request);
            String object = model.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            String menuId = json.getString("menuId");
            String buttonId = json.getString("buttonId");
            model.putAll(searchService.parseJSON2Map(model.get("object")
                    .toString()));

            searchService.operateCheck(menuId, model, buttonId);
            ResponseUtils.renderJson(response, BeanUtil.toJsonString(model));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "查询列表时出错,请联系管理员", false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "修改时出错,请联系管理员", false);
        }
    }

    /***
     * 描述：列表行修改 <br/>
     * 作者：chenhui.yan@rogrand.com <br/>
     * 生成日期：2016-6-22 <br/>
     * @param request
     * @param response
     * @param version
     * @throws Exception
     */
    @RequestMapping("/{version}/update")
    public void update(HttpServletRequest request,
                       HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam model = BeanUtil.wrapBean(PageParam.class, request);
            String object = model.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            String menuId = json.getString("menuId");
            model.putAll(searchService.parseJSON2Map(model.get("object").toString()));

            Map<String, Object> templateMap = commonSearchTemplateService.getTempMap(menuId);
            Map map = (Map) templateMap.get("update");
            List<Map> conditions = (List<Map>) map.get("conditions");
            boolean flag = false;
            if (conditions != null) {
                for (Map condition : conditions) {
                    if (null != condition.get("@repeat")
                            && "false".equals(condition.get("@repeat"))) {
                        if (map.get("key") != null && map.get("key") != null) {
                            String currentKeyName = map.get("key")
                                    .toString();
                            String currentKey = json.get("key").toString();
                            flag = searchService.searchRepeat(condition.get("@table")
                                            .toString(), condition.get("@name").toString(),
                                    model.get(condition.get("@name")), currentKeyName, currentKey);
                        } else {
                            flag = commonSearchService.searchRepeat(condition.get("@table")
                                    .toString(), condition.get("@name").toString(), model
                                    .get(condition.get("@name")));
                        }
                        if (flag) {
                            model.put("checkMessage", condition.get("@title").toString() + ":"
                                    + model.get(condition.get("@name"))
                                    + "  已存在,请修改后再提交");
                            ResponseUtils.renderJsonResult(response, condition.get("@title").toString() + ":"
                                    + model.get(condition.get("@name"))
                                    + "  已存在,请修改后再提交", false);
                            return;
                        }
                    }
                }
            }
            searchService.update(menuId, model);
            model.put("checkMessage", "修改成功");
            for (Map.Entry<String, Object> m : model.entrySet()) {
                if (m.getValue() == null || m.getValue().equals("null")) {
                    model.put(m.getKey(), "");
                }
            }
            ResponseUtils.renderText(response, BeanUtil.toJsonString(model));
        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "查询列表时出错,请联系管理员", false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "修改时出错,请联系管理员", false);
        }
    }


    /**
     * 描述：批量修改 <br/>
     * 作者：chenghui.yan@rogrand.com <br/>
     * 生成日期：2016-7-25 <br/>
     *
     * @param request
     * @param response
     * @param version
     * @throws Exception
     */
    @RequestMapping("/{version}/updateBatch")
    public void updateBatch(HttpServletRequest request,
                            HttpServletResponse response, @PathVariable String version)
            throws Exception {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        try {
            CommonSearchService searchService = (CommonSearchService) ac
                    .getBean("commonSearchService" + version);
            PageParam model = BeanUtil.wrapBean(PageParam.class, request);
            String object = model.get("object").toString();
            JSONObject json = JSONObject.fromObject(object);
            String menuId = json.getString("menuId");
            String buttonId = null;
            String operate = null;
            if (json.containsKey("buttonId")) {
                buttonId = json.getString("buttonId");
            }
            if (json.containsKey("operate")) {
                operate = json.getString("operate");
            }
            model.putAll(BeanUtil.wrapBean(HashMap.class, model.get("object").toString()));
            //Map map = (Map) commonSearchTemplateService.getTempButton(buttonId, menuId);
//			Map<String, Object> map = new HashMap<String, Object>();
//			if(StringUtil.isEmpty(operate)){
//				map = commonSearchTemplateService.getTempButton(buttonId, menuId);
//			}else{
//				map = commonSearchTemplateService.getTempOperate(buttonId, menuId);
//			}

            searchService.buttonExecute(menuId, model, buttonId);
            ResponseUtils.renderText(response, BeanUtil.toJsonString(model));

//			List<Map> conditions = (List<Map>) map.get("conditions");
//			boolean flag = false;
//			if(conditions != null){
//				for (Map condition : conditions) {
//					if (null != condition.get("@repeat")
//							&& "false".equals(condition.get("@repeat"))) {
//						if(map.get("key") != null && map.get("key") != null){
//							String currentKeyName = map.get("key")
//									.toString();
////							String currentKey = json.get("key").toString();
//							String currentKey = json.get(currentKeyName).toString();
//							model.put("key", currentKey);
//							flag = searchService.searchRepeat(condition.get("@table")
//									.toString(), condition.get("@name").toString(),
//									model.get(condition.get("@name")), currentKeyName, currentKey);
//						}else {
//							flag = commonSearchService.searchRepeat(condition.get("@table")
//									.toString(), condition.get("@name").toString(), model
//									.get(condition.get("@name")));
//						}
//						
//						if (flag) {
//							model.put("checkMessage", condition.get("@title").toString() + ":"
//											+ model.get(condition.get("@name"))
//											+ "  已存在,请修改后再提交");
//							ResponseUtils.renderJsonResult(response, condition.get("@title").toString() + ":"
//									+ model.get(condition.get("@name"))
//									+ "  已存在,请修改后再提交",false);
//							return;
//						}
//					}
//					if(null != condition.get("@validate") && null != condition.get("@validateprocess")){
//						if(condition.get("@validateprocess").toString().indexOf(".")>0){
//							model.put("process", condition.get("@validateprocess"));
//							if(!StringUtil.isEmpty(model.get(condition.get("@name")).toString())){
//								model.put("value", model.get(condition.get("@name")));
//								model = searchService.valideteProcess(model);
//								String type = (String)model.get("valideteFlag");
//								if(!"1".equals(type)){
//									ResponseUtils.renderJsonResult(response, condition.get("@title").toString() + ":"
//											+ model.get(condition.get("@name"))
//											+ "  错误,请修改后再提交",false);
//									return;
//								}
//							}
//						}
//					}
//				}
//			}


        } catch (NoSuchBeanDefinitionException e) {
            logger.error("查询列表时出错，请检查模板文件版本号.错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "查询列表时出错,请联系管理员", false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改时出错，错误信息：" + e.getMessage());
            ResponseUtils.renderJsonResult(response, "修改时出错,请联系管理员", false);
        }
    }

    /**
     * 描述：打开工具窗口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-19 <br/>
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/{version}/dynamicLink")
    public void dynamicLink(HttpServletRequest request,
                            HttpServletResponse response, @PathVariable String version) throws Exception {
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        PageParam model = BeanUtil.wrapBean(PageParam.class, request);
        String object = model.get("object").toString();
        JSONObject json = JSONObject.fromObject(object);
        String menuId = json.getString("menuId");
        String buttonId = json.getString("buttonId");
        String operate = null;
        if (json.get("operate") != null) {
            operate = json.getString("operate");
        }

        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map<String, Object> buttonMap = new HashMap<String, Object>();
        if (StringUtil.isEmpty(operate)) {
            buttonMap = commonSearchTemplateService.getTempButton(buttonId, menuId);
        } else {
            buttonMap = commonSearchTemplateService.getTempOperate(buttonId, menuId);
        }
        Map<String, Object> linkMap = (Map<String, Object>) buttonMap.get("link");

        ResponseUtils.renderText(response, BeanUtil.toJsonString(linkMap));
    }

    @RequestMapping("/gotoPostLink")
    public ModelAndView gotoPostLink(HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        PageParam pageParam = BeanUtil.wrapPageBean(request);
        Map<String, Object> model = new HashMap<String, Object>();
        String url = request.getParameter("url");
        String dataJson = request.getParameter("dataJson");
        return getView(request, model);
    }

//	/**
//	 * 文件上传到七牛
//	 * @param request
//	 * @param response
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping("/uploads")
//	public ModelAndView uploads(HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//    	MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//    	Map<String, Object> model = new HashMap<String, Object>();
//    	model.put("result", "errer");
//		Map<String, MultipartFile> map = multipartRequest.getFileMap();
//		  Iterator it = map.entrySet().iterator();
//		  while (it.hasNext()) {
//		   Map.Entry entry = (Map.Entry) it.next();
//		   Object key = entry.getKey();
//		   Object value = entry.getValue();
//		   MultipartFile multipartFile = (MultipartFile)value;
//		   File file = new File(getServletContext().getRealPath("/pictures") + "/"
//					+multipartFile.hashCode()+"."+ multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".")+1));
//			multipartFile.transferTo(file);
////		   CommonsMultipartFile cf= (CommonsMultipartFile)multipartFile;
////	        DiskFileItem fi = (DiskFileItem)cf.getFileItem();
////	        File f = fi.getStoreLocation();
//	        Result<UploadData> result = fileManager.upload(file, BusinessType.FILE_UPLOAD_IMAGE_PUBLIC,false,new Date());
//			System.out.println("返回结果：FTP路径："+result.getData().getFtpPath()+"  ,  七牛路径："+result.getData().getCdnUrl());
//			file.delete();
//			model.put("picUrl", fileManager.getFileUrl(result.getData().getKey(),  BusinessType.FILE_UPLOAD_IMAGE_PUBLIC));
//		    model.put("result", "success");
//			model.put("oldname", multipartFile.getOriginalFilename());
//		  }
//
//		//MultipartFile multipartFile = multipartRequest.getFile("file_path");
//        String json = BeanUtil.toJsonString(model);
//        return responseText(response, json);
//
//	}

}
