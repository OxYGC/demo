package com.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.web.client.RestClient;

/*
 * 天气远程MCP工具
 */
public class WeatherTool {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherTool() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.weather.gov")
                .defaultHeader("Accept", "application/geo+json")
                .defaultHeader("User-Agent", "WeatherApiClient/1.0 (your@email.com)")
                .build();
    }

    @Tool(description = "根据经纬度获取天气预报")
    public String getWeatherForecast(double latitude, double longitude) {
        log.info("Getting weather forecast for latitude: {} and longitude: {}", latitude, longitude);
        try {
            String uri = String.format("/points/%s,%s", latitude, longitude);

            String response = restClient.get()
                    .uri(uri).retrieve().body(String.class);

            // 解析JSON，获取 forecast 链接
            JsonNode jsonNode = objectMapper.readTree(response);
            String forecastUrl = null;
            if (jsonNode.has("properties") && jsonNode.get("properties").has("forecast")) {
                forecastUrl = jsonNode.get("properties").get("forecast").asText();
            }
            if (forecastUrl == null || forecastUrl.isEmpty()) {
                log.warn("No forecast url found in API response properties!");
                return "未能在返回结果中获取到天气预报链接。";
            }

            // 二次请求真正的天气预报内容
            // forecastUrl 可能是个完整URL，RestClient 的 baseUrl 只对相对路径生效，需处理（但通常天气.gov的URL是完整绝对路径，需新建RestClient或直接用Java传统HTTP，或者RestClient.withUrl）。
            String forecastResponse = RestClient.create()
                    .get()
                    .uri(forecastUrl)
                    .retrieve()
                    .body(String.class);
            log.debug("Forecast detail API response received: {}", forecastResponse);
            return forecastResponse;

        } catch (Exception e) {
            log.info("Error getting weather forecast for lat:{} lon:{}", latitude, longitude, e);
            return "获取天气预报时出错：" + e.getMessage();
        }
    }

    /**
     * @param area: AL, AK, AS, AR, AZ, CA, CO, CT, DE, DC, FL, GA, GU, HI, ID, IL, IN, IA, KS, KY, LA, ME, MD, MA, MI, MN, MS, MO, MT, NE, NV, NH, NJ, NM, NY, NC, ND, OH, OK, OR, PA, PR, RI, SC, SD, TN, TX, UT, VT, VI, VA, WA, WV, WI, WY, MP, PW, FM, MH
     * @return 指定地区的灾害预警信息
     */
    @Tool(description = "获取指定美国地区的灾害预警信息")
    public String getWeatherAlerts(String area) {
        try {
            String response = restClient.get()
                    .uri("/alerts/active/area/{zone}", area)
                    .retrieve()
                    .body(String.class);
            log.debug("Weather alerts API response received: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error getting weather alerts for area: {}", area, e);
            throw e;
        }
    }
}