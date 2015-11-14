package net.snookr.util;

import net.snookr.flickr.Photos;

class Environment {
    // Flickr Stuff for snookerNet

    // Application API keypair: https://www.flickr.com/services/api/keys/
    static String api_key=System.getenv("FLICKR_API_KEY");
    static String secret=System.getenv("FLICKR_SECRET");

    // These are obtained from: cd node-auth; node auth.js
    static String user_id=System.getenv("FLICKR_USER_ID");
    static String access_token=System.getenv("FLICKR_ACCESS_TOKEN");
    static String access_token_secret=System.getenv("FLICKR_ACCESS_TOKEN_SECRET");

    static String yapFile = "snookr.yap";

    public static void check() {
        println "Flickr Creds:";
        println "  FLICKR_API_KEY = api_key = oauth_consumer_key    : ${Environment.api_key}";
        println "  FLICKR_SECRET = secret                           : ${Environment.secret}";
        println "  FLICKR_USER_ID = user_id                         : ${Environment.user_id}";
        println "  FLICKR_ACCESS_TOKEN = access_token               : ${Environment.access_token}";
        println "  FLICKR_ACCESS_TOKEN_SECRET = access_token_secret : ${Environment.access_token_secret}";

        if (!api_key || !secret || !user_id || !access_token || !access_token_secret) {
            println "Missing at least one environment variable!"
            System.exit(0);
        }
        // File f = new File('auth-nodejs/2014-03-27T11.15.26-4f1bd9ac2e1bee52ebd19ce7c0af76a5-test.jpg');
        // def photoid = new Photos().uploadPhoto(f);
        // if (photoid==-1){
        //     println "Photo was already present"
        // } else {
        //     println "Photo was uploaded at id:${photoid}"
        // }
        // System.exit(0);
    }
}
