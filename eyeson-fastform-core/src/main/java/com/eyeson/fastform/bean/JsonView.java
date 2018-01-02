/**
 * @(#)JsonView.java Copyright 2011 jointown, Inc. All rights reserved.
 */
package com.eyeson.fastform.bean;

import org.springframework.ui.Model;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * 自定义 JSON View
 *
 * @author jianguo.xu
 * @version 1.0, 2011-4-27
 */
public class JsonView extends AbstractView {

    public static final String DEFAULT_JSON_KEY = "default_json";

    protected String jsonKey;
    protected Object object;

    public JsonView(String jsonKey, Model model) {
        this(jsonKey, model.asMap());
    }

    /**
     * @param jsonKey 对应json配置文件中item节点的name属性
     * @param object  需要转换的json的对象
     * @author jianguo.xu
     */
    public JsonView(String jsonKey, Object object) {
        this.jsonKey = jsonKey;
        this.object = object;
    }

    /**
     * @param object 需要转换的json的对象
     * @author jianguo.xu
     */
    public JsonView(Object object) {
        this(DEFAULT_JSON_KEY, object);
    }

    /**
     * @param success 是否成功
     * @param msg     消息 可为null
     * @author jianguo.xu
     */
    public JsonView(boolean success, String msg) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("success", success);
        model.put("msg", msg);
        this.jsonKey = DEFAULT_JSON_KEY;
        this.object = model;
    }

    /**
     * @param model spring model对象
     * @author jianguo.xu
     */
    public JsonView(Model model) {
        this(model.asMap());
    }

    public void addModel(String key, Object object) {
        Map<String, Object> model = (Map<String, Object>) this.object;
        model.put(key, object);
        this.object = model;
    }

    @Override
    protected void renderMergedOutputModel(@SuppressWarnings("rawtypes") Map model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {
//    	System.out.println(123);
//    	ResponseUtils.renderJson(response, JsonUtils.toJsonString(model));
////        ResponseUtils.writeJSON(request, response, object, jsonKey);
    }

}
