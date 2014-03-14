(ns minuette.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))


;;==================================================================================
; GET request handler to get all things without post any filters
; this fn is deprecated as nav path for all things is [:all 0 :parent]
; XXX deprecated !
;;==================================================================================
(defn get-schedule
  "get things by type, ret from peer a list of thing in a new line sep string"
  [req]
  ; path segment in req contains request params, /api/:thing, /api/lecture
  (let [type (get-in req [:path-params :thing])
        things (peer/get-things (keyword type) [])  ;qpath is null
        result {:status 200 :data things}
        jsonresp (bootstrap/json-response result)] ; conver to keyword for query
    (newline)
    (println "service get peer get-all-things " (count things) type things)
    jsonresp))
    ; (-> (ring-response/response things)
    ;     (ring-response/content-type "application/edn"))))


;;==================================================================================
; POST form details to add a particular thing
; reqbody is body when xhr-request on api service side
;;==================================================================================
(defn add-schedule
  "add a thing upon post request, request contains http post data"
  [{reqbody :edn-params :as request}]
  (let [;resp (bootstrap/json-print {:result msg-data})
        type (get-in request [:path-params :thing])  ; /api/:thing
        added-things (peer/add-schedule (keyword type) (:details reqbody))
        result {:status 200 :data added-things}
        jsonresp (bootstrap/json-response result)
        ]
    (log/info :message "received message" :request request :msg-data reqbody)
    (prn "service got peer adding thing done " added-things)
    jsonresp))

;==================================================================================
;; define routing table with verb map and route interceptor
;;==================================================================================
(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/msgs" {:get subscribe :post publish}
        "/events" {:get wait-for-events}]   ; define the route for later url-for redirect
     ["/about" {:get about-page}]
     ["/api/schedule" {:get get-schedule :post add-schedule}]
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
