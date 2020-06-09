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
 * Fetch all comments and display them on the page
 */
function fetchComments() {
    
    var response_json = fetch('/data?num_comments=' + document.getElementById("num_comments_field").value).then(response => response.json());
    console.log("Displaying comments");
    console.log(response_json);
    response_json.then((comments) => {
        const commentsListElement = document.getElementById('comments-container');
        commentsListElement.innerHTML = '';
        var i;
        for (i = 0; i < comments.length; i++) {
            commentsListElement.appendChild(
                createListElement(comments[i].text));
        }
    });
}

/**
 * Delete all comments from the database
 */
function deleteComments() {
    console.log("Deleting comments");
    const promise = fetch(new Request('/delete-data', {method: 'POST'}));
    promise.then(() => {
        fetchComments();
    });
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

var markers = [];

var marker;

// Marker information for notable restaurants
var restaurants = [
  ['Rich JC', 42.275201, -83.732619],
  ['Frita Batidos', 42.280368, -83.749291],
  ['Panda Express', 42.290879, -83.717648],
  ['Piada', 42.278907, -83.740573],
  ['Tomukun Korean BBQ', 42.279500, -83.742775]
];

var restaurant_descriptions = [
    ['Rich JC', "An amazing counter-side Korean restaurant. \
                Highly recommend the Budae Jigae and the spam fried rice. \
                Perfect warm spicy food for a cold day."],
    ['Frita Batidos', "Super rich Cuban food. The burgers (Fritas) \
                       are cooked perfectly medium rare, the fruit \
                       smoothies (Batidos) are refreshing and flavorful. \
                       often considered the best restaurant in town."],
    ['Panda Express', "A staple of the engineering school, you will find \
                       many a CS major eating here after lecture. \
                       It ain't authentic, but it sure tastes good."],
    ['Piada', "A chipotle style restaurant serving Italian street food. \
               Very good for a quick bite as there is a student deal on \
               a pasta dish with drink."],
    ['Tomukun Korean BBQ', "This restaurant is for when you want to \
                            splurge and chow down like there's no \
                            tomorrow. On the pricey side but worth it \
                            for the incredible flavor."]
]

var icons = {
          michigan: {
            icon: {
                url: '/images/michigan_logo.png',
                scaledSize: new google.maps.Size(32, 32),
            }
          }
        };

/** Creates a map and adds it to the page. */
function initMap() {
  var mich_loc = {lat: 42.278046, lng: -83.738220};
  const map = new google.maps.Map(
    document.getElementById('map'),
    {center: mich_loc, zoom: 14});
  map.setTilt(45);
  marker = new google.maps.Marker({
    position: mich_loc,
    icon: icons['michigan'].icon,
    map: map,
    title: "Michigan Campus",
    draggable: true
  });
  marker.addListener('click', toggleBounce);
  markers.push(marker);

  for(var i = 0; i < restaurants.length; i++) {
      var restaurant = restaurants[i];
      addMarkerWithTimeout(restaurant, i * 200, map);
  }
}

function addMarkerWithTimeout(restaurant, timeout, map) {
    window.setTimeout(function() {
        console.log("Adding marker")
        markers.push(new google.maps.Marker({
            position: {lat: restaurant[1], lng: restaurant[2]},
            map: map,
            title: restaurant[0],
            draggable: true,
            animation: google.maps.Animation.DROP
        }));
    }, timeout);
}

function toggleBounce() {
  if (marker.getAnimation() !== null) {
    marker.setAnimation(null);
  } else {
    marker.setAnimation(google.maps.Animation.BOUNCE);
  }
}

