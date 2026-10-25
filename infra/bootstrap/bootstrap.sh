#!/usr/bin/env bash
set -euo pipefail

BUCKET_NAME="sampedro2002-catalog-terraform-state"
REGION="us-east-1"
TABLE_NAME="terraform-locks"

echo "Creando bucket S3: $BUCKET_NAME en $REGION..."
aws s3api create-bucket --bucket "$BUCKET_NAME" --region "$REGION"

echo "Habilitando versionado en el bucket S3..."
aws s3api put-bucket-versioning --bucket "$BUCKET_NAME" --versioning-configuration Status=Enabled

echo "Creando tabla DynamoDB: $TABLE_NAME para bloqueo de estado..."
aws dynamodb create-table \
  --table-name "$TABLE_NAME" \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region "$REGION"

echo "Bootstrap completado exitosamente."
