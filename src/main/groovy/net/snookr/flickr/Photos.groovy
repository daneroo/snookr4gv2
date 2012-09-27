package net.snookr.flickr;

import groovy.util.slurpersupport .*;   // for parsing utils at end
import groovy.lang.Closure;
import java.text.SimpleDateFormat;

import net.snookr.util.MD5;
import net.snookr.util.Spawner;
import net.snookr.util.Progress;
import net.snookr.util.Environment;
import net.snookr.util.DateFormat;
import net.snookr.model.FlickrImage;

class Photos {
    static long PHOTO_ALREADY_PRESENT=-1;
    Flickr flickr = new Flickr();

    // This really should return a boolean, with an exception if upload fails
    // true if upload succeeds.
    // and false if already present. 
    //
    // this checks if the photo is not already present (by md5tag)
    // return -1 (PHOTO_ALREADY_PRESENT) as photoid if already present.
    // could return a FlickrImage through getFlickrImage instead,
    // with null return value if already present.
    long uploadPhoto(File f) {
        String md5tag = "snookr:md5=${MD5.digest(f)}";
        boolean present = isMD5PresentOnFlickr(md5tag);
        if (present) {
            println "Photo ${f} with tag: ${md5tag} is already present on flickr";
            return PHOTO_ALREADY_PRESENT;
        }
        def rsp = parse( flickr.uploadPhoto(f,md5tag) );
        assert 1 == rsp.photoid.list().size()
        return Long.valueOf(rsp.photoid.text());
    }

    // this is an example of counting images for seach params.
    // could be refactored. for later re-use
    // pass a list of tags ["tag1","tag2"], possible tag_mode etc.
    boolean isMD5PresentOnFlickr(String md5tag) {
        int perPage=500;
        int page=1;
        Map searchParams = [
            "user_id":Environment.user_id,
            "per_page":"${perPage}",
            "page":"${page}",
            "tags":"${md5tag}",
        ]
        def rsp = parse( flickr.getPhotoSearch(searchParams) );
        // assert invariants
        assert page == Integer.valueOf(rsp.photos.@page.text());
        assert perPage == Integer.valueOf(rsp.photos.@perpage.text());

        int pages = Integer.valueOf(rsp.photos.@pages.text());
        // pre-emptively return zero if pages="0", because we have seen cases where
        // no matching photo return this fragment:
        // <photos page="1" pages="0" perpage="500" total="" />
        // instead of the usual:
        // <photos page="1" pages="0" perpage="500" total="0" />
        if (pages==0) {
            return 0;
        }
        int countPhotosMatchingTag  = Integer.valueOf(rsp.photos.@total.text());
        if (countPhotosMatchingTag>1) {
            println "multiple (${countPhotosMatchingTag}) photos matching tag: ${md5tag} are already present on flickr";
        }
        //println "photos matching tag: ${md5tag}: ${countPhotosMatchingTag}"
        return (countPhotosMatchingTag);
        // if i wanted the actual list...
        // List returnedPhotosMatchingTag = rsp.photos.photo.list().'@id'*.text();
        // then parse as in getPage below, using extras, to fill in all info
    }

    int getTotal() {
        def rsp = parse( flickr.getPhotoCounts() );

        // the date range (1900-2099) in getPhotoCounts should only return one range : one count
        assert 1 == rsp.photocounts.photocount.list().size()

        return Integer.valueOf(rsp.photocounts.photocount.'@count'.text());
    }

    /* getPhotoList Could have variable parameters later:
    perPage, sort, other search criteeria.
     */

    List getPhotoList(int numThreads) {
        int perPage = 500; // max 500
        int expectedTotal = getTotal();
        int expectedPages = (total+perPage-1)/perPage
        
        println "  Expecting total of ${expectedTotal} photos in ${expectedPages} pages of ${perPage} photos"
        
        List flickrList = null;
        if (numThreads>1) {
            flickrList = getListMultiThreaded  (perPage,expectedPages,expectedTotal,numThreads)
        } else {
            flickrList = getListSingleThreaded (perPage,expectedPages,expectedTotal)
        }
        
        println "Flickr List size: ${flickrList.size()}"
        
        assert expectedTotal == flickrList.size();
        assertUniquenessOfPhotoid(flickrList);
        return flickrList;
        
    }
    List getListMultiThreaded(int perPage,int expectedPages,int expectedTotal,int numThreads) {
        // this Lists acess needs to be synchronized
        List flickrList = [];
        List pageList = (1..expectedPages);
        Closure getPhotoPageClosure = { page ->
            List pageFlickrList = new Photos().getPage(page,perPage,expectedPages,expectedTotal);
            // this Lists acess needs to be synchronized
            flickrList.addAll(pageFlickrList);
        }
        new Spawner(pageList,getPhotoPageClosure,numThreads).run();
        return flickrList;
    }

    List getListSingleThreaded(int perPage,int expectedPages,int expectedTotal) {
        Progress progress = new Progress(expectedPages,"page");
        List flickrList = [];
        for ( page in 1..expectedPages) { 
            List pageFlickrList = getPage(page,perPage,expectedPages,expectedTotal);
            flickrList.addAll(pageFlickrList);

            // show progress
            progress.increment();
        }
        return flickrList;
    }


