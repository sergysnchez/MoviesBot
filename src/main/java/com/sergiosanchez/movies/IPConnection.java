package com.sergiosanchez.movies;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.sergiosanchez.configuration.Config;

@SuppressWarnings("deprecation")
public class IPConnection {

    private static DefaultHttpClient httpclient;
	private static DefaultHttpClient httpclient2;
	private static DefaultHttpClient httpclient3;

	public static void addFile(String IP,String source) throws Exception {

        HttpHost targetHost = new HttpHost(IP, Integer.parseInt(Config.getPORT()) , "http");

        httpclient = new DefaultHttpClient();
        try {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                    new UsernamePasswordCredentials(Config.getUSER(), Config.getPASSWORD()));

            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

            HttpGet httpget = new HttpGet("http://"+IP+":"+Config.getPORT()+"/gui/");
            HttpResponse response = httpclient.execute(targetHost, httpget, localcontext);
            EntityUtils.consumeQuietly(response.getEntity());
            
            httpget = new HttpGet("http://"+IP+":"+Config.getPORT()+"/gui/token.html");
            response = httpclient.execute(targetHost, httpget, localcontext);
            
            HttpEntity e = response.getEntity();
            InputStream is = e.getContent();
            StringWriter sw = new StringWriter();
            IOUtils.copy(is, sw);
            sw.flush();
            sw.close();
            is.close();
            
            String t = sw.toString();
            int start = "<html><div id='token' style='display:none;'>".length();
            int end = t.indexOf("</div></html>");
            String token = t.substring(start,end);
            System.out.println(token);
            EntityUtils.consumeQuietly(response.getEntity());
            
             String add = URLEncoder.encode(source,"UTF-8");
            httpget = new HttpGet("http://"+IP+":"+Config.getPORT()+"/gui/?action=add-url&s="+add+"&token="+token);
            response = httpclient.execute(targetHost, httpget, localcontext);
            
            e = response.getEntity();
            is = e.getContent();
            sw = new StringWriter();
            IOUtils.copy(is, sw);
            sw.flush();
            sw.close();
            is.close();
            System.out.println(sw.toString());

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }
    
    public static String getInfo(String IP) {
    	 HttpHost targetHost = new HttpHost(IP, Integer.parseInt(Config.getPORT()) , "http");
    	 
    	 	String responseMethod = "";

         httpclient2 = new DefaultHttpClient();
         try {
             httpclient2.getCredentialsProvider().setCredentials(
                     new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                     new UsernamePasswordCredentials(Config.getUSER(), Config.getPASSWORD()));

             AuthCache authCache = new BasicAuthCache();
             BasicScheme basicAuth = new BasicScheme();
             authCache.put(targetHost, basicAuth);

             BasicHttpContext localcontext = new BasicHttpContext();
             localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

             HttpGet httpget = new HttpGet("http://"+IP+":"+Config.getPORT()+"/gui/");
             HttpResponse response = httpclient2.execute(targetHost, httpget, localcontext);
             EntityUtils.consumeQuietly(response.getEntity());
             
             httpget = new HttpGet("http://"+IP+":"+Config.getPORT()+"/gui/token.html");
             response = httpclient2.execute(targetHost, httpget, localcontext);
             
             HttpEntity e = response.getEntity();
             InputStream is = e.getContent();
             StringWriter sw = new StringWriter();
             IOUtils.copy(is, sw);
             sw.flush();
             sw.close();
             is.close();
             
             String t = sw.toString();
             int start = "<html><div id='token' style='display:none;'>".length();
             int end = t.indexOf("</div></html>");
             String token = t.substring(start,end);
             System.out.println(token);
             EntityUtils.consumeQuietly(response.getEntity());
             
             httpget = new HttpGet("http://"+IP+":"+8080+"/gui/?list=1&token="+token);
	         response = httpclient2.execute(targetHost, httpget, localcontext);
             
             e = response.getEntity();
             is = e.getContent();
             sw = new StringWriter();
             IOUtils.copy(is, sw);
             sw.flush();
             sw.close();
             is.close();
             responseMethod = sw.toString();
             System.out.println(sw.toString());

         } catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
             httpclient2.getConnectionManager().shutdown();
         }
		return responseMethod;
    }
    
    public static String removeFile(String IP, String hash) {
   	 HttpHost targetHost = new HttpHost(IP, Integer.parseInt(Config.getPORT()) , "http");
   	 
   	 	String responseMethod = "";

        httpclient3 = new DefaultHttpClient();
        try {
            httpclient3.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                    new UsernamePasswordCredentials(Config.getUSER(), Config.getPASSWORD()));

            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

            HttpGet httpget = new HttpGet("http://"+IP+":"+Config.getPORT()+"/gui/");
            HttpResponse response = httpclient3.execute(targetHost, httpget, localcontext);
            EntityUtils.consumeQuietly(response.getEntity());
            
            httpget = new HttpGet("http://"+IP+":"+Config.getPORT()+"/gui/token.html");
            response = httpclient3.execute(targetHost, httpget, localcontext);
            
            HttpEntity e = response.getEntity();
            InputStream is = e.getContent();
            StringWriter sw = new StringWriter();
            IOUtils.copy(is, sw);
            sw.flush();
            sw.close();
            is.close();
            
            String t = sw.toString();
            int start = "<html><div id='token' style='display:none;'>".length();
            int end = t.indexOf("</div></html>");
            String token = t.substring(start,end);
            System.out.println(token);
            EntityUtils.consumeQuietly(response.getEntity());
            
            httpget = new HttpGet("http://"+IP+":"+8080+"/gui/?action=remove&hash="+hash+"&token="+token);
	         response = httpclient3.execute(targetHost, httpget, localcontext);
            
            e = response.getEntity();
            is = e.getContent();
            sw = new StringWriter();
            IOUtils.copy(is, sw);
            sw.flush();
            sw.close();
            is.close();
            responseMethod = sw.toString();
            System.out.println(sw.toString());

        } catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
            httpclient3.getConnectionManager().shutdown();
        }
		return responseMethod;
   }
}