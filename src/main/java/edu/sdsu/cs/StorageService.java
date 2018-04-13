package edu.sdsu.cs;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.net.URL;
import java.util.UUID;

/**
 * @author Tom Paulus
 * Created on 4/13/18.
 */
@Log4j
public class StorageService {
    private static StorageService ourInstance = new StorageService();
    private static String bucketName = Config.getProperty("s3.source_bucket");
    private AmazonS3 s3client;

    private StorageService() {
        s3client = AmazonS3ClientBuilder
                .standard()
                .withRegion(Config.getRegion())
                .withCredentials(Config.getCredentials())
                .build();
    }

    public static StorageService getInstance() {
        return ourInstance;
    }

    public URL uploadFile(File file) {
        String s3FileName = UUID.randomUUID().toString() + file.getName().substring(file.getName().lastIndexOf(".") + 1);
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName,
                s3FileName,
                file);
        PutObjectResult result = s3client.putObject(putObjectRequest);

        URL url = s3client.getUrl(bucketName, s3FileName);
        log.debug("File Resource URL - " + url.toString());
        log.debug("PutObjectRequest - " + s3client.getCachedResponseMetadata(putObjectRequest));

        return url;
    }
}
