package net.snookr.flickr;

import net.snookr.util.Environment;
import net.snookr.util.MD5;
import net.snookr.util.SHA1;

class Flickr {

    // you probably want to use Photo.uploadFile(f) which checks for md5tag first.
    String uploadPhoto(File f) {
        return uploadPhoto(f,null);
    }
    String uploadPhoto(File f,String md5tag) {
        // the Photos version checks if alread exists.
        println "uploading ${f} : ${f.getCanonicalPath()}";
        // calculate md5 sum if not passed as param.
        if (!md5tag) md5tag = "snookr:md5=${MD5.digest(f)}";

        def baos = new ByteArrayOutputStream();
        baos << f.newInputStream(); /* not sure InputStream is closed...*/
        byte[] b = baos.toByteArray();
        return postMultipart(["title":f.getName(),"photo":b,"tags":"\"snookrd\" \"${md5tag}\""])
    }
    String setPostedDate(String photoid,Date posted) { // Does this imply a Timezone! works for me
        long postedSecs = posted.getTime()/1000l;
        return get(["method":"flickr.photos.setDates","photo_id":photoid,"date_posted":"${postedSecs}"])
    }
    String setNoExifDates(String photoid) {
        //long postedSecs = 0;  // 1970-01-01 GMT 
        long postedSecs = 5*60*60;  // 1970-01-01 EST
        String taken = "1970-01-01 00:00:00"
        return get(["method":"flickr.photos.setDates","photo_id":photoid,"date_posted":"${postedSecs}","date_taken":taken])
    }
    


    String getPhotoCounts() { 
        return get(["method":"flickr.photos.getCounts","taken_dates":"1900-01-01,2099-01-01"])
    }
    String getPhotoInfo(Map params) {
        return get(inject(params,["method":"flickr.photos.getInfo"]))
    }
    String getSizes(Map params) {
        return get(inject(params,["method":"flickr.photos.getSizes"]))
    }
    String getExif(Map params) {
        return get(inject(params,["method":"flickr.photos.getExif"]))
    }
    String getPhotoSearch(Map params) {
        return get(inject(params,["method":"flickr.photos.search"]))
    }
    String getEcho(Map params) {
        return get(inject(params,["method":"flickr.test.echo"]))
    }
    String testLogin() {
        def noparams = [:]
        return get(inject(noparams,["method":"flickr.test.login"]))
    }

    // injects oauth_consumer_key and oauth_token
    // sign the request
    // subit with http get:
    String get(Map params) {
        return new REST().get(sign("GET",REST.urlBase,injectApiAndToken(params)));
    }
    String post(Map params) {
        return new REST().post(sign("POST",REST.urlBase,injectApiAndToken(params)));
    }
    String postMultipart(Map params) {
        return new REST().postMultipart(sign("POST",REST.urlUpload,injectApiAndToken(params)));
    }


    //////////////////////////////////////////////
    // Below are internal private implementations
    //////////////////////////////////////////////

    // copy params and override with oauth_consumer_key, and oauth_token
    // also add oauth_timestamp, and oauth_nonce
    private Map injectApiAndToken(Map params) {
        String timestamp = ""+System.currentTimeMillis();
        String nonce = MD5.digest(timestamp)
        return inject(params,[
            //"format": "json",
            "api_key": Environment.api_key,
            "oauth_consumer_key": Environment.api_key,
            "oauth_token": Environment.access_token,
            "oauth_timestamp": timestamp,
            "oauth_nonce": nonce,
            "oauth_signature_method": "HMAC-SHA1"
            ]);
    }
    
    // make a copy of map m1, with map m2 added on top
    // the map m2 will "override" entries in map m1 if they hav common keys
    private Map inject(Map m1,Map m2) {
        def resultMap = [:];
        resultMap.putAll(m1);
        resultMap.putAll(m2);
        return resultMap;
    }

    // performs flickr signature by injecting oauth_signature into params map
    //  -returns a copy of tha map , as per inject behaviour
    // add a way to exclude some (one) parameter ('photo') for upload
    // depends on Environment.secret, and Environment.access_token_secret,
    // which are currently negotiated outside and set in Environment
    // does not sign a parameter named 'photo'
    private Map sign(String verb,String url,Map params) {
        // maks a list : [key1,value1,key2,value2]
        // sorted by key names
        //printMap("-Sign",params);
        String queryString = formQueryString(params);
        String data = formBaseString(verb, url, queryString);
        String hmacKey = Environment.secret+'&'+Environment.access_token_secret;
        String signature = SHA1.calculateRFC2104HMAC(data,hmacKey);
        //printMap("-Sign",["data":data,"hmacKey":hmacKey,"digest":signature]);
        def signed =  inject(params,["oauth_signature":signature]);
        //printMap("+Sign",signed);
        return signed;
    }

    private String printMap(String name,Map params) {
        System.out.println(name+":");

        params.keySet().sort().each() {
            if ("photo"==it) return;
            System.out.println("  "+it+": "+params.get(it));
        }
    }

    private static String formQueryString(Map params) {
        // makes a list : ["key1=value1","key2=value2"]
        // where values are URI encoded
        // then return the sorted list, joined by '&'
        def sortedArgs = []; // List of sorted param names, and values
        params.keySet().each() {
            if ("photo"==it) return;
            sortedArgs << it+"="+encodeURIComponent(params.get(it));
        }
        sortedArgs.sort(); // mutates the array
        return sortedArgs.join("&");
    }

    // Turn a url + query string into a Flickr API "base string".
    private static String formBaseString(verb,url,queryString) {
      return [verb, encodeURIComponent(url), encodeURIComponent(queryString)].join("&");  
    }

    // from https://gist.github.com/declanqian/7892516
    private static String encodeURIComponent(String component) {
        String result = null;

        try {
            result = URLEncoder.encode(component, "UTF-8")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = component;
        }
        return result;
    }
    
}
