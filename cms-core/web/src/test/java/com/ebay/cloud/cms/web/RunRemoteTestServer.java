/*
Copyright [2013-2014] eBay Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


/* 
Copyright 2012 eBay Software Foundation 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

    http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/ 

package com.ebay.cloud.cms.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import com.ebay.cloud.cms.sysmgmt.server.CMSConfig;
import com.ebay.cloud.cms.sysmgmt.server.CMSServer;

/**
 * @author jianxu1
 */
public class RunRemoteTestServer {

    private static URI getBaseURI(int port) {
        return UriBuilder.fromUri("http://localhost:" + port + "/ui/home.html").build();
    }

    private static Server   jettyServer;

    protected static Server startJettyServer(int port) throws Exception {
        System.out.println("Starting jetty...");

        Server server = new Server(port);
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        ServletContextHandler rootHandler = new ServletContextHandler(contexts, "/", ServletContextHandler.SESSIONS);
        
//        rootHandler.addFilter(CALServletFilter.class, "/*", FilterMapping.REQUEST);
        
        ServletHolder apiServletholder = new ServletHolder(com.sun.jersey.spi.container.servlet.ServletContainer.class);
        apiServletholder.setInitParameter("com.sun.jersey.config.property.packages",
                "com.ebay.cloud.cms.service.resources.impl");
        apiServletholder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        apiServletholder.setInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.PostReplaceFilter,com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");
        apiServletholder.setInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters", "com.sun.jersey.api.container.filter.GZIPContentEncodingFilter");
        rootHandler.addServlet(apiServletholder, "/cms/*");

        // ui servlet settting -
        // http://docs.codehaus.org/display/JETTY/Servlets+Bundled+with+Jetty
        ServletHolder uiServletHolder = new ServletHolder(new ResourceDefaultServlet());
        uiServletHolder.setInitParameter("resourceBase", getUIProjectResource());
        uiServletHolder.setInitParameter("dirAllowed ", Boolean.FALSE.toString());
        rootHandler.addServlet(uiServletHolder, "/ui/*");

        server.setHandler(rootHandler);

        server.start();
        return server;
    }

    @SuppressWarnings("serial")
    public static class ResourceDefaultServlet extends DefaultServlet {
        @Override
        public Resource getResource(String pathInContext) {
            if (pathInContext.startsWith("/ui")) {
                if (pathInContext.length() == 3 || pathInContext.length() == 4) {
                    pathInContext = "home.html";
                } else {
                    pathInContext = pathInContext.substring(3);
                }
            }
            return super.getResource(pathInContext);
        }
    }

    public static void main(String[] args) throws Exception {

        startServer(args);

        int port = jettyServer.getConnectors()[0].getPort();
        System.out.println(String.format("CMS app started with WADL available at "
                + "%sapplication.wadl\nTry out %s\nHit enter to stop it...", getBaseURI(port), getBaseURI(port)));
        System.in.read();
        stopServer();
    }

    public static void stopServer() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

    public static void startServer(String[] args) throws Exception {

        Map<String, String> dbConfig = new HashMap<String, String>();
//        dbConfig.put(CMSConfig.MONGO_CONNECTION, "phx7b02c-718441.stratus.phx.ebay.com");
        dbConfig.put(CMSConfig.MONGO_CONNECTION, "phx5qa01c-2b82.stratus.phx.qa.ebay.com");
        dbConfig.put(CMSConfig.SERVER_NAME, "zhuang1");
        
        CMSServer.getCMSServer(dbConfig).start();

        jettyServer = startJettyServer(9090);
    }

    private static String getUIProjectResource() throws IOException {
        String uiProjectPath = System.getProperty("CMS_UI", "../cms-ui/src/main/webapp/ui/");
        System.out.println("using CMS_UI :" + uiProjectPath);
        File f = new File(uiProjectPath);
        if (f.exists()) {
            System.out.println("start ui project at uri: /ui");
            return f.getCanonicalPath();
        }
        return null;
    }
}
