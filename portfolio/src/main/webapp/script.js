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

/** 
 * Adds a random quote to the page.
 */
function addRandomQuote() {

  const tv_quotes =
      ['"Winter is coming."', '"Most men would rather deny a hard truth than face it."', '"What do we say to the Lord of Death?"\n"Not today."']

  // Pick a random TV quote
  const quote = tv_quotes[Math.floor(Math.random() * tv_quotes.length)];

  // Add it to the page.
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

/**
 * Add all comments to the page
 */
function addComments() {
    
    var response_json = fetch('/data?num_comments=' + document.getElementById("num_comments_field").value).then(response => response.json());
    console.log("Displaying comments");
    console.log(response_json);
    response_json.then((comments) => {
        const commentsListElement = document.getElementById('comments-container');
        commentsListElement.innerHTML = '';
        var i;
        for (i = 0; i < comments.length; i++) {
            commentsListElement.appendChild(
                createListElement(comments[i]));
        }
    });
}

/**
 * Delete all comments from the database
 */
function deleteComments() {
    console.log("Deleting comments");
    fetch(new Request('/delete-data', {method: 'POST'})).then(addComments());
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/**
 * Enables collapsibles to expand and close on click
 */
var coll = document.getElementsByClassName("collapsible");
var i;

for (i = 0; i < coll.length; i++) {
    coll[i].addEventListener("click", function() {
        this.classList.toggle("active");
        var summary = this.nextElementSibling;
        if (summary.style.maxHeight){
        summary.style.maxHeight = null;
        } else {
        summary.style.maxHeight = summary.scrollHeight + "px";
        } 
    });
}
