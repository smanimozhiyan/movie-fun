package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {
    private final Logger log = LoggerFactory.getLogger(BlobStore.class);
    private AmazonS3Client amazonS3Client = null;
    private String photoStorageBucket = null;

    public S3Store(AmazonS3Client amazonS3Client, String photoStorageBucket) {
        this.amazonS3Client = amazonS3Client;
        this.photoStorageBucket = photoStorageBucket;

        if (!amazonS3Client.doesBucketExist(photoStorageBucket)) {
            log.info("New bucket created  : " + photoStorageBucket);
            amazonS3Client.createBucket(photoStorageBucket);
        }
    }

    @Override
    public void put(Blob blob) throws IOException {
        log.info("S3store put method invoked with Bold : " + blob);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        objectMetadata.setContentLength(blob.inputStream.available());
        amazonS3Client.putObject(photoStorageBucket, blob.name, blob.inputStream, objectMetadata);
        log.info("S3store put method added successfully !!!");

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        log.info("S3store get method invoked with Name : " + name);
        if (amazonS3Client.doesObjectExist(photoStorageBucket, name)) {
            S3Object s3Object = amazonS3Client.getObject(photoStorageBucket, name);
            Blob blob = new Blob(s3Object.getKey(), s3Object.getObjectContent(), s3Object.getObjectMetadata().getContentType());
            log.info("S3store get method returns blob : " + blob);
            return Optional.of(blob);
        } else {
            log.info("S3store get method Object not available : " + name);
            return Optional.empty();
        }


    }

    @Override
    public void deleteAll() {
        amazonS3Client.deleteBucket(photoStorageBucket);
    }
}
