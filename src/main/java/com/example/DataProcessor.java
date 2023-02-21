package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.example.model.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor implements RequestHandler<SQSEvent, String> {

    private final String QUEUE_URL = System.getenv("QUEUE_URL");
    private final String REGION = System.getenv("REGION");
    private final String TABLE_NAME = System.getenv("TABLE_NAME");

    private final DynamoDbClient ddb = DynamoDbClient.builder()
            .region(Region.of(REGION))
            .build();

    private final DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(ddb)
            .build();

    private final DynamoDbTable<VehicleModel> mappedTable = enhancedClient
            .table(TABLE_NAME, TableSchema.fromBean(VehicleModel.class));

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessor.class);
    @Override
    public String handleRequest(SQSEvent event, Context context) {
        LOGGER.info("SQS handleRequest");

        List<SQSEvent.SQSMessage> sqsMessages = event.getRecords();
        List<String> messageReceipts = ddbPersist(sqsMessages);
        List<DeleteMessageResponse> response = deleteSQSMessages(messageReceipts);

        return String.format("%s %d","Messages persisted and deleted:", response.size());
    }



    private List<String> ddbPersist(List<SQSEvent.SQSMessage> messages) {
        List<String> messageReceipts = new ArrayList<>();
        messages.forEach(message -> {
            LOGGER.info("Table :"+ TABLE_NAME);
            try {
                VehicleModel model = new VehicleModel(message.getBody());
                VehicleModel ItemModel = mappedTable.getItem(model);

                if(ItemModel!=null){
                    ItemModel.setCount(ItemModel.getCount()+1);
                    mappedTable.updateItem(ItemModel);
                    LOGGER.info("updateItem  :"+ ItemModel.toString());
                }else {
                    model.setCount(1);
                    mappedTable.putItem(model);
                    LOGGER.info("putItem  :"+ ItemModel.toString());
                }
                messageReceipts.add(message.getReceiptHandle());

            } catch (DynamoDbException e) {
                LOGGER.error("DynamoDbException"+ e);
            }
        });
        return messageReceipts;
    }

    private List<DeleteMessageResponse> deleteSQSMessages(List<String> messageReceipts) {
        List<DeleteMessageResponse> deletedMessages = new ArrayList<>();

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.of(REGION))
                .build();
        messageReceipts.forEach(mr -> {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .receiptHandle(mr)
                    .build();

           DeleteMessageResponse response = sqsClient.deleteMessage(deleteMessageRequest);
            LOGGER.info("response " + response.sdkHttpResponse().isSuccessful() + " " + response.sdkHttpResponse().statusCode() + " " + response.sdkHttpResponse().statusText().get());
            deletedMessages.add(response);
        });

        return deletedMessages;
    }

}
