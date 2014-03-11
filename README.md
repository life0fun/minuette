# Minuette

Minuette is a RESTFul scheduling service using Quartz 

Minuette depends on Quartzite, a clojure scheduling library which built on top of Quartz Scheduler to implement scheduling.


## Configuration and Run

1. lein new pedestal-service minuette

2. Start the service: `lein run-dev` \*
3. Go to [localhost:8080](http://localhost:8080/).

4. config/logback.xml config By default, the app logs to stdout and logs/ 

5. curl, or use request python lib to send post requests to create schedules or update, query schedules.


## Quartz Lib and Quartzite

Minuette utilize Quartzite lib that is based on the Quartz Scheduler to support for sophisticated schedules, multiple calendars, Cron expressions, clustering, plugins and more.

Minuette allows manage operation and schedules via RESTful APIs. It also has web app that support query and visualize schedules. Minuette persistents schedules into mysql db to preserve state across crashes. 

##

