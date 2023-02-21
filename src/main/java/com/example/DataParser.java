package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;




public class DataParser implements RequestHandler<S3Event,String> {
    private final String QUEUE_URL = System.getenv("QUEUE_URL");
    private final String REGION = System.getenv("REGION");
    private final SqsClient sqsClient = SqsClient.builder()
            .region(Region.of(REGION))
            .build();
    private final S3Client s3Client = S3Client.builder()
            .region(Region.of(REGION))
            .build();
    private static final Logger LOGGER = LoggerFactory.getLogger(DataParser.class);
    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        LOGGER.info("S3 handleRequest");
        S3EventNotification.S3Entity s3Entity = s3Event.getRecords().get(0).getS3();

        String bucketName = s3Entity.getBucket().getName();
        String fileName = s3Entity.getObject().getKey();

        LOGGER.info("bucket: " + bucketName);
        LOGGER.info("file: " + fileName);

        List<String> models = getModelsFromS3(bucketName, fileName);
        sendBatchMessages(models, QUEUE_URL);
        //----delete object
        if (!models.isEmpty()){
            deleteFile(bucketName,fileName);
        }
            return "Success";
    }

    private List<String> getModelsFromS3(String bucketName, String fileName) {
        AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().build();
        try {
            final S3Object s3Object = amazonS3Client.getObject(bucketName, fileName);
            final InputStreamReader streamReader = new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8);
            final BufferedReader reader = new BufferedReader(streamReader);
            return reader.lines().filter(Predicate.not(String::isEmpty)).collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }

    }

    private void deleteFile(String bucketName, String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        LOGGER.info("Deleted Text File from S3Bucket: "+ fileName);
    }

    private void sendBatchMessages(List<String> models, String queueUrl) {
        try {
            Collection<SendMessageBatchRequestEntry> messages = models.stream()
                    .map(model -> SendMessageBatchRequestEntry.builder().id(UUID.randomUUID().toString()).messageBody(model).build()).collect(Collectors.toList());
            SendMessageBatchRequest sendMessageBatchRequest = SendMessageBatchRequest.builder()
                    .queueUrl(queueUrl)
                    .entries(messages)
                    .build();
            sqsClient.sendMessageBatch(sendMessageBatchRequest);
            LOGGER.info("Messages(Models) has been sent successfully as a batch");
        } catch (SqsException e) {
            LOGGER.error("Failed to send SQS messages: ",e.awsErrorDetails().errorMessage());
        }

    }
}