(ns minuette.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [io.pedestal.service.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [io.pedestal.service.http.servlet :as ps]
            [io.pedestal.service.log :as log]
            ;; the impl dependencies will go away
            [io.pedestal.service.impl.interceptor :as interceptor]
            [io.pedestal.service.interceptor :refer [definterceptor handler]]
            [io.pedestal.service.http.impl.servlet-interceptor :as servlet-interceptor]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            ;; these next two will collapse to one
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [io.pedestal.service.http.sse :refer :all]
            [ring.util.mime-type :as ring-mime]
            [ring.middleware.session.cookie :as cookie]

            [minuette.quartzite.scheduler :as scheduler]
    ))


;; -------------------------------------------------------------------------------
;; for xhr/request from pedestal app, request body was edn encoded, hence under :end-params key.
;; destrcture [{reqbody :edn-params :as request}], request context map still :as request.
;;

; An interceptor is one instance of an Interceptor record or a map with 
; :enter, :leave, :pause, :resume, and :error keys. 


(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))


; gen uuid session id
(defn- gen-session-id [] (.toString (java.util.UUID/randomUUID)))

; session intercept to extract cookie.
(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))


;;==================================================================================
; retrieve schedule
;;==================================================================================
(defn get-schedule
  "get things by type, ret from peer a list of thing in a new line sep string"
  [req]     ; get req, no post data body.
  ; path segment in req can be parameterized in nested map.
  (let [schedid (get-in req [:path-params :schedid]) ; nested map under :path-params
        schedule (scheduler/get-schedule schedid)
        result {:status 200 :schedule schedule}
        jsonresp (bootstrap/json-response result)] ; conver to keyword for query
    (newline)
    (println "service get-schedule " (count schedule) " " schedule)
    jsonresp))
    ; (-> (ring-response/response things)
    ;     (ring-response/content-type "application/edn"))))


;;==================================================================================
; POST form details to add a schedule, request map.
; {:session {}, :servlet-path "", :path-params {}, :form-params {"detail" "123", ...}
; :cookies {}, :path-info "/api/schedule", :uri "/api/schedule", :request-method :post, :query-string nil,
; :params {"detail" "123", "min" "3", "hour" "14", "weekdays" "[1,2]"}, 
; :headers {"origin" "", "host" "localhost:8080", "accept" "*/*", "content-length" "39", "content-type" "application/x-www-form-urlencoded"
;;==================================================================================
(defn add-schedule
  "add a schedule upon post request, request contains post data"
  [{postbody :edn-params :as request}]   ; xhr/request post data is edn encoded, not usable here.
  (let [;resp (bootstrap/json-print {:result msg-data})
        details (or postbody (:params request))  ; curl post body in :params
        added-schedule (scheduler/add-schedule details)
        result {:status 200 :schedule added-schedule}
        jsonresp (bootstrap/json-response result)
       ]
    ; INFO  minuette.service - {:line 52, "service add-schedule " {"detail" "123", "min" "3", "weekdays" "[1,2]"}
    (log/info "add-schedule request " request)
    jsonresp))

;;==================================================================================
; define routing table with verb map and a list of route interceptors to invoke on request.
; Each route table is a vector of route vectors. [ [:app-name [nested route vectors]] ]
; Nested route vector contain path and verb map {:get/:post destination-interceptor}
; Interceptor can be Ring hdl takes Ring request map and ret Ring resp map, or a vector of Ring handler.
; Every route incl an interceptor vector marked with ^:interceptors [] to be executed.
; [[["/order"  ^:interceptors [verify-request] ^:constraints {:user-id #"[0-9]+"}
;    {:get list-orders :post create-order}
;    ["/:id"  ^:constraints {:user-id #"[0-9]+"} ^:interceptors [verify-order-ownership load-order-from-db]
;    {:get view-order :post [:make-an-order o/create-order]}]]]]
;;==================================================================================
(defroutes routes
  [[["/" {:get home-page}
     ; set intermediate interceptors to be executed for before dest-interceptor.
     ^:interceptors [(body-params/body-params) bootstrap/html-body session-interceptor]
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
              ::bootstrap/port 3000})
