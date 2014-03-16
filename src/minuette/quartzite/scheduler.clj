(ns minuette.quartzite.scheduler
    (:require [clojurewerkz.quartzite.scheduler :as qs]
              [clojurewerkz.quartzite.jobs :as jobs]
              [clojurewerkz.quartzite.triggers :as triggers]
              [clojurewerkz.quartzite.jobs :refer [defjob]]
              [clojurewerkz.quartzite.schedule.simple :refer 
                  [with-repeat-count with-interval-in-milliseconds] :as simple]
              [clojurewerkz.quartzite.schedule.cron :refer [cron-schedule] :as cron]
              [clojurewerkz.quartzite.schedule.calendar-interval :refer 
                  [with-interval-in-days] :as calint]
              [clojurewerkz.quartzite.schedule.daily-interval :refer 
                  [monday-through-friday starting-daily-at time-of-day 
                   ending-daily-at with-interval-in-minutes] :as dayint]
              ))


; this module interface to quartzite lib to Quartz.
(defn get-schedule
  [schedid]
  (prn "get-schedule " schedid)
  schedid)


(defn add-schedule
  "add a schedule based on details. details includes "
  [details]
  (prn "add-schedule " details)
  details)

