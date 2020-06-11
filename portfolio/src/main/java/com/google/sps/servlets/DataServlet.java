// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comment data as a JSON list */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  // Constant parameters for datastore and request operations
  private static final String COMMENT_FORM_PARAMETER = "comment-text";
  private static final String COMMENT_KIND = "Comment";
  private static final String TEXT_PROPERTY = "text";
  private static final String TIMESTAMP_PROPERTY = "timestamp";
  private static final String IMAGE_URL_PROPERTY = "image_url";
  private static final String NUM_COMMENTS_PARAMETER = "num_comments";
  private static final String IMAGE_ELEMENT_NAME = "image";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query(COMMENT_KIND).addSort(TIMESTAMP_PROPERTY, SortDirection.DESCENDING);

        // Construct a read policy for eventual consistency
        ReadPolicy policy = new ReadPolicy(ReadPolicy.Consistency.STRONG);

        // Set the read policy
        DatastoreServiceConfig strongConsistentConfig =
            DatastoreServiceConfig.Builder.withReadPolicy(policy);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(strongConsistentConfig);
        PreparedQuery results = datastore.prepare(query);

        // Create ArrayList of comments and populate from the database
        ArrayList<Comment> commentsList = new ArrayList<Comment>();
        Iterator resultIterator = results.asIterable().iterator();
        String numCommentsString = request.getParameter(NUM_COMMENTS_PARAMETER);
        if(numCommentsString == "" || numCommentsString == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
              "No value entered for number of comments to display");
        } 

        int numComments = 0;
        try {
            numComments = Integer.parseInt(numCommentsString);
            if(numComments < 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  "Negative value entered for number of comments to display");
            }
        } catch(NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
              "Value entered is not an integer");
        }
        for (int i = 0; i < numComments; i++) {
            if(resultIterator.hasNext()) {
                Entity commentEntity = (Entity) resultIterator.next();
                String text = (String) commentEntity.getProperty(TEXT_PROPERTY);
                long timestamp = (long) commentEntity.getProperty(TIMESTAMP_PROPERTY);
                String imageURL = (String) commentEntity.getProperty(IMAGE_URL_PROPERTY);
                    
                Comment comment = new Comment(text, timestamp, imageURL);
                commentsList.add(comment);
            }
        }

        // Convert comments to json
        String json = convertToJsonUsingGson(commentsList);

        System.out.println(json);

        // Set response to comments in json form
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Get the input from the form and log time of comment
        String commentText = request.getParameter(COMMENT_FORM_PARAMETER);
        long timestamp = System.currentTimeMillis();
        String imageURL = getUploadedFileUrl(request);

        // Create entity with comment text and timestamp info
        Entity commentEntity = new Entity(COMMENT_KIND);
        commentEntity.setProperty(TEXT_PROPERTY, commentText);
        commentEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
        commentEntity.setProperty(IMAGE_URL_PROPERTY, imageURL);

        // Store comment task with datastore instance
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        // Redirect back to the HTML page.
        response.sendRedirect("/index.html");
    }

    /**
    * Converts an ArrayList instance into a JSON string using the Gson library.
    */
    private static String convertToJsonUsingGson(ArrayList<Comment> list) {

        Gson gson = new Gson();
        return gson.toJson(list);
    }

    /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
    private String getUploadedFileUrl(HttpServletRequest request) {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> blobKeys = blobs.get(IMAGE_ELEMENT_NAME);

        // User submitted form without selecting a file, so we can't get a URL. (dev server)
        if (blobKeys == null || blobKeys.isEmpty()) {
            return null;
        }

        // Our form only contains a single file input, so get the first index.
        BlobKey blobKey = blobKeys.get(0);

        // User submitted form without selecting a file, so we can't get a URL. (live server)
        BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
        if (blobInfo.getSize() == 0) {
            blobstoreService.delete(blobKey);
            return null;
        }

        // We could check the validity of the file here, e.g. to make sure it's an image file
        // https://stackoverflow.com/q/10779564/873165

        // Use ImagesService to get a URL that points to the uploaded file.
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

        // To support running in Google Cloud Shell with AppEngine's dev server, we must use the relative
        // path to the image, rather than the path returned by imagesService which contains a host.
        try {
            URL url = new URL(imagesService.getServingUrl(options));
            return url.getPath();
        } catch (MalformedURLException e) {
            return imagesService.getServingUrl(options);
        }
    }
}
