/**
 * Copyright (C) 2016 Apigee Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apigee.edge.config.rest;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apigee.edge.config.utils.PrintUtil;
import com.apigee.edge.config.utils.ServerProfile;
import com.apigee.mgmtapi.sdk.client.MgmtAPIClient;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class RestUtil {

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final HttpTransport APACHE_HTTP_TRANSPORT = new ApacheHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    static String versionRevision;
    static Logger logger = LoggerFactory.getLogger(RestUtil.class);
    static String accessToken = null;
    
    static HttpRequestFactory REQUEST_FACTORY = HTTP_TRANSPORT
            .createRequestFactory(new HttpRequestInitializer() {
                // @Override
                public void initialize(HttpRequest request) {
                    request.setParser(JSON_FACTORY.createJsonObjectParser());
                    XTrustProvider.install();
                    FakeHostnameVerifier _hostnameVerifier = new FakeHostnameVerifier();
                    // Install the all-trusting host name verifier:
                    HttpsURLConnection.setDefaultHostnameVerifier(_hostnameVerifier);

                }
            });
    
    static HttpRequestFactory APACHE_REQUEST_FACTORY = APACHE_HTTP_TRANSPORT
            .createRequestFactory(new HttpRequestInitializer() {
                // @Override
                public void initialize(HttpRequest request) {
                    request.setParser(JSON_FACTORY.createJsonObjectParser());
                    XTrustProvider.install();
                    FakeHostnameVerifier _hostnameVerifier = new FakeHostnameVerifier();
                    // Install the all-trusting host name verifier:
                    HttpsURLConnection.setDefaultHostnameVerifier(_hostnameVerifier);

                }
            });

    /***************************************************************************
     * Env Config - get, create, update
     **/
    public static HttpResponse createEnvConfig(ServerProfile profile, 
                                                String resource,
                                                String payload)
            throws IOException {

        ByteArrayContent content = new ByteArrayContent("application/json", 
                                                            payload.getBytes());

        String importCmd = profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/environments/"
                            + profile.getEnvironment() + "/" + resource;

        HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(
                new GenericUrl(importCmd), content);
        restRequest.setReadTimeout(0);

        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
            //response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse createEnvConfig(ServerProfile profile,
                                               String resource,
                                               String resourceId,
                                               String subResource,
                                               String payload)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/environments/"
                + profile.getEnvironment() + "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource;

        return executeAPIPost(profile, payload, importCmd);
    }

    public static HttpResponse createEnvConfigUpload(ServerProfile profile, String resource, String filePath)
			throws IOException {
		byte[] file = Files.readAllBytes(new File(filePath).toPath());
		ByteArrayContent content = new ByteArrayContent("application/octet-stream", file);

		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
							+ "/environments/" + profile.getEnvironment()
							+ "/" + resource;

		HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(new GenericUrl(importCmd), content);
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}
    
    public static HttpResponse updateEnvConfig(ServerProfile profile, 
                                                String resource,
                                                String resourceId,
                                                String payload)
            throws IOException {

        ByteArrayContent content = new ByteArrayContent("application/json", 
                                                            payload.getBytes());

        String importCmd = profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/environments/"
                            + profile.getEnvironment() + "/" + resource + "/"
                            + URLEncoder.encode(resourceId, "UTF-8");

        HttpRequest restRequest = REQUEST_FACTORY.buildPutRequest(
                new GenericUrl(importCmd), content);
        restRequest.setReadTimeout(0);

        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse updateEnvConfig(ServerProfile profile,
                                               String resource,
                                               String resourceId,
                                               String subResource,
                                               String subResourceId,
                                               String payload)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/environments/"
                + profile.getEnvironment() + "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource +"/"
                + subResourceId;

        return executeAPIPost(profile, payload, importCmd);
    }

	public static HttpResponse updateEnvConfigUpload(ServerProfile profile, String resource, String resourceId,
			String filePath) throws IOException {

		byte[] file = Files.readAllBytes(new File(filePath).toPath());
		ByteArrayContent content = new ByteArrayContent("application/octet-stream", file);

		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
							+ "/environments/"+ profile.getEnvironment()
							+ "/" + resource + "/" + resourceId;

		HttpRequest restRequest = REQUEST_FACTORY.buildPutRequest(new GenericUrl(importCmd), content);
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}
	
	public static HttpResponse deleteEnvResourceFileConfig(ServerProfile profile, String resource, String resourceId)
			throws IOException {

		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
							+ "/environments/" + profile.getEnvironment()
							+ "/" + resource + "/" + resourceId;

		HttpRequest restRequest = REQUEST_FACTORY.buildDeleteRequest(new GenericUrl(importCmd));
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}
    
    public static HttpResponse deleteEnvConfig(ServerProfile profile, 
									            String resource,
									            String resourceId)
	throws IOException {
    	return deleteEnvConfig(profile, resource, resourceId, null);
    }
    
    public static HttpResponse deleteEnvConfig(ServerProfile profile, 
                                                String resource,
                                                String resourceId,
                                                String payload)
            throws IOException {
    	HttpRequest restRequest;
        String importCmd = profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/environments/"
                            + profile.getEnvironment() + "/" + resource + "/"
                            + URLEncoder.encode(resourceId, "UTF-8");
        
        if(payload!=null && !payload.equalsIgnoreCase("")){
        	ByteArrayContent content = new ByteArrayContent("application/json", 
                    payload.getBytes());
        	restRequest = REQUEST_FACTORY.buildRequest(HttpMethods.DELETE, new GenericUrl(importCmd), content);
        }else{
        	restRequest = REQUEST_FACTORY.buildDeleteRequest(
                    new GenericUrl(importCmd));
        }
        restRequest.setReadTimeout(0);

        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse getEnvConfig(ServerProfile profile, 
                                                String resource) 
            throws IOException {

        HttpRequest restRequest = REQUEST_FACTORY
                .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/environments/"
                        + profile.getEnvironment() + "/" + resource));
        restRequest.setReadTimeout(0);
        
        //logger.debug(PrintUtil.formatRequest(restRequest));

        HttpResponse response = null;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) return null;
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse getEnvConfig(ServerProfile profile,
                                            String resource,
                                            String resourceId,
                                            String subResource,
                                            String subResourceId)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/environments/"
                + profile.getEnvironment()  + "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource + "/"
                + subResourceId;

        return executeAPIGet(profile, importCmd);
    }
    
	public static HttpResponse patchEnvConfig(ServerProfile profile, 
            String resource,
            String resourceId,
            String payload)
	throws IOException {
	
		ByteArrayContent content = new ByteArrayContent("application/json", 
		                        payload.getBytes());
		
		String importCmd = profile.getHostUrl() + "/"
		+ profile.getApi_version() + "/organizations/"
		+ profile.getOrg() + "/environments/"
		+ profile.getEnvironment() + "/" + resource + "/"
		+ URLEncoder.encode(resourceId, "UTF-8");
		
		HttpRequest restRequest = APACHE_REQUEST_FACTORY.buildRequest(HttpMethods.PATCH, new GenericUrl(importCmd), content);
		restRequest.setReadTimeout(0);
		
		//logger.info(PrintUtil.formatRequest(restRequest));
		
		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

	return response;
	}

    /***************************************************************************
     * Org Config - get, create, update
     **/
    public static HttpResponse createOrgConfig(ServerProfile profile, 
                                                String resource,
                                                String payload)
            throws IOException {

        ByteArrayContent content = new ByteArrayContent("application/json", 
                                                            payload.getBytes());

        String importCmd = profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/" + resource;

        HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(
                new GenericUrl(importCmd), content);
        restRequest.setReadTimeout(0);
        
        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse createOrgConfig(ServerProfile profile,
                                               String resource,
                                               String resourceId,
                                               String subResource,
                                               String payload)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg()+ "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource;

        return executeAPIPost(profile, payload, importCmd);
    }
    
	public static HttpResponse createOrgConfigUpload(ServerProfile profile, String resource, String filePath)
			throws IOException {
		byte[] file = Files.readAllBytes(new File(filePath).toPath());
		ByteArrayContent content = new ByteArrayContent("application/octet-stream", file);

		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
				+ "/" + resource;

		HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(new GenericUrl(importCmd), content);
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}

    public static HttpResponse updateOrgConfig(ServerProfile profile, 
                                                String resource,
                                                String resourceId,
                                                String payload)
            throws IOException {

        ByteArrayContent content = new ByteArrayContent("application/json", 
                                                            payload.getBytes());

        String importCmd = profile.getHostUrl() + "/"
                                + profile.getApi_version() + "/organizations/"
                                + profile.getOrg() + "/" + resource + "/"
                                + URLEncoder.encode(resourceId, "UTF-8");

        HttpRequest restRequest = REQUEST_FACTORY.buildPutRequest(
                new GenericUrl(importCmd), content);
        restRequest.setReadTimeout(0);
        
        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse updateOrgConfig(ServerProfile profile,
                                               String resource,
                                               String resourceId,
                                               String subResource,
                                               String subResourceId,
                                               String payload)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource + "/"
                + subResourceId;

        return executeAPIPost(profile, payload, importCmd);
    }
    
	public static HttpResponse updateOrgConfigUpload(ServerProfile profile, 
													String resource,
													String resourceId,
													String filePath) throws IOException {

		byte[] file = Files.readAllBytes(new File(filePath).toPath());
		ByteArrayContent content = new ByteArrayContent("application/octet-stream", file);
		
		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
		+ "/" + resource+"/"+resourceId;

		HttpRequest restRequest = REQUEST_FACTORY.buildPutRequest(new GenericUrl(importCmd), content);
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}

    public static HttpResponse deleteOrgConfig(ServerProfile profile, 
                                                String resource,
                                                String resourceId)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                                + profile.getApi_version() + "/organizations/"
                                + profile.getOrg() + "/" + resource + "/"
                                + URLEncoder.encode(resourceId, "UTF-8");

        HttpRequest restRequest = REQUEST_FACTORY.buildDeleteRequest(
                                                    new GenericUrl(importCmd));
        restRequest.setReadTimeout(0);

        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }
    
	public static HttpResponse deleteOrgResourceFileConfig(ServerProfile profile, String resource, String resourceId)
			throws IOException {

		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
				+ "/" + resource + "/" + resourceId;

		HttpRequest restRequest = REQUEST_FACTORY.buildDeleteRequest(new GenericUrl(importCmd));
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}

    public static HttpResponse getOrgConfig(ServerProfile profile, 
                                                String resource) 
            throws IOException {

        HttpRequest restRequest = REQUEST_FACTORY.buildGetRequest(
                new GenericUrl(profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/" + resource));
        restRequest.setReadTimeout(0);

        //logger.debug(PrintUtil.formatRequest(restRequest));

        HttpResponse response = null;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) return null;
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse getOrgConfig(ServerProfile profile,
                                            String resource,
                                            String resourceId,
                                            String subResource,
                                            String subResourceId)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg()  + "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource + "/"
                + subResourceId;

        return executeAPIGet(profile, importCmd);
    }

    /***************************************************************************
     * API Config - get, create, update
     **/
        public static HttpResponse createAPIConfig(ServerProfile profile, 
                                                    String api,
                                                    String resource,
                                                    String payload)
            throws IOException {

        ByteArrayContent content = new ByteArrayContent("application/json", 
                                                            payload.getBytes());

        String importCmd = profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/apis/"
                            + api + "/" + resource;

        HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(
                new GenericUrl(importCmd), content);
        restRequest.setReadTimeout(0);

        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse createAPIConfig(ServerProfile profile,
                                               String api,
                                               String resource,
                                               String resourceId,
                                               String subResource,
                                               String payload)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/apis/"
                + api+ "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource;

        return executeAPIPost(profile, payload, importCmd);
    }
        
        public static HttpResponse createAPIConfigUpload(ServerProfile profile, String api, String resource, String filePath)
    			throws IOException {
    		byte[] file = Files.readAllBytes(new File(filePath).toPath());
    		ByteArrayContent content = new ByteArrayContent("application/octet-stream", file);

    		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
    							+ "/apis/" + api
    							+ "/" + resource;

    		HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(new GenericUrl(importCmd), content);
    		restRequest.setReadTimeout(0);

    		//logger.info(PrintUtil.formatRequest(restRequest));

    		HttpResponse response;
    		try {
    			//response = restRequest.execute();
    			response = executeAPI(profile, restRequest);
    		} catch (HttpResponseException e) {
    			logger.error("Apigee call failed " + e.getMessage());
    			throw new IOException(e.getMessage());
    		}

    		return response;
    	}

    public static HttpResponse updateAPIConfig(ServerProfile profile, 
                                                String api,
                                                String resource,
                                                String resourceId,
                                                String payload)
            throws IOException {

        ByteArrayContent content = new ByteArrayContent("application/json", 
                                                            payload.getBytes());

        String importCmd = profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/apis/"
                            + api + "/" + resource + "/"
                            + URLEncoder.encode(resourceId, "UTF-8");

        HttpRequest restRequest = REQUEST_FACTORY.buildPutRequest(
                new GenericUrl(importCmd), content);
        restRequest.setReadTimeout(0);

        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse updateAPIConfig(ServerProfile profile,
                                               String api,
                                               String resource,
                                               String resourceId,
                                               String subResource,
                                               String subResourceId,
                                               String payload)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/apis/"
                + api + "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource + "/"
                + subResourceId;

        return executeAPIPost(profile, payload, importCmd);
    }
    
    public static HttpResponse updateAPIConfigUpload(ServerProfile profile, String api, String resource, String resourceId,
			String filePath) throws IOException {

		byte[] file = Files.readAllBytes(new File(filePath).toPath());
		ByteArrayContent content = new ByteArrayContent("application/octet-stream", file);

		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
							+ "/apis/"+ api
							+ "/" + resource + "/" + resourceId;

		HttpRequest restRequest = REQUEST_FACTORY.buildPutRequest(new GenericUrl(importCmd), content);
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}

    public static HttpResponse deleteAPIConfig(ServerProfile profile, 
                                                String api,
                                                String resource,
                                                String resourceId)
            throws IOException {


        String importCmd = profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/apis/"
                            + api + "/" + resource + "/"
                            + URLEncoder.encode(resourceId, "UTF-8");

        HttpRequest restRequest = REQUEST_FACTORY.buildDeleteRequest(
                new GenericUrl(importCmd));
        restRequest.setReadTimeout(0);

        //logger.info(PrintUtil.formatRequest(restRequest));

        HttpResponse response;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse getAPIConfig(ServerProfile profile,
                                                String api,
                                                String resource) 
            throws IOException {

        HttpRequest restRequest = REQUEST_FACTORY
                .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/apis/"
                        + api + "/" + resource));
        restRequest.setReadTimeout(0);
        
        //logger.debug(PrintUtil.formatRequest(restRequest));

        HttpResponse response = null;
        try {
        	//response = restRequest.execute();
            response = executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) return null;
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    public static HttpResponse getAPIConfig(ServerProfile profile,
                                            String api,
                                            String resource,
                                            String resourceId,
                                            String subResource,
                                            String subResourceId)
            throws IOException {

        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/apis/"
                + api + "/" + resource + "/"
                + URLEncoder.encode(resourceId, "UTF-8")
                + "/" + subResource + "/"
                + subResourceId;

        return executeAPIGet(profile, importCmd);
    }

    public static HttpResponse deleteAPIResourceFileConfig(ServerProfile profile, String api, String resource, String resourceId)
			throws IOException {

		String importCmd = profile.getHostUrl() + "/" + profile.getApi_version() + "/organizations/" + profile.getOrg()
				+ "/apis/"+api
				+ "/" + resource + "/" + resourceId;

		HttpRequest restRequest = REQUEST_FACTORY.buildDeleteRequest(new GenericUrl(importCmd));
		restRequest.setReadTimeout(0);

		//logger.info(PrintUtil.formatRequest(restRequest));

		HttpResponse response;
		try {
			//response = restRequest.execute();
			response = executeAPI(profile, restRequest);
		} catch (HttpResponseException e) {
			logger.error("Apigee call failed " + e.getMessage());
			throw new IOException(e.getMessage());
		}

		return response;
	}
    
    public static void initMfa(ServerProfile profile) throws IOException {

    	// any simple get request can be used to - we just need to get an access token
    	// whilst the mfatoken is still valid
    	
        // trying to construct the URL like
        // https://api.enterprise.apigee.com/v1/organizations/apigee-cs/apis/
        // success response is ignored
    	if (accessToken == null) {
			logger.info("=============Initialising MFA================");
	
	        HttpRequest restRequest = REQUEST_FACTORY
	                .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
	                        + profile.getApi_version() + "/organizations/"
	                        + profile.getOrg() + "/apis/"));
	        restRequest.setReadTimeout(0);
	
	        try {
	            HttpResponse response = executeAPI(profile, restRequest);            
	            //ignore response - we just wanted the MFA initialised
	            logger.info("=============MFA Initialised================");
	        } catch (HttpResponseException e) {
	            logger.error(e.getMessage());
	            //throw error as there is no point in continuing
	            throw e;
	        }
    	}
    }

    private static HttpResponse executeAPIGet(ServerProfile profile, String importCmd)
            throws IOException {

        HttpRequest restRequest = REQUEST_FACTORY
                .buildGetRequest(
                        new GenericUrl(importCmd));
        restRequest.setReadTimeout(0);

        HttpResponse response;
        try {
            response = RestUtil.executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) return null;
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }

    private static HttpResponse executeAPIPost(ServerProfile profile, String payload,
                                               String importCmd)
            throws IOException {

        ByteArrayContent content = new ByteArrayContent("application/json",
                payload.getBytes());

        HttpRequest restRequest = REQUEST_FACTORY
                .buildPostRequest(
                        new GenericUrl(importCmd), content);
        restRequest.setReadTimeout(0);

        HttpResponse response;
        try {
            response = RestUtil.executeAPI(profile, restRequest);
        } catch (HttpResponseException e) {
            logger.error("Apigee call failed " + e.getMessage());
            throw new IOException(e.getMessage());
        }

        return response;
    }
  
    /**
     * 
     * @param profile
     * @param request
     * @return
     * @throws IOException
     */
    private static HttpResponse executeAPI(ServerProfile profile, HttpRequest request) 
            throws IOException {
    	HttpHeaders headers = request.getHeaders();
    	try {
    		MgmtAPIClient client = new MgmtAPIClient();
    		if (profile.getServiceAccountJSONFile() == null || profile.getServiceAccountJSONFile().equalsIgnoreCase("")) {
				logger.error("Service Account file is missing");
				throw new IOException("Service Account file is missing");
			}
            File serviceAccountJSON = new File(profile.getServiceAccountJSONFile());
            accessToken = client.getGoogleAccessToken(serviceAccountJSON);
            logger.debug("**Access Token** "+ accessToken);
    		headers.setAuthorization("Bearer " + accessToken);
    	}catch (Exception e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
         }
        
    	logger.info(PrintUtil.formatRequest(request));
        return request.execute();
    }
    
}
