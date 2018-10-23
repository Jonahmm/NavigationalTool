# NavigationalTool
An app to help navigate around difficult building layouts.

## Requirements

### Stakeholders

*   Client
    *   Needs to be able to apply the app to the new maths building
*   Building Users
    *   Students
        *   Students will be the majority of users of the product
    *   Staff
        *   The building is new so staff may need help to navigate
    *   Visitors
        *   Visitors will not know the layout so will need navigational help
    *   Disabled Users
        *   Disabled routes will require different pathways
*   The University
    *   The building owner/manager would need to be involved if we were to use beacons etc

### Functional Requirements

*   The product must be an app that works on Android devices
*   The model must use a modular design that allows the client to switch between building layouts
    *   There should be nothing hard-coded except a file path giving building information
*   It must find the shortest route allowed from origin to destination
*   The navigation must support different routes for disabled users and different access levels
*   The route must be displayed graphically on screen
    *   Should display (at least) a floor plan with the route overlaid

### Non-Functional Requirements

*   The app should have an intuitive and usable interface
    *   Users should be able to use the app properly with under 5 minutes' screen time
*   The interface should appear polished and perform smoothly
    *   The UI should not wait for the model, instead display 'calculating' or similar when working
*   It should be optimised enough to calculate any route in under 2 seconds
*   It should be packaged small enough to fit on most smartphones
    *   ≤100MB
*   The app should be reliable and shouldn't crash or hang
    *   Test all sensible use cases: different rooms, access levels (inc. disabled) etc

---Previous
#### Top priority (ie: "Must")
- Work on Android.
- Be able to switch out the building layout easily.
- Find shortest route to destination.
- Show this route on a top down floor plan.

#### Mid priority (ie: "Should")
- Have different access levels for different routes.
- Cointain disabled friendly routes.
- Show 360* pictures outside each major location or junction.
- Be properly documented with more than just descriptive method & variable names.
- Clean up the floor plan & make the UI look good.

#### Low priority (ie: "Would be good to")
- Figure out where you are automatically.
- Group rooms together. (eg: "Lectures halls", "Offices", etc)
- Show alternative routes.
- Work on iOS. (very very low priority)

## Notes
We're using the layout of the Physics Building whilst making the app, not the Fry Building.

I think we agreed we'd be using Eclipse, though this shouldn't really matter as long as the .gitignore includes everything it needs to.

We should agree to use the same Java version. Java 8 would probably be best, as we used that for OOP last year and it's the most stable currently, even though Java 10/11 should be okay.

We should try to avoid any ideas that have phones talking to each other, as that'd require a server somewhere which would not be ideal.

It'd be good to try and meet up in person for more than just the mentor and client meetings, just to make sure we're communicating properly and all up to speed on all decisions/issues that we have. Either Wednesday, the weekend, or some time after a shared lecture is probably best for this.
