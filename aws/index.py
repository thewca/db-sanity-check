# This function is pasted in the cloudformation as inline function
# quite manual, but this should not change that much
# and it saves some steps in the build

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
