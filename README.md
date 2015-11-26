# event-site

A simple event site (e.g. for a wedding) application with support for RSVPs to invitations. Written in Clojure with
Reagent in the front-end.

## External dependencies

The application stores the registrations to MongoDB. You must have MongoDB running to run the app.

The email confirmation system expects the server to be running local sendmail.

## Usage

Configure the app by changing settings in config.edn. Put the config file in the same directory
as the jar file. Then just run it:

    $ java -jar event-site.jar

This starts the server and connects to MongoDB.

## Development

Start the development REPL:

    $ lein repl

Start the web server component in the REPL:

    user=> (go)

Front-end development is done with Figwheel. Start it in  another terminal:

    $ lein figwheel

Styles are in LESS, changes to those can be watched and compiled with:

    $ lein less auto

Figwheel loads CLJS and LESS changes to the running browser automatically.

The app is compiled for production use with:

    $ lein uberjar

## Legal

Copyright © 2015 Markus Penttilä

Distributed under the GNU General Public License v2.0.
