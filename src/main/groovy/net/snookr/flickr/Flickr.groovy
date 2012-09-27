package net.snookr.flickr;

import net.snookr.util.Environment;
import net.snookr.util.MD5;

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
        return postMultipart(["title":f.getName(),"photo":b,"tags":"snookrd ${md5tag}"])
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

    // injects api_key and auth_token
    // sign the request
    // subit with http get:
    String get(Map params) {
        return new REST().get(sign(injectApiAndToken(params)));
    }
    String post(Map params) {
        return new REST().post(sign(injectApiAndToken(params)));
    }
    String postMultipart(Map params) {
        return new REST().postMultipart(sign(injectApiAndToken(params)));
    }


    //////////////////////////////////////////////
    // Below are internal private implementations
    //////////////////////////////////////////////

    // copy params and override with api_key, and auth_token
    private Map injectApiAndToken(Map params) {
        return inject(params,["api_key":Environment.api_key,"auth_token":Environment.auth_token]);
    }
    
    // make a copy of map m1, with map m2 added on top
    // the map m2 will "override" entries in map m1 if they hav common keys
    private Map inject(Map m1,Map m2) {
        def resultMap = [:];
        resultMap.putAll(m1);
        resultMap.putAll(m2);
        return resultMap;
    }

    // performs flickr signature by injecting api_sig into map
    //  -returns a copy of tha map , as per inject behaviour
    //   could just ad to the passed Map...
    // add a way to exclude some (one) parameter ('photo') for upload
    // depends on secret
    // does not sign a parameter named 'photo'
    private Map sign(Map params) {
        // maks a list : [key1,value1,key2,value2]
        // sorted by key names
        def sorted = []; // List of sorted param names, and values
        params.keySet().sort().each() {
            if ("photo"==it) return;
            sorted << it; 
            sorted << params.get(it);
        }
        return inject(params,["api_sig":MD5.digest(Environment.secret+sorted.join(""))]);
    }

    
}
