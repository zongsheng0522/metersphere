package io.metersphere.api.parse;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.Request;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ApiImportAbstractParser implements ApiImportParser {

    protected String getApiTestStr(InputStream source) {
        BufferedReader bufferedReader;
        StringBuilder testStr = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8));
            testStr = new StringBuilder();
            String inputStr;
            while ((inputStr = bufferedReader.readLine()) != null) {
                testStr.append(inputStr);
            }
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        } finally {
            try {
                source.close();
            } catch (IOException e) {
                MSException.throwException(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        }
        return testStr.toString();
    }

    protected void setScenarioByRequest(Scenario scenario, ApiTestImportRequest request) {
        if (request.getUseEnvironment()) {
            scenario.setEnvironmentId(request.getEnvironmentId());
        }
    }

    protected void addContentType(Request request, String contentType) {
        addHeader(request, HttpHeader.CONTENT_TYPE.toString(), contentType);
    }

    protected void addCookie(Request request, String key, String value) {
        List<KeyValue> headers = Optional.ofNullable(request.getHeaders()).orElse(new ArrayList<>());
        boolean hasCookie = false;
        for (KeyValue header : headers) {
            if (StringUtils.equalsIgnoreCase(HttpHeader.COOKIE.name(), header.getName())) {
                hasCookie = true;
                String cookies = Optional.ofNullable(header.getValue()).orElse("");
                header.setValue(cookies + key + "=" + value + ";");
            }
        }
        if (!hasCookie) {
            addHeader(request, HttpHeader.COOKIE.name(), key + "=" + value + ";");
        }
    }

    protected void addHeader(Request request, String key, String value) {
        List<KeyValue> headers = Optional.ofNullable(request.getHeaders()).orElse(new ArrayList<>());
        boolean hasContentType = false;
        for (KeyValue header : headers) {
            if (StringUtils.equalsIgnoreCase(header.getName(), key)) {
                hasContentType = true;
            }
        }
        if (!hasContentType) {
            headers.add(new KeyValue(key, value));
        }
        request.setHeaders(headers);
    }
}
