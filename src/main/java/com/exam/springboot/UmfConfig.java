package com.exam.springboot;

import cn.payingcloud.umf.TokenProvider;
import cn.payingcloud.umf.UmfClient;
import cn.payingcloud.umf.UmfProfile;
import cn.payingcloud.umf.util.CertUtils;
import com.aliyun.oss.OSSClient;
import com.ibs.pg.support.AliyunOssUtils;
import com.ibs.pg.support.channel.umf.UmfTokenProvider;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * @author YQ.Huang
 */
@Configuration
@EnableConfigurationProperties(UmfConfig.UmfProperties.class)
public class UmfConfig {

    private final UmfProperties umf;
    private final OSSClient ossClient;

    @Autowired
    public UmfConfig(UmfProperties umf, OSSClient ossClient) {
        this.umf = umf;
        this.ossClient = ossClient;
    }

    @Bean
    UmfClient umfClient() {
        PrivateKey privateKey = loadPrivateKey();
        X509Certificate certificate = loadCertificate();
        return new UmfClient(umf.getProfile(), privateKey, certificate, tokenProvider());
    }

    @Bean
    TokenProvider tokenProvider() {
        return new UmfTokenProvider(umf.getProfile(), umf.getClientId(), umf.getClientSecret());
    }

    private PrivateKey loadPrivateKey() {
        InputStream inputStream = AliyunOssUtils.download(ossClient, umf.getOssBucket(), umf.getOssKeyFile());
        return CertUtils.loadRsaPrivateKey(inputStream);
    }

    private X509Certificate loadCertificate() {
        InputStream inputStream = AliyunOssUtils.download(ossClient, umf.getOssBucket(), umf.getOssCertFile());
        return CertUtils.loadX509Certificate(inputStream);
    }

    @Validated
    @ConfigurationProperties(prefix = "umf")
    public static class UmfProperties {
        @NotNull
        private UmfProfile profile;
        @NotBlank
        private String clientId;
        @NotBlank
        private String clientSecret;
        @NotBlank
        private String ossBucket;
        @NotBlank
        private String ossKeyFile;
        @NotBlank
        private String ossCertFile;

        public UmfProfile getProfile() {
            return profile;
        }

        public void setProfile(UmfProfile profile) {
            this.profile = profile;
        }

        public String getOssBucket() {
            return ossBucket;
        }

        public void setOssBucket(String ossBucket) {
            this.ossBucket = ossBucket;
        }

        public String getOssKeyFile() {
            return ossKeyFile;
        }

        public void setOssKeyFile(String ossKeyFile) {
            this.ossKeyFile = ossKeyFile;
        }

        public String getOssCertFile() {
            return ossCertFile;
        }

        public void setOssCertFile(String ossCertFile) {
            this.ossCertFile = ossCertFile;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }
}
