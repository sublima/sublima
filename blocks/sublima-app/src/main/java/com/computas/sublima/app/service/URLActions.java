package com.computas.sublima.app.service;

import com.computas.sublima.query.impl.DefaultSparulDispatcher;
import com.computas.sublima.query.service.SettingsService;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.extractor.HTMLTextExtractor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A class to do various things with a URL or its contents
 *
 * @author: kkj
 * Date: Apr 23, 2008
 * Time: 11:11:41 AM
 */
public class URLActions {
    private URL url;
    private HttpURLConnection con = null;
    private String ourcode = null; // This is the code we base our status on
    private String encoding = "ISO-8859-1";
    private static Logger logger = Logger.getLogger(URLActions.class);
    private DefaultSparulDispatcher sparulDispatcher;
    private Exception exception;


    public URLActions(String u) {
        try {
            url = new URL(u);
        } catch (MalformedURLException e) {
            setExceptionAndCode(e);
        }
    }

    private void setExceptionAndCode(Exception e) {
	exception = e;
	ourcode = e.getClass().getSimpleName();
    }

    public URLActions(URL u) {
        url = u;
    }

    public URL getUrl() {
        return url;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public HttpURLConnection getCon() {
        return con;
    }

    /**
     * Method to establish a connection
     * <p/>
     * Apparently, the underlying library needs one connection object for each thing to
     * retrieve from the connection. This seems very awkward, thus, methods in this class
     * resets the connection for each thing they do. This method will always refresh the object's
     * connection object.
     */
    public void connect() {
        if (con == null) {
            try {
                con = (HttpURLConnection) url.openConnection();
                
                //Some servers require user-agent to be set
                con.addRequestProperty("User-Agent", "Sublima/1.0");
                
                //some more content negotiation, just to make sure
                con.addRequestProperty("Accept-Language", "en-us,en,no");
                con.addRequestProperty("Accept-Charset", "utf-8,iso-8859-1");
            }
            catch (IOException e) {
        	setExceptionAndCode(e);
            }
        }
    }

    public InputStream readContentStream() {
        InputStream result = null;

        try {
            connect();
            result = con.getInputStream();
	} catch (IOException e) {
            setExceptionAndCode(e);
        }
        con = null;
        return result;
    }

    /**
     * @deprecated Not in use, candidate for removal?
     */
    public String readContent() { // Sux0rz. Dude, where's my multiple return types?
        String result = null;
        try {
            InputStream content = readContentStream();
            result = IOUtils.toString(content);
        }
        catch (IOException e) {
            setExceptionAndCode(e);
        }
        con = null;
        return result;

    }


    /**
     * Method to get only the HTTP Code, or String representation of exception
     *
     * @return ourcode
     */
    private String getCode() {
        if (ourcode != null) {
            return ourcode;
        }

        logger.info("getCode() ---> " + url.toString());

        FutureTask<?> theTask = null;
        try {
            // create new task
            theTask = new FutureTask<Object>(new Runnable() {
                public void run() {
                    try {
                        connect();
                        con.setConnectTimeout(6000);
                        ourcode = String.valueOf(con.getResponseCode());
		    } catch (Exception e) {
                	setExceptionAndCode(e);
                    }
                }
            }, null);

            // start task in a new thread
            new Thread(theTask).start();

            // wait for the execution to finish, timeout after 10 secs
            theTask.get(10L, TimeUnit.SECONDS);
        }
        catch (TimeoutException e) {
            setExceptionAndCode(e);
        } catch (InterruptedException|ExecutionException e) {
            setExceptionAndCode(e);
            e.printStackTrace();
        }

        finally {
            try {
                con.disconnect();
                con = null;
            } catch (Exception e) {
                logger.error("Could not close connection");
            }
        }

        logger.info("getCode() ---> " + url.toString() + " returned a " + ourcode);

        if (ourcode == null) {
            ourcode = "UNKNOWN";
        }

        return ourcode;
    }

    /**
     * A method to check a URL. Returns the HTTP code.
     * In cases where the connection gives an exception
     * the exception is catched and a String representation
     * of the exception is returned as the http code
     *
     * @return A HashMap<String, String> where each key is an HTTP-header, but in lowercase,
     *         and represented in an appropriate namespace. The returned HTTP code is in the
     *         http:status field. In case of exceptions a String
     *         representation of the exception is used.
     *         
     * @deprecated Not in use, candidate for removal
     */
    public HashMap<String, String> getHTTPmap() {
        final HashMap<String, String> result = new HashMap<String, String>();

        FutureTask<?> theTask = null;
        try {
            // create new task
            theTask = new FutureTask<Object>(new Runnable() {
                public void run() {
                    try {
                        logger.info("getHTTPmap() ---> " + url.toString());
                        connect();
                        con.setConnectTimeout(6000);
                        for (String key : con.getHeaderFields().keySet()) {
                            if (key != null) {
                                result.put("httph:" + key.toLowerCase(), con.getHeaderField(key));
                            }

                        }

                        ourcode = String.valueOf(con.getResponseCode());
                        con = null;
                    } catch (Exception e) {
                	setExceptionAndCode(e);
                    }
                    result.put("http:status", ourcode);
                }
            }, null);

            // start task in a new thread
            new Thread(theTask).start();

            // wait for the execution to finish, timeout after 10 secs
            theTask.get(10L, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            setExceptionAndCode(e);
        }
        return result;
    }

    /**
     * Method that updates a resource based on the HTTP Code.
     * The resource can have one of three statuses: OK, CHECK or INACTIVE.
     * This list shows what HTTP Codes that gives what status.
     * <p/>
     * 2xx - OK
     * <p/>
     * 301 - should arguably be CHECK, but this causes unnecessary noise in the link checker, so we accept it as OK
     * 302 - OK
     * 303 - OK
     * 304 - OK
     * 305 - OK
     * 306 - INACTIVE
     * 307 - OK
     * <p/>
     * 400 - INACTIVE
     * 401 - CHECK
     * 403 - INACTIVE
     * 404 - CHECK
     * 405 - INACTIVE
     * 406 - CHECK
     * 407 - CHECK
     * 408 - CHECK
     * 409 - CHECK
     * 410 - GONE
     * 411 to 417 - CHECK
     * <p/>
     * 5xx - CHECK
     * <p/>
     * MALFORMED_URL - INACTIVE
     * UNSUPPORTED_PROTOCOL - INACTIVE
     * UNKNOWN_HOST - CHECK
     * CONNECTION_TIMEOUT - CHECK
     * <p/>
     * Others - CHECK
     */
    public void getAndUpdateResourceStatus() {
        sparulDispatcher = new DefaultSparulDispatcher();
        String status = "";

        try {

            if (ourcode == null) {
                getCode();
            }

            if (isValid()) {
                status = "<http://sublima.computas.com/status/ok>";

                // Update the external content of the resource
                //updateResourceExternalContent();

            }
            // GONE
            else if ("410".equals(ourcode)) {
                status = "<http://sublima.computas.com/status/gone>";
            }
            // INACTIVE
            else if ("306".equals(ourcode) ||
                    "400".equals(ourcode) ||
                    "403".equals(ourcode) ||
                    "405".equals(ourcode)) {
                status = "<http://sublima.computas.com/status/inaktiv>";
            }
            // CHECK
            else {
                status = "<http://sublima.computas.com/status/check>";
            }

        }
        catch (Exception e) {
            logger.info("Exception -- updateResourceStatus() ---> " + url.toString() + ":" + ourcode);
            e.printStackTrace();
            insertNewStatusForResource("<http://sublima.computas.com/status/check>");
        }
        // OK

        insertNewStatusForResource(status);
    }

    /**
     * Attempts to fetch the URL, and checks that the resulting status code is OK
     * @return true if the URL is valid
     */
    public boolean isValid() {
	if (ourcode == null) {
            getCode();
        }
	
	return ourcode.startsWith("2") ||
		"301".equals(ourcode) || // should arguably be CHECK, but this causes unnecessary noise in the link checker, so we accept it as OK
	        "302".equals(ourcode) ||
	        "303".equals(ourcode) ||
	        "304".equals(ourcode) ||
	        "305".equals(ourcode) ||
	        "307".equals(ourcode);
    }

    private void insertNewStatusForResource(String status) {
        String deleteString;
        String updateString;

        try {

            if (status.equalsIgnoreCase("<http://sublima.computas.com/status/ok>")) {
                deleteString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                        "DELETE FROM <" + SettingsService.getProperty("sublima.basegraph") + ">\n" +
                        "{\n" +
                        "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                        "}\n" +
                        "WHERE {\n" +
                        "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                        "}";

                updateString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                        "INSERT INTO <" + SettingsService.getProperty("sublima.basegraph") + ">\n" +
                        "{\n" +
                        "<" + url.toString() + "> sub:status " + status + ".\n" +
                        "}";
            } else {
                deleteString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                        "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                        "DELETE FROM <" + SettingsService.getProperty("sublima.basegraph") + ">\n" +
                        "{\n" +
                        "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                        "<" + url.toString() + "> wdr:describedBy ?oldstatus .\n" +
                        "}\n" +
                        "WHERE {\n" +
                        "<" + url.toString() + "> sub:status ?oldstatus .\n" +
                        "<" + url.toString() + "> wdr:describedBy ?oldstatus .\n" +
                        "}";

                updateString = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                        "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                        "INSERT INTO <" + SettingsService.getProperty("sublima.basegraph") + ">\n" +
                        "{\n" +
                        "<" + url.toString() + "> sub:status " + status + ".\n" +
                        "<" + url.toString() + "> wdr:describedBy " + status + ".\n" +
                        "}";
            }

            logger.info("insertNewStatusForResource() ---> " + url.toString() + ":" + ourcode + " -- SPARUL DELETE  --> " + deleteString);

            boolean success;
            success = sparulDispatcher.query(deleteString);
            logger.info("updateResourceStatus() ---> " + url.toString() + " with code " + ourcode + " -- DELETE OLD STATUS --> " + success);
            logger.info("insertNewStatusForResource() ---> " + url.toString() + ":" + ourcode + " -- SPARUL UPDATE  --> " + updateString);

            success = false;

            success = sparulDispatcher.query(updateString);
            logger.info("insertNewStatusForResource() ---> " + url.toString() + ":" + ourcode + " -- INSERT NEW STATUS --> " + success);
        } catch (Exception e) {
            logger.info("insertNewStatusForResource() ---> Gave an exception. Check if this URL is valid.");
        }
    }

