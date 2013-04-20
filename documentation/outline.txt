DeckWorkout

Fit Partner Deck

“Deck of Cards” Workout

with a Remote User

* * * * *

Table of Contents {.c7}
=================

[Table of Contents](#h.dk77zmmfjtrn)

[Authors](#h.hx8tj9hqbu0g)

[Abstract](#h.j9jnpvfsn5nb)

[Review of similar Apps](#h.bpjruww6zobz)

[Mock-Up and Screen Shot](#h.m8s35jliatgs)

[Functional Overview](#h.ytspho7ckeeb)

[Installation (Evan)](#h.60ymsys7dgr1)

[User Management (Nirmal)](#h.ufxy3oxvpj8u)

[Deck Inspection and Customization (Evan)](#h.cnkgica7oqrj)

[Navigation (Evan)](#h.nkhg9ax695oq)

[Workout Page Flow (Evan)](#h.54llpxy2bw2w)

[Finding Partner (Nirmal)](#h.h8pcl4nzn2sq)

[Platform](#h.97mlwiks0quu)

[Initial Object Definitions](#h.gi2oplt9rzln)

[Division of Tasks](#h.j9cqpsengr68)

* * * * *

Authors {.c7}
=======

1.  Evan Dana

1.  evan.dana@gmail.com

2.  Nirmal Veerasamy

1.  nirmal.veerasamy@gmail.com

Abstract {.c7}
========

DeckWorkout will provide a convenient way to perform a “deck of cards”
workout. In a deck of cards workout, suits are assigned exercises and
the athlete completes the number of repetitions associated with the
value of the card flipped. For example, if Spades represent sit-ups and
an Ace of Spades is flipped, the athlete would do 14 sit-ups. This app
will allow some customization of the exercises associated with suits and
the size of the deck. Users will be encouraged to continue to use the
app by seeing their progress tracked.

Review of similar Apps {.c7}
======================

See attached folder with screenshots.

These apps do not incorporate a multi-player aspect.

Mock-Up and Screen Shot {.c7}
=======================

Open the DeckApp\_2/index.html in Safari or other compatible browser.

The mock-up referenced above shows how the app might roughly look/behave
on an iOS device. The mock-up represents an idealized app and includes
many features that may need to be skipped due reasonable scoping: the
[Functional Overview](#id.mpl88b2w2y5d) is what we are committing to and
takes precedence.

![](pubimage?id=12AbgHoTgMLQWmwQ4tu_iS8d1cBKmlL0t4CRfHQsz3OA&image_id=19ce9ILkb5e2yezSITJX3O0WhDZBPBMWauoKQKw)

[](#)

Functional Overview {.c7}
===================

Note that any items in gray are considered bonus and will be completed
if extra time allows.

1.  Installation (Evan)
    -------------------

1.  Home Screen icon
2.  Splash Page
3.  App icon

2.  User Management (Nirmal)
    ------------------------

1.  Connecting to any XMPP Servers

1.  The option we are considering is to connect to any existing XMPP
    servers (like gtalk, facebook..) using oauth 2.0. This enables us to
    leverage our already populated contact list of day-to-day chat
    servers and choose friends to participate in workout. On the initial
    research
    [scribe-java](http://www.google.com/url?q=https%3A%2F%2Fgithub.com%2Ffernandezpablo85%2Fscribe-java&sa=D&sntz=1&usg=AFQjCNHxZQ813WVSNCIFj952O6Wuqk8MFQ) looks
    to be a good API set to use for authentication, especially because
    it supports lot of major oauth APIs and could be used to switch
    between like gtalk, facebook, windows live(really!!).
2.  By using oauth type authentication, we only have to ask the user
    approval once (or maybe for set of sessions?) to connect to their
    chat account (and no password though) then the app can sign-in
    without asking for user approval. But I’ve never worked on it, so as
    a back-up the app might have the flow to ask the user to enter the
    XMPP server user/passwd to connect. This is not a good solution, but
    this would be the backup plan, if we encounter any issues with oauth
    type authentication.
3.  Once logged in to the server, user will see the list of friends
    available online. The user can invite any friend to start a workout
    session if the friend also has the app installed.

3.  Deck Inspection and Customization (Evan)
    ----------------------------------------

1.  User can select one of 3 different decks

1.  Create 3 different decks

2.  User can customize at least one deck
3.  2-4 Exercises to choose from (mapped to suit)
4.  4 Suits in every deck (mapped to exercise)
5.  Corrolate face cards to values
6.  Set speech to “on” or “off”
7.  Customizations per deck

1.  Assign exercise to suit
2.  Choose number of cards in deck from set of provided options

1.  Generates a deck with appropriate distribution of cards for each
    suit

3.  Set value override of face cards
4.  Set exercise override associated with all face cards

8.  Store customizations
9.  If app is updated, customizations will not be lost
10. Saves of data will take place immediately upon selection.
11. Unexpected interruption

1.  Leave any saved modifications as is and ignore unsaved
    modifications.

12. Navigation

1.  Uses Back button
2.  Shows tabbed menu on first page depth of customization

4.  Navigation (Evan)
    -----------------

13. Tabbed navigation

1.  Show tab as selected if on that page

14. Back button

1.  Select pages put on stack
2.  If enabled on the page, will bring user to last page on the stack

5.  Workout Page Flow (Evan)
    ------------------------

15. Start requested
16. Capture date time of start
17. Capture deck details
18. Timer

1.  Start counting up timer
2.  Show counting up timer
3.  Format time in HH:MM:SS
4.  Ensure that the timer isn’t still ticking when the app is in the
    background.

19. Countdown for 5 seconds

1.  Show 5, 4, 3, 2, 1

20. Show Card

1.  Check if cards remain

1.  If so, continue
2.  If not, go to End Deck

2.  Show random card from deck
3.  Start timer
4.  Remove this card from available cards in deck
5.  Speak exercise of card, associated with suit
6.  Speak number of reps, associated with card value

21. On touch event

1.  Stop timer
2.  Log time
3.  Log number of reps completed and exercise
4.  Go to Show Card

22. End Deck

1.  Save information to user’s workout log

1.  Deck completed: Boolean
2.  Date time deck started
3.  Deck duration
4.  Count of reps per exercise
5.  See [Initial Object Definitions](#id.i17ie8d0bbiq)

2.  Update Stats

1.  Iterate through stats

1.  If stat has been exceeded (e.g. faster deck time for same deck or
    total pushup count) then update stat

1.  If stat is updated, does it also enable a badge?

1.  If so enable that badge

23. Unexpected interruption

3.  Make sure the game is paused.

24. Unexpected resume

1.  Should pick up where last left off

6.  Finding Partner (Nirmal)
    ------------------------

25. From where: From existing chat server’s (gtalk, facebook ...)
    contact list. We are planning to use
    [XMPP](http://tools.ietf.org/html/rfc6120) based communication
    between “Fit Partner Deck” apps over internet. (The purpose of XMPP
    is to enable the exchange of relatively small pieces of structured
    data over a network between any two or more entities).
26. Show online users: With the help of XMPP client API
    ([Smack](http://www.igniterealtime.org/projects/smack/) is the
    winner at the moment)

1.  Get the list of available online friends
2.  Display list to the user
3.  Clicking on friend’s name connects to friend

27. Connect to User: Once the user chooses a friend, the notification
    would be showed by the app in the other side. If acknowledged by the
    friend then the session starts at that time (connect more than 2?
    possible, but not going to complicate for now).
28. Communications: All communications are 2-way handshake (notify and
    acknowledge) for the end-users, so that accurate timings can be
    marked for each activity if required.

1.  Choose the deck for workout (notify and ack)
2.  Start the card to workout (notify and ack), then workout started.
3.  Once finished card, click the done button (the timer is marked at
    this point and sent to the other side as well).
4.  Once both have finished the card, it is considered done.
5.  Next card is served to both at the same time.
6.  If all the cards are complete then gather the deck’s workout stats
    and display to users. Otherwise go back to step d.ii and continue.

29. Handling issues:

1.  When Internet connection drops briefly during communication, try to
    queue up the required information and send once the connection is
    established (the queue size should be very limited).
2.  Follow i, when the partner goes offline briefly.
3.  If the connection is interrupted for more than 20 seconds, alert the
    user and then switch the mode to solo workout mode.
4.  If the user resumes online after few minutes, maybe start a new
    stream to share the workout stats so far? or maybe start a new
    session by copying the existing stat? or just simply start a fresh
    session and let the user to skip to the card?

Platform {.c7}
========

Initially intended to be an iOS application, we decided to go with
Android because of device accessibility across the team. Please pardon
the iOS aesthetic in the interactive mock up as it was created before
the platform switch decision.

[](#)

Initial Object Definitions {.c7}
==========================

Please refer to accompanying document basicObjects.json.

These object definitions represent an idealized app and include many
features that may need to be skipped due reasonable scoping: the
[Functional Overview](#id.mpl88b2w2y5d) is what we are committing to and
takes precedence.

Division of Tasks {.c7}
=================

See labels in [Functional Overview](#id.mpl88b2w2y5d) after section
title.

page /                        DeckWorkout

Published by [Google
Drive](//docs.google.com/ "Learn more about Google Drive")–[Report
Abuse](//docs.google.com/abuse?id=12AbgHoTgMLQWmwQ4tu_iS8d1cBKmlL0t4CRfHQsz3OA)–Updated
automatically every 5 minutes
