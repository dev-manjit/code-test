## Description
## A Lambda function read messages from text file which uploaded in  S3bucket, and send each message to a SQS queue, store them into a DynamoDB table, then removes them from the SQS queue and Text file if operation is success

This pattern creates one S3 bucket,a SQS queue, a Lambda function and a DynamoDB table using SAM and Java 11.

## Language:
####This is a Maven project which uses Java 11 and AWS SDK

## Framework

The framework used to deploy the infrastructure is SAM

## Services used

The AWS services used in this pattern are
#### AWSS3 - AWS Lambda - Amazon SQS - DynamoDB


## Description
The SAM template contains all the information to deploy AWS resources(one S3 bucket, one Lambda function, one SQS queue and one DynamoDB table)
and also the permission required by these service to communicate.

You will be able to create and delete the CloudFormation stack using the CLI commands.

The lambda function will read messages in batch, will store them into the DynamoDB table, then will remove messages from the queue.

This is fully functional example developed in Java 11.

## Deployment commands

````
mvn clean package

# create an S3 bucket where the source code will be stored:
aws s3 mb s3://bucket9999codetest

# copy the source code located in the target folder:
aws s3 cp target/sourceCode.zip s3://bucket9999codetest

# SAM will deploy the CloudFormation stack described in the template.yml file:
sam deploy --s3-bucket bucket9999codetest --stack-name stack9999codetest --capabilities CAPABILITY_IAM

````

## Testing

Upload text file(vehicle-model.tx at root of code-test project) to Amazon S3>Buckets>sourceconfigbck

you can verify the change with AWS managenment consdole

   Logs: CloudWatch>Log groups>/aws/lambda/DataParser for file reading, sending msg to queue, delete file from s3bucket
         CloudWatch>Log groups>/aws/lambda/DataProcessor processing the queue msg, delete msg, Dynamodb saving as entity model
    DB:  DynamoDB> VehicleModel ,see vehicle model and count, first upload: volvo(2),Renault(1)
    Bucket: S3>Buckets>sourceconfigbck , deleted the file after parsing the text
    Lambda montior
    SQS

To test the endpoint first send data using the following command. Be sure to update the endpoint with endpoint of your stack.

```
# first send a few messages to the SQS queue
aws sqs send-message --queue-url https://sqs.YOUR_AWS_REGION.amazonaws.com/YOUR_AWS_ACCOUNT/MessageQueue --message-body '{"modelId":"volvo"}'
aws sqs send-message --queue-url https://sqs.YOUR_AWS_REGION.amazonaws.com/YOUR_AWS_ACCOUNT/MessageQueue --message-body '{"orderId":"BMW"}'

# scan the dynamodb table
aws dynamodb scan --table-name OrdersTable

```

## Cleanup

Run the given command to delete the resources that were created. It might take some time for the CloudFormation stack to get deleted.
```
aws cloudformation delete-stack --stack-name stack9999codetest

aws s3 rm s3://bucket9999codetest --recursive

aws s3 rb s3://bucket9999codetest
```

## Requirements

* [Create an AWS account](https://portal.aws.amazon.com/gp/aws/developer/registration/index.html) if you do not already have one and log in. The IAM user that you use must have sufficient permissions to make necessary AWS service calls and manage AWS resources.
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) installed and configured
* [Git Installed](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
* [AWS Serverless Application Model](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html) (AWS SAM) installed



## Author bio
Name: Manjit

