# Flickr API from node
Trying to replicate authentication to fix groovy.
See: https://www.npmjs.com/package/flickrapi

- First try no authentication, using our app ke/secret
- Then try using an auth token

If I need the new auth scheme, here is an [implementation of HMAC-SHA1](http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/AuthJavaSampleHMACSignature.html)
