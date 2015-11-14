package net.snookr.flickr;

class REST {
    static String host = "www.flickr.com"; // ?port
    static String urlBase = "https://${host}/services/rest/";
    static String urlUpload = "https://up.flickr.com/services/upload/";

    boolean debug=true;
    String get(Map params) {
        def paramList = []; //list of 'name=value' Strings
        params.each() { key , value ->
                paramList << (key+"="+URLEncoder.encode(value));
        }

        def url = "${urlBase}?"+paramList.join("&");

        def baos = new ByteArrayOutputStream()
        def bout = new BufferedOutputStream(baos)
        bout << new URL(url).openStream(); // who closes this ?
        bout.close()
        return baos.toString();
    }

    String post(Map params) {
        def posturl = new URL(urlBase);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) posturl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            conn.connect();
            OutputStream out = null;
            try {
                out = conn.getOutputStream();
                def paramList = []; //list of 'name=value' Strings
                params.each() { key,value ->
                        paramList << (key+"="+URLEncoder.encode(value))
                }
                out << paramList.join("&");
                out.flush();
            } finally {
                out?.close(); // what about IOException
            }

            InputStream is = null;
            try {
                def baos = new ByteArrayOutputStream()
                def bout = new BufferedOutputStream(baos)
                is = conn.getInputStream();
                bout << is;
                bout.close()
                return baos.toString();
            } finally {
                is?.close(); // what about IOException
            }
        } finally {
            conn?.disconnect(); // safe deref
        }

    }

    String postMultipart(Map params) {
        def posturl = new URL(urlUpload);

        // md5 of 'snookr' !
        String boundary = "------e085d567677b1d7bfe85a051772aca43";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) posturl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
            conn.connect();
            OutputStream out = null;
            try {
                out = conn.getOutputStream();

                out << ("--${boundary}\r\n");
                params.each() { key,value ->
                    if (value instanceof byte[]) {
                        out << ("Content-Disposition: form-data; name=\"${key}\"; filename=\"image.jpg\";\r\n");
                        out << ("Content-Type: image/jpeg" + "\r\n\r\n");
                        out << ((byte[]) value);
                        out << ("\r\n" + "--${boundary}\r\n");
                    } else {
                        out << ("Content-Disposition: form-data; name=\"${key}\"\r\n\r\n");
                        out << (String.valueOf(value));
                        out << ("\r\n" + "--${boundary}\r\n");
                    }
                }
                // the last --bOuNdArY-- should hav an extra trailing 2 hyphens --
                out.flush();
            } finally {
                out?.close(); // what about IOException
            }

            InputStream is = null;
            try {
                def baos = new ByteArrayOutputStream()
                def bout = new BufferedOutputStream(baos)
                is = conn.getInputStream();
                bout << is;
                bout.close()
                return baos.toString();
                
            } catch (Exception e) {
                println e;                
            } finally {
                is?.close(); // what about IOException
            }
        } finally {
            conn?.disconnect(); // safe deref
        }

    }
    
    
}
