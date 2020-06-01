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

import java.util.ArrayList;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  ArrayList<String> comments = new ArrayList<String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

	//add some hard coded values to comments list
    comments.add("hello");
    comments.add("world");
    comments.add("so cool!");

	//convert comments to json
	String json = convertToJsonUsingGson(comments);

	//set response to comments in json form
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /**
   * Converts an ArrayList instance into a JSON string using the Gson library.
   */
  private String convertToJsonUsingGson(ArrayList list) {
    Gson gson = new Gson();
    String json = gson.toJson(list);
    return json;
  }
}
