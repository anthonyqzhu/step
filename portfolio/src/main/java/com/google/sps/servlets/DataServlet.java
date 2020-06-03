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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comment data as a JSON list */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  // Create an ArrayList to store the comments 
  private static final ArrayList<String> commentsList = new ArrayList<String>();

  private static final String COMMENT_FORM_PARAMETER = "comment-text";
  private static final String COMMENT_KIND = "Comment";
  private static final String TEXT_PROPERTY = "text";
  private static final String TIMESTAMP_PROPERTY = "timestamp";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Convert comments to json
    String json = convertToJsonUsingGson(commentsList);

    // Set response to comments in json form
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the input from the form and log time of comment
    String commentText = request.getParameter(COMMENT_FORM_PARAMETER);
    long timestamp = System.currentTimeMillis();

    commentsList.add(commentText);

    // Create entity with comment text and timestamp info
    Entity taskEntity = new Entity(COMMENT_KIND);
    taskEntity.setProperty(TEXT_PROPERTY, commentText);
    taskEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);

    // Store comment task with datastore instance
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * Converts an ArrayList instance into a JSON string using the Gson library.
   */
  private static String convertToJsonUsingGson(ArrayList<String> list) {

    Gson gson = new Gson();
    return gson.toJson(list);
  }
}
