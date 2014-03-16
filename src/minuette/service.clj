(ns minuette.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [io.pedestal.service.log :as log]
              [ring.util.response :as ring-resp]
              [minuette.quartzite.scheduler :as scheduler]
    ))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))


;;==================================================================================
;
;;==================================================================================
(defn get-schedule
  "get things by type, ret from peer a list of thing in a new line sep string"
  [req]
  ; path segment in req contains request params, /api/:thing, /api/lecture
  (let [schedid (get-in req [:path-params :schedid])
        schedule (scheduler/get-schedule schedid)
        result {:status 200 :schedule schedule}
        jsonresp (bootstrap/json-response result)] ; conver to keyword for query
    (newline)
    (println "service get-schedule " (count schedule) " " schedule)
    jsonresp))
    ; (-> (ring-response/response things)
    ;     (ring-response/content-type "application/edn"))))


;;==================================================================================
; POST form details to add a schedule
; reqbody is body when xhr-request on api service side
;;==================================================================================
(defn add-schedule
  "add a schedule upon post request, request contains post data"
  [{reqbody :edn-params :as request}]  ; reqbody is json post data
  (let [;resp (bootstrap/json-print {:result msg-data})
        added-schedule (scheduler/add-schedule (:details reqbody))
        result {:status 200 :schedule added-schedule}
        jsonresp (bootstrap/json-response result)
        ]
    ; INFO  minuette.service - {:line 52, "service add-schedule " nil}
    (log/info "add-schedule " request reqbody (:details reqbody))
    jsonresp))

;==================================================================================
;; define routing table with verb map and route interceptor
;;==================================================================================
(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/about" {:get about-page}]
     ["/api/schedule/:schedid" {:get get-schedule}]
     ["/api/schedule" {:post add-schedule}]
    ]]])


; url-for convert route interceptor to URL with defined routing table.
;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by minuette.server/init service/service
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
