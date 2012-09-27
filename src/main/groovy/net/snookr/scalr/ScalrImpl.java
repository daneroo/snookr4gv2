/*
 *  javadoc: http://hc.apache.org/httpclient-3.x/apidocs/
 * Use the legacy httpclient 3.x library to multipart post:
 * commons-httpclient-3.1.jar ( commons-codec-1.3.jar,commons-logging-1.1.1.jar )
 *
 * Decided NOT to use the newer httpcore-4.0.jar, httpclient-4.0-beta2.jar from
 *    not ready yet: http://hc.apache.org/ apache http components
 */
package net.snookr.scalr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.snookr.util.Timer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 *
 * @author daniel
 */
public class ScalrImpl {

    /* Convinience to return as string */
    static final int MAXRETURNBODYLENGTH = 10 * 1024 * 1024;
    private static final boolean verboseGETRate = false;
    private static final boolean verbosePOSTRate = true;

    public byte[] get(String getURL, Map<String, String> params) {
        GetMethod getMethod = new GetMethod(getURL);

        int numParams = params.size();
        NameValuePair[] nvpairs = new NameValuePair[numParams];
        int p = 0;
        for (Map.Entry<String, String> e : params.entrySet()) {
            String pName = e.getKey();
            String pValue = e.getValue();
            nvpairs[p++] = new NameValuePair(pName, pValue);
        }
        getMethod.setQueryString(nvpairs);

        HttpClient client = new HttpClient();
        // set per default
        //client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        //        new DefaultHttpMethodRetryHandler());

        try {
            Timer tt = new Timer();
            int statusCode = client.executeMethod(getMethod);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + getMethod.getStatusLine());
            }

            byte[] responseBody = getMethod.getResponseBody(MAXRETURNBODYLENGTH);
            if (verboseGETRate) {
                float sizekB = responseBody.length / 1024.0f;
                String rateMsg = String.format("Transfer %.1f kB / %.1f s rate: %.1f kB/s ", sizekB, tt.diff(), sizekB / tt.diff());
                System.out.println("-=-=-=-" + rateMsg);
            }
            return responseBody;

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            getMethod.releaseConnection();
        }
        return null;
    }

    /* Convert params:Map to Part array
     */
    private Part[] paramsToPartList(Map params) throws FileNotFoundException {
        Part[] parts = new Part[params.size()];
        List<Part> partsList = new ArrayList<Part>();
        for (Object key : params.keySet()) {
            Object value = params.get(key);
            String keyString = String.valueOf(key);
            if (value instanceof byte[]) {
                partsList.add(new FilePart(keyString, new ByteArrayPartSource(keyString, (byte[]) value)));
            } else if (value instanceof File) {
                partsList.add(new FilePart(keyString, (File) value));
            } else {
                String valueString = String.valueOf(value);
                partsList.add(new StringPart(keyString, valueString));
            }
        }
        // Generified toArray invocation replaces the original array if sizes don't match
        parts = partsList.toArray(parts);
        return parts;
    }

    public byte[] postMultipart(String postURL, Map params) {
        PostMethod filePost = new PostMethod(postURL);
        try {

            //filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE,false);
            //filePost.setContentChunked(true);

            Part[] parts = paramsToPartList(params);
            MultipartRequestEntity mpre = new MultipartRequestEntity(parts, filePost.getParams());
            filePost.setRequestEntity(mpre);

            HttpClient client = new HttpClient();

            client.getHttpConnectionManager().
                    getParams().setConnectionTimeout(5000);

            Timer tt = new Timer();
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
                //String body = filePost.getResponseBodyAsString();
                byte[] responseBody = filePost.getResponseBody(MAXRETURNBODYLENGTH);
                if (verbosePOSTRate) {
                    float sizekB = mpre.getContentLength() / 1024.0f;
                    String rateMsg = String.format("Transfer %.1f kB / %.1f s rate: %.1f kB/s ", sizekB, tt.diff(), sizekB / tt.diff());
                    System.out.println("-=-=-=-" + rateMsg);
                }
                return responseBody;
            } else {
                log("Upload failed, response=" + HttpStatus.getStatusText(status));
            }
        } catch (Exception ex) {
            log("ERROR: " + ex.getClass().getName() + " " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            filePost.releaseConnection();
        }
        return null;
    }

    private void log(String m) {
        System.err.println(m);
    }
}
