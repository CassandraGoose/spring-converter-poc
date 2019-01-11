package com.devetry.converter;

import java.io.File;
import java.io.IOException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3Controller {
  private String filePath;
  private String applicant;
  private String region;
  private String bucketName;
  private String key;
  private String secret;

  public S3Controller(String filePath, String applicant, String region, String bucketName, String key, String secret) {
    this.filePath = filePath;
    this.applicant = applicant;
    this.region = region;
    this.bucketName = bucketName;
    this.key = key;
    this.secret = secret;
  }

  public String sendToS3() throws IOException, AmazonServiceException, SdkClientException {

    File file = new File(filePath);

    AWSCredentials credentials = new BasicAWSCredentials(key,
        secret);

    AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(region).build();

    try {
      s3client.putObject(bucketName, file.getName(), file);
    } catch (AmazonServiceException e) {
      e.printStackTrace();
      return "There was an error uploading to S3.";
    } catch (SdkClientException e) {
      e.printStackTrace();
      return "There was an error uploading to S3.";
    }
    return "Successfully uploaded to S3. (hopefully)(maybe)";
  }

}