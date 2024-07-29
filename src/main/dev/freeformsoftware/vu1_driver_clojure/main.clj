(ns dev.freeformsoftware.vu1-driver-clojure.main
  (:require
   [clj-http.client :as client]
   [clojure.set :as set]))

(def api-key "cTpAWYuRpA2zx75Yh961Cg")

(defn make-v0-path-prefix
  [{::keys [uri port]
    :or    {uri  "localhost"
            port 5340}}]
  (str "http://" uri ":" port "/api/v0/"))

(defn make-v0-dial-path-prefix
  [{::keys [dial-uid extended-path] :as params}]
  (assert dial-uid)
  (assert extended-path)
  (str (make-v0-path-prefix params) "dial/" dial-uid extended-path))

(defn die-if-status-bad!
  [request-result]
  (assert (= "ok" (get-in request-result [:body :status]))
          (str "Improper request result! " request-result))
  request-result)

(def dial-api-paths
  [{::input    #{::value}
    ::output   #{}
    ::doc      "A 0-100 value"
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/get
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/set"))
                   {:query-params {:key   (::api-key params)
                                   :value (get-in params [::input ::value])}
                    :as           :json}))
                 {})}
   {::input    #{}
    ::output   #{::status}
    ::execute! (fn [params]
                 {::status (-> (client/get
                                (make-v0-dial-path-prefix
                                 (assoc params ::extended-path "/status"))
                                {:query-params {:key (::api-key params)}
                                 :as           :json})
                               (die-if-status-bad!)
                               (get-in [:body :data]))})}

   {::input    #{::name}
    ::output   #{}
    ::doc      "A thirty character string denoting the UI name for a dial"
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/get
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/name"))
                   {:query-params {:key  (::api-key params)
                                   :name (get-in params [::input ::name])}
                    :as           :json}))
                 {})}
   {::input    #{::background-color}
    ::output   #{}
    ::doc      "[r-0-255 g-0-255 b-0-255 w?]"
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/get
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/backlight"))
                   {:query-params {:key   (::api-key params)
                                   :red   (get-in params [::input ::background-color 0] 0)
                                   :green (get-in params [::input ::background-color 1] 0)
                                   :blue  (get-in params [::input ::background-color 2] 0)
                                   :white (get-in params [::input ::background-color 3] 0)}
                    :as           :json}))
                 {})}
   {::input    #{::dial-easing}
    ::output   #{}
    ::doc      ""
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/get
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/easing/dial"))
                   {:query-params {:key    (::api-key params)
                                   :step   (get-in params [::input ::dial-easing :step])
                                   :period (get-in params [::input ::dial-easing :period])}
                    :as           :json}))
                 {})}
   {::input    #{::backlight-easing}
    ::output   #{}
    ::doc      ""
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/get
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/easing/backlight"))
                   {:query-params {:key    (::api-key params)
                                   :step   (get-in params [::input ::backlight-easing :step])
                                   :period (get-in params [::input ::backlight-easing :period])}
                    :as           :json}))
                 {})}
   {::input    #{}
    ::output   #{::easing-config}
    ::doc      "Get the easing config from the dial"
    ::execute! (fn [params]
                 {::easing-config (-> (client/get
                                       (make-v0-dial-path-prefix
                                        (assoc params ::extended-path "/easing/get"))
                                       {:query-params {:key (::api-key params)}
                                        :as           :json})
                                      (die-if-status-bad!)
                                      (get-in [:body :data]))})}])

(defn execute!
  [{::keys [input outputs] :as params}]
  (let [input-set   (set (keys input))
        outputs-set (set outputs)
        api-paths   (filter (fn [x]
                              (or (seq (set/intersection input-set (::input x)))
                                  (seq (set/intersection outputs-set (::output x)))))
                            dial-api-paths)]
    (reduce (fn [acc {::keys [execute!]}]
              (merge acc (execute! params)))
            {}
            api-paths)))

(comment



  (for [value [8 57 33 86 99 1]]
    (do
      (execute! {::input    {::value value}
                 ::dial-uid "840033000650564139323920"
                 ::api-key  api-key})
      (Thread/sleep 1000)))


  (execute! {::input    {::value            40
                         ::name             "L-ref-1"
                         ::background-color [255 255 255 255]}
             ::dial-uid "840033000650564139323920"
             ::api-key  api-key})

  (execute! {::input    {::dial-easing {:step   1
                                        :period 2000}
                        }
             ::dial-uid "840033000650564139323920"
             ::api-key  api-key})

  (execute! {::inputs   {}
             ::outputs  [::status ::easing-config]
             ::dial-uid "840033000650564139323920"
             ::api-key  api-key})
)