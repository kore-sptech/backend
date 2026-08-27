package kore.backend.config.storage;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class AwsS3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return builder.build();
    }
}