    public String strippedContent
            (final String
                    content) throws UnsupportedEncodingException {

        final StringBuilder sb = new StringBuilder();
        logger.info("strippedContent() ---> Getting external content");
        FutureTask<?> theTask = null;
        try {
            // create new task
            theTask = new FutureTask<Object>(new Runnable() {
                public void run() {

                    connect();
                    if (con.getContentEncoding() != null) {
                        encoding = con.getContentEncoding();
                    }

                    InputStream stream = null;
                    if (content == null) {
                        stream = readContentStream();
                    } else {
                        stream = IOUtils.toInputStream(content);
                    }

                    HTMLTextExtractor textExtractor = new HTMLTextExtractor();

                    try {
                        connect();
                        String contentType = con.getContentType();
                        if (contentType.toLowerCase().contains("text/html") || contentType.toLowerCase().contains("text/xhtml") || contentType.toLowerCase().contains("text/xml") || contentType.toLowerCase().contains("xhtml+xml")) {
                            Reader reader = textExtractor.extractText(stream, contentType, "UTF-8");
                            int charValue = 0;

                            while ((charValue = reader.read()) != -1) {
                                sb.append((char) charValue);
                            }
                            logger.info("strippedContent() ---> TEXT:\n" + sb.toString());
                        }
                    } catch (Exception e) {
                        logger.warn("URLActions.strippedContent() gave Exception, returning \"\" ---> " + e.getMessage());
                        sb.append("");
                    }

                }
            }, null);

            // start task in a new thread
            new Thread(theTask).start();

            // wait for the execution to finish, timeout after 10 secs
            theTask.get(10L, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            setExceptionAndCode(e);
        }
        return sb.toString();

    }

    public String getMessage() {
	String message = (exception == null ? "" : exception.getMessage());

	return String.format("Status: %s, %s", ourcode, message);
    }
}
