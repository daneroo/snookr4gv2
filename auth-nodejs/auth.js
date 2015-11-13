var Flickr = require("flickrapi");
var flickrOptions = {
  api_key: process.env.FLICKR_API_KEY,
  secret: process.env.FLICKR_SECRET,
  // copy the values here after the first run, or export as instruction state
  user_id: process.env.FLICKR_USER_ID,
  access_token: process.env.FLICKR_ACCESS_TOKEN,
  access_token_secret: process.env.FLICKR_ACCESS_TOKEN_SECRET,
  permissions: 'write' // read,write or delete : default if not set is read
};

var log = console.log;

if (!flickrOptions.api_key || !flickrOptions.secret) {
  log('Provide at least Application tokens: FLICKR_API_KEY, FLICKR_SECRET');
  log('  Application API keypair: https://www.flickr.com/services/api/keys/');
  process.exit(-1);

}
log('flickrOptions: ', JSON.stringify(flickrOptions, null, 2));

Flickr.authenticate(flickrOptions, function(error, flickr) {
  if (error) {
    log('error', error);
    process.exit(-1);
  }
  log('Looks good we are authenticated');

  // now validate the permissions
  flickr.auth.oauth.checkToken({
    api_key: flickrOptions.api_key,
    oauth_token: flickrOptions.access_token
  }, function(err, result) {
    if (error) {
      log('checkToken:error', error);
      process.exit(-1);
    }
    // log('result', JSON.stringify(result, null, 2));
    log ('user: %j has permissions: %s',result.oauth.user,result.oauth.perms._content);
    process.exit(0);
  });

});
