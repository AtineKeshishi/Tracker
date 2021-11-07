# About
This tracker allows you to
- track your route while running/walking
- sort the list of activities by date, time, ...
- view individual statistics for every activity
- view total statistics and chart
- share your statistics
- calculate your BMI (Body Mass Index)
 
 # Tech Stack
 * Kotlin
 * MVVM
 * Google Maps
 * Android Architecture Components
     - ViewModel - UI related data holder, lifecycle aware.
     - LiveData - Observable data holder that notify views when underlying data changes.
     - View Binding - Generates a binding class for each XML layout file present in that module and allows you to more easily write code that interacts with views.
     - Navigation component - Fragment routing handler.
* Retrofit + OkHttp - RESTful API and networking client.
* Moshi - Convert Java Objects into their JSON and vice versa
* Hilt - Dependency injection.
* Coroutines - Asynchronous programming
* Glide - Image loading.
