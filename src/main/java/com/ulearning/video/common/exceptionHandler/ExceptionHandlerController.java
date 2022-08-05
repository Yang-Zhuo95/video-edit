package com.ulearning.video.common.exceptionHandler;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.ulearning.video.common.exception.CustomizeException;
import com.ulearning.video.common.exception.DataInconsistentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.naming.NoPermissionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionHandlerController extends DefaultHandlerExceptionResolver {
    private static final int REQUEST_ERROR_PARAM_REQUIRED = 1001;
    private static final int REQUEST_ERROR_PARAM_FORMAT_FAULT = 1101;
    private static final int REQUEST_ERROR_PARAM_DATA_INCONSISTENT = 1201;
    private static final int REQUEST_ERROR_TIME_OUT_WEI_YUN = 1301;
    private final static Log logger = LogFactory.getLog(ExceptionHandlerController.class);

    @ExceptionHandler
    @ResponseBody
    public void handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request,
            HttpServletResponse response) {
        FieldError fe = ex.getBindingResult().getFieldError();
        int code = getBadRequestCode(fe.getCode());
        String message = fe.getDefaultMessage();
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
        printResponse(HttpServletResponse.SC_BAD_REQUEST,
                toJson(code, message), response);
    }


    private int getBadRequestCode(String errorType) {
        if ("NOTNULL".equals(errorType.toUpperCase())) {
            return REQUEST_ERROR_PARAM_REQUIRED;
        } else {
            return REQUEST_ERROR_PARAM_FORMAT_FAULT;
        }
    }

    @ExceptionHandler
    public ModelAndView handleException(Exception ex, HttpServletRequest request,
                                        HttpServletResponse response) {
        ModelAndView mav = doResolveException(request, response, null, ex);
        if (mav == null) {
            mav = handleUncatchedException(ex, request, response, null);
        }
        return mav;
    }

    @Override
    protected ModelAndView handleTypeMismatch(TypeMismatchException ex,
                                              HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (logger.isWarnEnabled()) {//TODO 警告
            logger.warn("url:" + getUrl(request) + ".Failed to bind request element: " + ex);
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return new ModelAndView();
    }

    private ModelAndView handleUncatchedException(Exception ex,
                                                  HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
        String url = getUrl(request);
        ex.printStackTrace();

        String requestBody = getRequestBody(request);
        logger.error("catch global exception:url=" + url + "---" + "\n" + "requestBody：" + requestBody, ex);
        try {
//			String serverinternalerror = I18nUtil.getValueByKey("serverinternalerror");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getMessage(ex, "服务器内部错误"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return new ModelAndView();
    }

    public String getRequestBody(HttpServletRequest httpServletRequest) {
        if (ContentCachingRequestWrapper.class.isAssignableFrom(httpServletRequest.getClass())) {
            ContentCachingRequestWrapper contentCachingRequestWrapper = (ContentCachingRequestWrapper) httpServletRequest;
            String body = StrUtil.str(contentCachingRequestWrapper.getContentAsByteArray(), Charset.forName(contentCachingRequestWrapper.getCharacterEncoding()));
            return body;
        }
        return "";
    }

    private String getUrl(HttpServletRequest request) {
        String url = request.getRequestURI();
        if (request.getQueryString() != null) {
            url += request.getQueryString();
        }
        return url;
    }

    @ExceptionHandler
    @ResponseBody
    public void handleDataInconsistentException(DataInconsistentException ex,
                                                HttpServletRequest request, HttpServletResponse response
    ) {
//		String datainconsistencyerror = I18nUtil.getValueByKey("datainconsistencyerror");
        String message = getMessage(ex, "数据不一致错误");
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
//		logService.error(logger, "error_service", message, userId,
//				Params.create(userId), ex);
        printResponse(HttpServletResponse.SC_BAD_REQUEST,
                toJson(REQUEST_ERROR_PARAM_DATA_INCONSISTENT, message),
                response);
    }

    @ExceptionHandler
    @ResponseBody
    public void handleMissingServletRequestParameterException(MissingServletRequestParameterException ex,
                                                              HttpServletRequest request, HttpServletResponse response
    ) {
//		String theparameterisempty = I18nUtil.getValueByKey("theparameterisempty");
        String message = getMessage(ex, "参数为空");
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
//		logService.error(logger, "error_service", message, userId, Params.create(userId), ex);
        printResponse(HttpServletResponse.SC_BAD_REQUEST,
                toJson(REQUEST_ERROR_PARAM_REQUIRED, message),
                response);
    }

    @ExceptionHandler
    @ResponseBody
    public void handleConstraintViolationException(ValidationException ex,
                                                   HttpServletRequest request, HttpServletResponse response
    ) {
        String message = ex.getLocalizedMessage();
        message = setchMessage(message);
        if (null == message) {
            //String theparameterisempty = I18nUtil.getValueByKey("theparameterisempty");
            message = getMessage(ex, "参数为空");
        }
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
        //logService.error(logger, "error_service", message, userId,
        //Params.create(userId), ex);
        printResponse(HttpServletResponse.SC_BAD_REQUEST,
                toJson(REQUEST_ERROR_PARAM_REQUIRED, message),
                response);
    }

    /**
     * 用于抓取嵌套的类中字段验证信息（暂定）
     * 格式如："Validation failed for classes
     * [com.ulearning.ulms.course.model.LabelModel]	 * during persist time for groups [javax.validation.groups.Default, ]\n
     * List of constraint violations:[\n\tConstraintViolationImpl
     * {interpolatedMessage='标签名长度只能在2-20之间', propertyPath=name,
     * rootBeanClass=class com.ulearning.ulms.course.model.LabelModel,
     * messageTemplate='标签名长度只能在2-20之间'}\n
     * @return
     * @Title: setchMessage
     */
    private String setchMessage(String message) {
        int messageIndex = message.indexOf("messageTemplate=");
        if (messageIndex == -1) {
            return null;
        }
        return message.substring(messageIndex + 17, message.lastIndexOf("'"));
    }
    /*
     * @ExceptionHandler
     *
     * @ResponseBody public void handleUnsupportedOperationException(
     * UnsupportedOperationException ex, HttpServletResponse response) { String
     * message = getMessage(ex, "不支持的操作");
     * printResponse(HttpServletResponse.SC_FORBIDDEN, toJson(message),
     * response); }
     */

    @ExceptionHandler
    @ResponseBody
    public void handleNoPermissionException(NoPermissionException ex,
                                            HttpServletRequest request, HttpServletResponse response
    ) {
//		String illegaloperation = I18nUtil.getValueByKey("illegaloperation");
        String message = getMessage(ex, "操作不合法");
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
//		logService.error(logger, "error_service", message, userId,
//				Params.create(userId), ex);
        printResponse(HttpServletResponse.SC_FORBIDDEN, toJson(message),
                response);
    }


//	@ExceptionHandler
//	@ResponseBody
//	public void handleTimeOutException(TimeOutException ex,
//									   HttpServletRequest request, HttpServletResponse response
//	) {
////		String illegaloperation = I18nUtil.getValueByKey("illegaloperation");
//		String message = getMessage(ex, "操作不合法");
//		String userId = String.valueOf((Integer) request.getAttribute("userId") == null?
//				-1 : (Integer) request.getAttribute("userId"));
////		logService.error(logger, "error_service", message, userId,
////				Params.create(userId), ex);
//		printResponse(HttpServletResponse.SC_FORBIDDEN, toJson(REQUEST_ERROR_TIME_OUT_WEI_YUN,message),
//				response);
//	}

    @ExceptionHandler
    @ResponseBody
    public void handleCustomizeException(CustomizeException ex,
                                         HttpServletRequest request, HttpServletResponse response
    ) {
//		String illegaloperation = I18nUtil.getValueByKey("illegaloperation");
        String message = getMessage(ex, ex.getMessage());
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
//		logService.error(logger, "error_service", message, userId,
//				Params.create(userId), ex);
        printResponse(ex.getStatusCode(),
                toJson(ex.getResultCode(), message),
                response);
    }

    @ExceptionHandler
    @ResponseBody
    public void handleIllegalArgumentException(IllegalArgumentException ex,
                                               HttpServletRequest request, HttpServletResponse response
    ) {
//		String unknownparametererror = I18nUtil.getValueByKey("unknownparametererror");
        String message = getMessage(ex, "未知参数错误");
        String userId = String.valueOf((Integer) request.getAttribute("userId") == null ?
                -1 : (Integer) request.getAttribute("userId"));
        String url = getUrl(request);
        ex.printStackTrace();

        String requestBody = getRequestBody(request);
        logger.error("catch global exception:url=" + url + "---" + "\n" + "requestBody：" + requestBody, ex);
        printResponse(HttpServletResponse.SC_BAD_REQUEST,
                toJson(REQUEST_ERROR_PARAM_FORMAT_FAULT, message), response);
    }

    private String getMessage(Exception ex, String defaultMessage) {
        String message = ex.getMessage();
        if (message == null || "".equals(message.trim())) {
            return defaultMessage;
        } else if (message.length() > 1000) {
            return message.substring(0, 1000) + "...";
        }
        return message;
    }

    private void printResponse(int status, String content,
                               HttpServletResponse response) {
        PrintWriter pw = null;
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Methods", "*");
            pw = response.getWriter();
            pw.print(content);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private String toJson(String message) {
        StringWriter sw = new StringWriter();
        JsonGenerator jg;
        try {
            jg = new JsonFactory().createGenerator(sw);
            jg.writeStartObject();
            jg.writeStringField("message", message);
            jg.writeEndObject();
            jg.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    private String toJson(int code, String message) {
        StringWriter sw = new StringWriter();
        JsonGenerator jg;
        try {
            jg = new JsonFactory().createGenerator(sw);
            jg.writeStartObject();
            jg.writeNumberField("code", code);
            jg.writeStringField("message", message);
            jg.writeEndObject();
            jg.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return sw.toString();
    }
}
