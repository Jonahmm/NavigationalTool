# Evaluation

After the beta release we conducted heuristic evaluation to receive verbal feedback on potential areas of improvement. We carried out user talk-throughs with students from the University of Bristol by making them navigate from a fixed source to a fixed destination and taking note of their thoughts regarding the app. We also asked them questions regarding the app's appearance and layout. These observations took place informally in a variety of environments, on the one hand for practical reasons and on the other relating to the idea that this application should be of use to users not only when they are present in the Physics Building, Fry Building etc, but also for exploring or route planning. 

## Feedback Received and Actions Taken
The most common instances of feedback we received are as follows:

1.   “It should have the each professors name associated with their respective room.”
      *   We did consult with our client on implementing this, but the University’s data protection policy prevents us from including any personal details in the application. As an alternative, we have included the roles of some rooms’ occupants, so searching for “head of school” (for example) would find the correct office.
2.   “It should give a written list of directions alongside the visual directions.”
      *   Directions have been implemented since the beta release and are now fully functional.
3.   “Editing the navigation start and end points with buttons is a little confusing - maybe it would be better to have them as text boxes.”
      *   We agreed with this sentiment that the selection buttons were confusing as they were the same design as the functional buttons in the application. It would have been complicated to implement the search feature on the same screen, but as a compromise we redesigned the fields and made the search open by default when these are tapped.
4.   “It would be nice if the app remembered where I’ve searched before - it’s inconvenient to have to type the same thing every time I look for a room.”
      *   As part of our final release, the search screen now displays all recent searches in reverse chronological order when there is no query.
5.   “Why can I use the staff mode without proving I’m a staff member? Isn’t that a security issue?”
      *   As the floorplans are displayed around the building, and restricted areas have UCard-activated doors, we didn’t feel it necessary to implement a check for staff access.
