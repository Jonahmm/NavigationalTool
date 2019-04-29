# Evaluation

After the beta release we conducted observations on the use of the application amongst our peers as well as our client, in order to receive verbal feedback on potential areas of improvement. We felt this appropriate as our peers being fellow students are potential users of the application and could thus provide us with valuable insight, and our client could provide us with the necessary direction towards his vision for the application. These observations took place informally in a variety of environments, on the one hand for practical reasons and on the other relating to the idea that this application should be of use to users not only when they are present in the Physics Building, Fry Building etc, but also for exploring or route planning. 

## Feedback Received

*   “It should have the each professors name associated with their respective room” - _Srikant_
*   “It should give a written list of directions alongside the visual directions” - _Henry_
*   “Editing the navigation start and end points with buttons is a little confusing - maybe it would be better to have them as text boxes” - _Meg_
*   “It would be nice if the app remembered where I’ve searched before - it’s inconvenient to have to type the same thing every time I look for a room.” - _Kieran_
*   “Why can I use the staff mode without proving I’m a staff member? Isn’t that a security issue?” - _James_
*   Our client expressed a desire for the application to include a Voronoi diagram, as this is part of the theme for the new maths building.

## Action Taken

*   We did consult with our client on implementing Srikant’s suggestion, but the University’s data protection policy prevents us from including any personal details in the application. As an alternative, we have included the roles of some rooms’ occupants, so searching for “head of school” (for example) would find the correct office.
*   Directions have been implemented since the beta release and are now fully functional.
*   We agreed with Meg’s sentiment that the selection buttons were confusing as they were the same design as the functional buttons in the application. It would have been complicated to implement the search feature on the same screen, but as a compromise we redesigned the fields and made the search open by default when these are tapped.
*   As part of our final release, the search screen now displays all recent searches in reverse chronological order when there is no query. 
*   As the floorplans are displayed around the building, and restricted areas have UCard-activated doors, we didn’t feel it necessary to implement a check for staff access.
*   The final application has a programmatically generated Voronoi diagram in the menu header.