    List getPage(int page,int perPage,int expectedPages,int expectedTotal) {
        String sortOrder="date-taken-asc";
        Map searchParams = [
            "user_id":Environment.user_id,
            "per_page":"${perPage}",
            "page":"${page}",
            "sort":sortOrder,
            // extras: license, date_upload, date_taken, owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media
            "extras":"date_upload,date_taken,tags,last_update,original_format",
        ]
        def rsp = parse( flickr.getPhotoSearch(searchParams) );
        /* These are twhat the elements look like:
        <photo id="3419478079" owner="43605851@N00" secret="9011a2880d" server="3346"
        farm="4" title="IMG_4585.jpg" ispublic="1" isfriend="0" isfamily="0"
        dateupload="1238964994" datetaken="2009-04-05 16:56:34" datetakengranularity="0"
        tags="snookrd snookr:md5=0bbc4b0ccfad80a7b8ac7c24ca9abbe8" lastupdate="1239074059"
        originalsecret="52f4bb048d" originalformat="jpg"/>
         */
        // assert invariants while iterating
        assert page == Integer.valueOf(rsp.photos.@page.text());
        assert perPage == Integer.valueOf(rsp.photos.@perpage.text());
        assert expectedPages == Integer.valueOf(rsp.photos.@pages.text());
        assert expectedTotal == Integer.valueOf(rsp.photos.@total.text());
        
        //List list = rsp.photos.photo.list().'@id'*.text();
        List list = [];

        rsp.photos.photo.each() { photo -> // for each photo
            FlickrImage flima = new FlickrImage();
            flima.photoid = photo.'@id';

            //TODO taken granularity is always 0.??
            String takenStr = photo.'@datetaken';
            flima.taken = DateFormat.parse(takenStr);

            String postedStr = photo.'@dateupload';
            flima.posted = new Date(Long.parseLong(postedStr)*1000l);

            String lastUpdateStr = photo.'@lastupdate';
            flima.lastUpdate = new Date(Long.parseLong(lastUpdateStr)*1000l);

            String tags = photo.'@tags';
            //tags.tokenize().each() { println "t: ${it}" }
            // md5
            def md5List = tags.tokenize().findAll(){ it =~ /snookr:md5=/};
            assert md5List.size()<=1;
            if (md5List.size==1) {
                flima.md5 = (md5List[0] =~ /snookr:md5=/).replaceFirst("");
            }

            // new fields for url reconstruction: farm server secret originalsecret
            flima.farm = photo.'@farm';
            flima.server = photo.'@server';
            flima.secret = photo.'@secret';
            flima.originalsecret = photo.'@originalsecret';

            //println "${flima}";
            list << flima;
        }

        assertUniquenessOfPhotoid(list);
        return list;
    }
    
    void assertUniquenessOfPhotoid(List listToCheck) {
        def uniqueMap = [:]
        listToCheck.each() { uniqueMap[it.photoid]=it }
        assert listToCheck.size() == uniqueMap.size();
    }

    FlickrImage getFlickrImage(String photoid) {
        def attr = getInfo(photoid);
        FlickrImage flima = new FlickrImage();
        flima.photoid = attr.photoid;
        flima.md5 = attr.md5;
        //TODO verify taken granularity is always 0.
        flima.taken = DateFormat.parse(attr.taken);
        flima.posted = new Date(Long.parseLong(attr.posted)*1000l);
        flima.lastUpdate = new Date(Long.parseLong(attr.lastUpdate)*1000l);
        return flima;
    }

    Map getInfo(String photoid) {
        def attr = ["photoid":photoid];

        def rsp = parse( flickr.getPhotoInfo(["photo_id":photoid]) );
        assert photoid == rsp.photo.'@id'.text();

        // taken
        attr.taken = rsp.photo.dates.'@taken'.text();
        // posted
        attr.posted = rsp.photo.dates.'@posted'.text();
        // lastupdate
        attr.lastUpdate = rsp.photo.dates.'@lastupdate'.text();

        // md5
        def md5List = rsp.photo.tags.tag.findAll(){ it.text() =~ /snookr:md5=/};
        assert md5List.size()<=1;
        attr.md5 = (md5List[0].text() =~ /snookr:md5=/).replaceFirst("");
        
        return attr;
    }

    Map getSizes(String photoid) {
        /*
        source: is the url for the image itself
        url: is a web page for that photo at that size
        May not have all sizes !
        <sizes canblog="1" canprint="1" candownload="1">
        <size label="Square" width="75" height="75" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_s.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=sq" />
        <size label="Thumbnail" width="100" height="75" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_t.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=t" />
        <size label="Small" width="240" height="180" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_m.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=s" />
        <size label="Medium" width="500" height="375" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=m" />
        <size label="Large" width="1024" height="768" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_b.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=l" />
        <size label="Original" width="2592" height="1944" source="http://farm1.static.flickr.com/145/419443247_1195f586b4_o.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=o" />
        </sizes>
         */
        def mapOfSizes = [:];
        def rsp = parse( flickr.getSizes(["photo_id":photoid]) );
        // should I assert anything ?
        rsp.sizes.size.each() { oneSize -> // for each photo
            String sizeLabel = oneSize.'@label';
            String sizeSource = oneSize.'@source';
            mapOfSizes[sizeLabel] = sizeSource;
        }
        return mapOfSizes;
    }

    //utility function for parser
    GPathResult parseV(String stringResponse) { // V for Verbose
        println "------------------------------"
        println(stringResponse);
        println "------------------------------"
        return parse(stringResponse);
    }

    // might inject Error handling throwing Exception on Error
    // Might Also push the slurping back to Flickr or REST
    GPathResult parse(String stringResponse) {
        boolean validating=false;
        boolean namespaceAware = true;
        return new XmlSlurper(validating,namespaceAware).parseText(stringResponse);
    }

}
