### Input: `#{:dev.freeformsoftware.vu1-driver-clojure.main/value}

```
[:map
 [:dev.freeformsoftware.vu1-driver-clojure.main/value
  [:and #function[clojure.core/int?] [:>= 0] [:<= 100]]]]
````

A 0-100 value

### Output: `#{:dev.freeformsoftware.vu1-driver-clojure.main/status}`



### Input: `#{:dev.freeformsoftware.vu1-driver-clojure.main/name}

```
[:map
 [:dev.freeformsoftware.vu1-driver-clojure.main/name
  [:and
   #function[clojure.core/string?--5475]
   [:fn
    #function[dev.freeformsoftware.vu1-driver-clojure.main/fn--22890]]]]]
````

A thirty character string denoting the UI name for a dial

### Input: `#{:dev.freeformsoftware.vu1-driver-clojure.main/background-color}

```
[:map
 [:dev.freeformsoftware.vu1-driver-clojure.main/background-color
  [:vector [:and #function[clojure.core/int?] [:>= 0] [:< 256]]]]]
````

[r-0-255 g-0-255 b-0-255 w?]

### Input: `#{:dev.freeformsoftware.vu1-driver-clojure.main/dial-easing}

```
[:map
 [:dev.freeformsoftware.vu1-driver-clojure.main/dial-easing
  [:map
   [:step {:optional true} #function[clojure.core/int?]]
   [:period {:optional true} #function[clojure.core/int?]]]]]
````

Configure easing for the dial. Step is the absolute amount that 
           can be moved per period. Peroid is the number of MS between update events.

### Input: `#{:dev.freeformsoftware.vu1-driver-clojure.main/backlight-easing}

```
[:map
 [:dev.freeformsoftware.vu1-driver-clojure.main/backlight-easing
  [:map
   [:step {:optional true} #function[clojure.core/int?]]
   [:period {:optional true} #function[clojure.core/int?]]]]]
````

Configure easing for the backlight color. Step is the absolute amount that 
           can be moved per period. Peroid is the number of MS between update events.

### Output: `#{:dev.freeformsoftware.vu1-driver-clojure.main/easing-config}`

Get the easing config from the dial

### Input: `#{:dev.freeformsoftware.vu1-driver-clojure.main/background-image}

```
[:map
 [:dev.freeformsoftware.vu1-driver-clojure.main/background-image
  [:fn file?]]]
````

A jio/file (png preferably) of resolution 200x144