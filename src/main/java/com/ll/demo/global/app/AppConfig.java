package com.ll.demo.global.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private static String activeProfile;

    public static String getGenFileDirPath() {
        if (isProd()) {
            return "/src/main/resources/static/imgFiles";
        }
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\imgFiles";
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            path = System.getProperty("user.dir") + "/src/main/resources/static/imgFiles";
        }
        return path;
    }


    @Value("${spring.profiles.active}")
    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public static boolean isProd() {
        return activeProfile.equals("prod");
    }

    public static boolean isDev() {
        return activeProfile.equals("dev");
    }

    public static boolean isTest() {
        return activeProfile.equals("Test");
    }

    public static boolean isNotProd() {
        return !isProd();
    }

    @Getter
    private static String siteFrontUrl;

    @Value("${custom.site.frontUrl}")
    public void setSiteFrontUrl(String siteFrontUrl) {
        this.siteFrontUrl = siteFrontUrl;
    }

    @Getter
    private static String devFrontUrl;

    @Value("${custom.dev.frontUrl}")
    public void setDevFrontUrl(String devFrontUrl) {
        this.devFrontUrl = devFrontUrl;
    }

    @Getter
    private static String siteBackUrl;

    @Value("${custom.site.backUrl}")
    public void setSiteBackUrl(String siteBackUrl) {
        this.siteBackUrl = siteBackUrl;
    }

    @Getter
    private static String siteCookieDomain;

    @Value("${custom.site.cookieDomain}")
    public void setSiteCookieDomain(String siteCookieDomain) {
        this.siteCookieDomain = siteCookieDomain;
    }
    @Getter
    public static String siteName;

    @Value("${custom.site.name}")
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    @Getter
    public static ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Getter
    public static int basePageSize = 10;


    @Getter
    private static String tossPaymentsWidgetSecretKey;

    @Value("${custom.tossPayments.widget.secretKey}")
    public void setTossPaymentsWidgetSecretKey(String tossPaymentsWidgetSecretKey) {
        this.tossPaymentsWidgetSecretKey = tossPaymentsWidgetSecretKey;
    }


    @Getter
    private static int orderCancelableSeconds;

    @Value("${custom.order.cancelableSeconds}")
    public void setOrderCancelableSeconds(int orderCancelableSeconds) {
        this.orderCancelableSeconds = orderCancelableSeconds;
    }

    @Getter
    private static double rebateRate;

    @Value("${custom.rebate.rate}")
    public void setRebateRate(double rebateRate) {
        this.rebateRate = rebateRate;
    }
}
