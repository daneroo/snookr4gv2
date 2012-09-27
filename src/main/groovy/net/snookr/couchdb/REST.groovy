package net.snookr.couchdb;

class REST {
    String host = "localhost:5984"; // ?port
    String urlBase = "http://${host}/snookr";

    boolean debug=true;
    String get(String key) {
        def url = "${urlBase}/${key}";
        def baos = new ByteArrayOutputStream()
        def bout = new BufferedOutputStream(baos)
        bout << new URL(url).openStream(); // who closes this ?
        bout.close()
        return baos.toString();
    }

    String put(String key,String json) {
        def posturl = new URL("${urlBase}/${key}");
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) posturl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            //conn.setRequestProperty("Content-Type","text/plain; charset=utf-8");
            conn.setRequestProperty("Content-Type","application/json; charset=utf-8");

            conn.connect();
            OutputStream out = null;
            try {
                out = conn.getOutputStream();
                out << json;
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
        } catch (Exception e) {
            System.err.println "Could not save: ${json}";
            System.err.println e.getMessage();
            //return e.getMessage();
        } finally {
            conn?.disconnect(); // safe deref
        }
        return null;
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
    
}
