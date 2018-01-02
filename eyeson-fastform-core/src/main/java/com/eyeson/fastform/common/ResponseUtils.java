package com.eyeson.fastform.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 版权：融贯资讯 <br/>
 * 作者：xuan.zhou@rogrand.com <br/>
 * 生成日期：2014-1-10 <br/>
 * 描述：〈HttpServletResponse 帮助类〉
 */
public final class ResponseUtils {

    public static final Logger log = LoggerFactory.getLogger(ResponseUtils.class);

    /**
     * 描述：〈发送文本，使用UTF-8编码〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-1-10 <br/>
     *
     * @param response HttpServletResponse
     * @param text     文本内容
     */
    public static void renderText(HttpServletResponse response, String text) {
        render(response, "text/plain;charset=UTF-8", text);
    }

    /**
     * 描述：〈发送文本，使用UTF-8编码〉 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-7 <br/>
     *
     * @param response HttpServletResponse
     * @param text     文本内容
     */
    public static void renderTextHtml(HttpServletResponse response, String text) {
        render(response, "text/html;charset=UTF-8", text);
    }

    /**
     * 描述：〈发送Json，使用UTF-8编码〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-1-10 <br/>
     *
     * @param response HttpServletResponse
     * @param text     Json文本
     */
    public static void renderJson(HttpServletResponse response, String text) {
        render(response, "application/json;charset=UTF-8", text);
    }

    /**
     * 描述：〈发送Json，使用UTF-8编码〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-1-10 <br/>
     *
     * @param response HttpServletResponse
     * @param text     Json文本
     */
    public static void renderJsonResult(HttpServletResponse response, String text, boolean success) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("commonResult", success);
        resultMap.put("checkMessage", text);
        renderJson(response, BeanUtil.toJsonString(resultMap));
    }

    /**
     * 描述：〈发送xml，使用UTF-8编码〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-1-10 <br/>
     *
     * @param response HttpServletResponse
     * @param text     Xml文本
     */
    public static void renderXml(HttpServletResponse response, String text) {
        render(response, "text/xml;charset=UTF-8", text);
    }

    /**
     * 描述：〈发送内容，使用UTF-8编码〉 <br/>
     * 作者：xuan.zhou@rogrand.com <br/>
     * 生成日期：2014-1-10 <br/>
     *
     * @param response    HttpServletResponse
     * @param contentType contentType
     * @param text        内容
     */
    public static void render(HttpServletResponse response, String contentType, String text) {
        response.setContentType(contentType);
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        try {
            response.getWriter().write(text);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
