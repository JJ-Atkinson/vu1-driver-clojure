(ns dev.freeformsoftware.vu1-driver-clojure.main
  (:require
   [clj-http.client :as client]
   [clojure.set :as set]
   [clojure.java.io :as jio]
   [clojure.pprint :as pprint]
   [malli.core :as mc]
   [malli.util :as mu]
   [taoensso.encore :as enc])
  (:import
   (java.io File)))

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
    ::spec     [:map
                [::value
                 [:and int?
                  [:>= 0]
                  [:<= 100]]]]
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
    ::doc      "Query for the status from the driver"
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
    ::doc      "A max thirty character string denoting the UI name for a dial"
    ::spec     [:map
                [::name
                 [:and string?
                  [:fn #(< 0 (count %) 30)]]]]
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
    ::spec     [:map
                [::background-color
                 [:vector [:and int? [:>= 0] [:< 256]]]]]
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
   {::input #{::dial-easing}
    ::output #{}
    ::doc
    "Configure easing for the dial. Step is the absolute amount that 
           can be moved per period. Peroid is the number of MS between update events."
    ::spec [:map
            [::dial-easing
             [:map
              [:step {:optional true} int?]
              [:period {:optional true} int?]]]]
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/get
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/easing/dial"))
                   {:query-params (enc/assoc-some {:key (::api-key params)}
                                                  :step   (get-in params [::input ::dial-easing :step])
                                                  :period (get-in params [::input ::dial-easing :period]))
                    :as           :json}))
                 {})}
   {::input #{::backlight-easing}
    ::output #{}
    ::doc
    "Configure easing for the backlight color. Step is the absolute amount that 
           can be moved per period. Peroid is the number of MS between update events."
    ::spec [:map
            [::backlight-easing
             [:map
              [:step {:optional true} int?]
              [:period {:optional true} int?]]]]
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/get
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/easing/backlight"))
                   {:query-params (enc/assoc-some {:key (::api-key params)}
                                                  :step   (get-in params [::input ::backlight-easing :step])
                                                  :period (get-in params [::input ::backlight-easing :period]))
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
                                      (get-in [:body :data]))})}
   {::input    #{::background-image}
    ::output   #{}
    ::doc      "A jio/file (png preferably) of resolution 200x144"
    ::spec     [:map
                [::background-image [:fn #(instance? File %)]]]
    ::execute! (fn [params]
                 (die-if-status-bad!
                  (client/post
                   (make-v0-dial-path-prefix
                    (assoc params ::extended-path "/image/set"))
                   {:query-params {:key   api-key
                                   :force true}
                    :multipart    [{:name    "imgfile"
                                    :content (get-in params [::input ::background-image])}]
                    :as           :json}))
                 {})}])

(defn execute!
  [{::keys [input outputs] :as params}]
  (let [input-set   (set (keys input))
        outputs-set (set outputs)
        api-paths   (filter (fn [x]
                              (or (seq (set/intersection input-set (::input x)))
                                  (seq (set/intersection outputs-set (::output x)))))
                            dial-api-paths)
        spec        [:map
                     [::input
                      (reduce mu/merge
                              [:map]
                              (keep ::spec api-paths))]]]
    (println spec)
    (if (mc/validate spec params)
      (reduce (fn [acc {::keys [execute!]}]
                (merge acc (execute! params)))
              {}
              api-paths)
      {::validation-error (mc/explain spec params)
       ::spec             spec})))


(comment

  (for [value [8 57 33 86 99 1]]
    (do
      (execute! {::input    {::value value}
                 ::dial-uid "840033000650564139323920"
                 ::api-key  api-key})
      (Thread/sleep 1000)))


  (execute! {::input    {::value            80
                         ;;  ::name             "L-ref-1"
                         ::background-color [255 182 0 0]}
             ::dial-uid "490026000650564139323920"
             ::api-key  api-key})

  (for [id [;"840033000650564139323920"
            "400028000650564139323920"
            #_#_#_"490026000650564139323920"
                "400028000650564139323920"
              "8C002B000650564139323920"]]
    (execute! {::input    {::value            80
                           ;;  ::name             "L-ref-1"
                           ::background-color [256 182 0 0]}
               ::dial-uid id
               ::api-key  api-key}))


  (execute! {::input    {::background-image (jio/file "test2.png")}
             ::dial-uid "400028000650564139323920"
             ::api-key  api-key})

  (execute! {::input    {::dial-easing {:step   10
                                        :period 7000}
                        }
             ::dial-uid "840033000650564139323920"
             ::api-key  api-key})

  (execute! {::inputs   {}
             ::outputs  [::status ::easing-config]
             ::dial-uid "840033000650564139323920"
             ::api-key  api-key})


  (println
   (clojure.string/join
    "\n\n"
    (map (fn [{::keys [input output doc spec]}]
           (str "### "
                (if (seq input) "Input: " "Output: ")
                "`"
                (pr-str (or (when (seq input) input) output))
                "`\n\n"
                (when spec
                  (str "\n\n```\n"
                       (with-out-str (pprint/pprint spec))
                       "```"))
                doc))
         dial-api-paths))))

(execute! {::input    {::background-image (jio/file "test2.png")
                       ::value 30 
                       ::background-color [100 0 100 0]}
           ::dial-uid "400028000650564139323920"
           ::uri      "localhost"
           ::port     5340
           ::api-key  api-key})