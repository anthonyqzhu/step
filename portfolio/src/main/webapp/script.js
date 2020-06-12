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

// Marker information for notable restaurants
const restaurants = [
  {
      name: 'Rich JC',
      lat: 42.275201,
      long: -83.732619,
      description: "An amazing counter-side Korean restaurant. \
                Highly recommend the Budae Jigae and the spam fried rice. \
                Perfect warm spicy food for a cold day."
  },
  {
      name: 'Frita Batidos',
      lat: 42.280368,
      long: -83.749291,
      description: "Super rich Cuban food. The burgers (Fritas) \
                       are cooked perfectly medium rare, the fruit \
                       smoothies (Batidos) are refreshing and flavorful. \
                       often considered the best restaurant in town."
  },
  {
      name: 'Panda Express',
      lat: 42.290879,
      long: -83.717648,
      description: "A staple of the engineering school, you will find \
                       many a CS major eating here after lecture. \
                       It ain't authentic, but it sure tastes good."
  },
  {
      name: 'Piada',
      lat: 42.278907,
      long: -83.740573,
      description: "A chipotle style restaurant serving Italian street food. \
               Very good for a quick bite as there is a student deal on \
               a pasta dish with drink."
  },
  {
      name: 'Tomukun Korean BBQ',
      lat: 42.279500,
      long: -83.742775,
      description:"This restaurant is for when you want to \
                            splurge and chow down like there's no \
                            tomorrow. On the pricey side but worth it \
                            for the incredible flavor."
  }
];

// Holds custom icons for map
var icons = {
    michigan: {
        icon: {
            url: '/images/michigan_logo.png',
            scaledSize: new google.maps.Size(32, 32),
        }
    }
};

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
                createListTextElement(comments[i].text));
            if(comments[i].imageURL !== null) {
                commentsListElement.appendChild(
                  createListImageElement(comments[i].imageURL));
            }
        }
    });
}

/**
 * Delete all comments from the database
 */
function deleteComments() {
    console.log("Deleting comments");
    promise = fetch(new Request('/delete-data', {method: 'POST'})).then(() => {
        fetchComments();
    });
}

/** Creates an <li> element containing text. */
function createListTextElement(text) {
    const liElement = document.createElement('li');
    liElement.innerText = text;
    return liElement;
}

/** Creates an <li> element containing an image. */
function createListImageElement(imageURL) {
    const liElement = document.createElement('li');
    const imgElement = document.createElement('img');
    imgElement.setAttribute('src', imageURL);
    imgElement.setAttribute('class', 'list-image');
    liElement.appendChild(imgElement);
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

/** Creates a map and adds markers with drop animation as well */
function initMap() {
  var mich_loc = {lat: 42.278046, lng: -83.738220};
  const map = new google.maps.Map(
    document.getElementById('map'),
    {center: mich_loc, zoom: 14});
  map.setTilt(45);

  var mich_marker = new google.maps.Marker({
    position: mich_loc,
    icon: icons['michigan'].icon,
    map: map,
    title: "Michigan Campus",
    draggable: true
  });
  mich_marker.addListener('click', () => {
    if (mich_marker.getAnimation() !== null) {
        mich_marker.setAnimation(null);
    } else {
        mich_marker.setAnimation(google.maps.Animation.BOUNCE);
    }
  });

  for(var i = 0; i < restaurants.length; i++) {
      var restaurant = restaurants[i];
      addRestaurantMarkerWithTimeout(restaurant, i * 200, map);
  }
}

/* Used to add marker to map with delay to create animation */
function addRestaurantMarkerWithTimeout(restaurant, timeout, map) {
    console.log("Adding marker");
    window.setTimeout(function() {
        const marker = new google.maps.Marker({
            position: {lat: restaurant.lat, lng: restaurant.long},
            map: map,
            title: restaurant.name,
            draggable: true,
            animation: google.maps.Animation.DROP
        });

        const infoWindow = new google.maps.InfoWindow({content: restaurant.description});
        marker.addListener('click', () => {
            infoWindow.open(map, marker);
        });
    }, timeout);
}

/* Sets up the comment form with the blobstore image upload URL */
function fetchBlobstoreUrlAndShowForm() {
  fetch('/create-image-upload-url')
      .then((response) => {
        return response.text();
      }).then((imageUploadUrl) => {
        const commentForm = document.getElementById('comment-form');
        commentForm.action = imageUploadUrl;
        commentForm.classList.remove('hidden');
      });
}

/* Initializes the page */
function startup() {
    initMap();
    fetchBlobstoreUrlAndShowForm();
}