resource "aws_s3_bucket" "fileserver" {
  bucket = "sd-fileserver-dev"

  tags = {
    Project     = "sd-services"
    Environment = "dev"
  }
}

# Block public access to the bucket to ensure that only your applications (via IAM roles) can access it.
resource "aws_s3_bucket_public_access_block" "fileserver_block" {
  bucket = aws_s3_bucket.fileserver.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
