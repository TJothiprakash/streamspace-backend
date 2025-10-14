package com.jp.streamspace.vidoestream.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class StorageService {

    private final S3Client s3;
    private final S3Presigner presigner;

    @Value("${wasabi.bucket}")
    private String bucket;

    public StorageService(S3Client s3, S3Presigner presigner) {
        this.s3 = s3;
        this.presigner = presigner;
    }

    /**
     * Generate presigned PUT URL for client to upload the object directly.
     * expirationSeconds: 900 (15 min) default.
     */
    public String presignPut(String key, int expirationSeconds) {
        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ) // optional, choose as per your policy
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .putObjectRequest(por)
                .build();

        return presigner.presignPutObject(presignRequest).url().toString();
    }

    /**
     * HEAD object to verify it exists in bucket.
     */
    public boolean doesObjectExist(String key) {
        try {
            HeadObjectRequest head = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3.headObject(head);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    public String objectUrl(String key) {
        // public URL format for Wasabi S3 endpoint
        // user may want a different host; using endpoint+bucket pattern
        // You already have wasabi.endpoint, but easiest is s3.wasabisys.com/bucket/key
        return String.format("%s/%s/%s", "https://" + bucket + ".s3.wasabisys.com", bucket, key);
    }
}
