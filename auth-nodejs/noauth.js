var Flickr = require("flickrapi");
var flickrOptions = {
  api_key: "efdee6ab4e6cb1a625bd30a67e2d0924",
  secret: "f84d041e2f7bb76a"
};

var log = console.log;

Flickr.tokenOnly(flickrOptions, function(error, flickr) {
  if (error) {
    log('error', error);
    process.exit(-1);
  }
  log('Looks good');
  // log('flickr', flickr);

  // we can now use "flickr" as our API object,
  // but we can only call public methods and access public data
  flickr.people.getPhotos({
    api_key: flickrOptions.api_key,
    user_id: '43605851@N00',
    page: 1,
    per_page: 5
  }, function(err, result) {
    /*
      This will give public results only, even if we used
      Flickr.authenticate(), because the function does not
      *require* authentication to run. It just runs with
      fewer permissions.
    */
    log('err', err);
    log('result', JSON.stringify(result,null,2));
    process.exit(0);
  });
});
