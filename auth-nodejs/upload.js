var Flickr = require("flickrapi");
var flickrOptions = {
  api_key: process.env.FLICKR_API_KEY,
  secret: process.env.FLICKR_SECRET,
  // copy the values here after the first run, or export as instruction state
  user_id: process.env.FLICKR_USER_ID,
  access_token: process.env.FLICKR_ACCESS_TOKEN,
  access_token_secret: process.env.FLICKR_ACCESS_TOKEN_SECRET
};

var log = console.log;

log('flickrOptions: ', JSON.stringify(flickrOptions, null, 2));

Flickr.authenticate(flickrOptions, function(error, flickr) {
  if (error) {
    log('error', error);
    process.exit(-1);
  }
  log('Looks good, authenticated');

  var uploadOptions = {
    photos: [{
      title: "test",
      tags: [
        "snookrd",
        "snookr:md5=4f1bd9ac2e1bee52ebd19ce7c0af76a5"
      ],
      photo: __dirname + "/2014-03-27T11.15.26-4f1bd9ac2e1bee52ebd19ce7c0af76a5-test.jpg"
    }]
  };

  Flickr.upload(uploadOptions, flickrOptions, function(err, result) {
    if (err) {
      return console.error(error);
    }
    console.log("photos uploaded", result);
    process.exit(0);
  });


});
