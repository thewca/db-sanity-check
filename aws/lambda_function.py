import json

import boto3

client = boto3.client('batch')


def lambda_handler(event, context):
    response = client.submit_job(
        jobName='SanityCheck',
        jobDefinition='SanityCheckJob',
        jobQueue='SanityCheckQueue',
    )
    return {
        "statusCode": 200,
        "headers": {
            "Content-Type": "application/json"
        },
        "body": json.dumps({
            "response ": json.dumps(response)
        })
    }


# if __name__ == "__main__":
#     event = {}
#     context = {}
#     print(lambda_handler(event, context))
