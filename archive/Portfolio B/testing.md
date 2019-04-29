# Development Testing

We throroughly test the functionality of the model of the application. To do this, we used the JUnit Testing framework to develop unit tests for each of the model’s constituent classes. We used black-box testing for this.

The most important component of the system is the **Navigator**. To test it, we created sample **Graph** objects for each unit test. The **Navigator** calls its navigate method on these. The tests then check if the output path is indeed the shortest path and it obeys the access levels chosen. The tests also ensure that the **Navigator** throws an exception if the destination is not accessible to the user.

In order to test the **Building** class and its methods, we created building data files for sample building layouts and used the getGraph method to obtain **Graph** objects based on them. The tests then check if these match the desired output by ensuring that all locations and paths given as a representation of the building are included in the graph. The tests also ensure that exceptions are thrown if there are duplicate locations or paths in the building files.

Since the **Building** class depends on the building data files from the assets folder, the Context of the app has to be emulated in order to access this folder during runtime. This challenge can easily be overcome by placing these tests in the androidTest folder to make them instrumented tests.

The **Graph** class has been tested by creating sample **Location** and **Path** instances for each test. Our tests ensure that locations and paths are added and retrieved as desired. We also check that exceptions are thrown if there are duplicate location IDs in the map, and that a NullPointerException is thrown when `null` is added as a location.

To test the **Location** and the **Path** classes, we designed tests to ensure that NullPointerExceptions are thrown if any of the arguments are `null`. We also designed tests to ensure that **Path** objects must be given at least one valid **User** when constructed.

In an effort to enhance our workflow with continuous testing, we attempted to set up CircleCI to automatically run our tests at each git commit, but encountered compatibility issues with CircleCI's Android image, which is in its alpha stage. We opted to continue our previous strategy of manually testing with each major change, and concentrate on development rather than spend our time debugging this.