# foodiebay
Sample App to show filterable Food Trucks Data on a Map

Problem Statement: 
  Food Trucks Problem: Create an app that tells the user what types of food trucks might be found near a specific location on a map. The data is available on DataSF: https://data.sfgov.org/Permitting/Mobile-Food-Facility-Permit/rqzj-sfat.
  
Solution:
  FoodieBay is an app which retrieves data from SFOpen Data (Mobile Food Facility Permit) and geolocalizes the Food facilities on a map. It also helps the user determine the types of FoodFacilities which might be found near a particular location.
  
Technical choices taken:
  1.) Retrofit Library used for Network Calls with parsing using Gson Library. Retrofit handles entire network call along with JSON/XML Parsing including Serialization/Deserialization. Avoids writing boilerplate code for initiatng network calls, maintaining asynchronous and synchronous calls. Also, Retrofit is very easy to use with good documentation and community support available.
  
  2.) Google Maps Utils Library used for making Google Map Marker Clusters. The rationale behind using this util library was the huge chunk of Food Facilities dataset available within a concentrated location. Using this library for the current use case instead of just adding Google Markers, proved to be efficient both in memory and performance aspects of the app. As an example, San Fransisco Food Facility Permit Data contains around 700 odd Food Facilities and showing this dataset on maps using markers is not good in terms of Memory for an app and this is where using Google Maps Cluster Utils helped.
  
  In this project, the Splash Screen Bottom-Up animation is something that i like a lot. Such animations are not used much in Splash screens acroos Android apps. This is one idea I had for Shuttl app and implemented the same there and thus chose to showcase the same in this project as well. 





