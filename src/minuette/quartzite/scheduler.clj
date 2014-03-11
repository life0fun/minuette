(ns minuette.quartzite.scheduler
    (:require [clojurewerkz.quartzite.scheduler :as qs]
              [clojurewerkz.quartzite.jobs :as jobs]
              [clojurewerkz.quartzite.triggers :as triggers]
              [clojurewerkz.quartzite.jobs :refer [defjob]]
              [clojurewerkz.quartzite.schedule.simple :refer 
                  [schedule with-repeat-count with-interval-in-milliseconds]]
              [clojurewerkz.quartzite.schedule.cron :refer [schedule cron-schedule]]
              [clojurewerkz.quartzite.schedule.calendar-interval :refer 
                  [schedule with-interval-in-days]]
              [clojurewerkz.quartzite.schedule.daily-interval :refer 
                  [schedule monday-through-friday starting-daily-at time-of-day 
                   ending-daily-at with-interval-in-minutes]]
              ))

(defn schedule
  [request]
  (prn "scheduling " request)
  request)

