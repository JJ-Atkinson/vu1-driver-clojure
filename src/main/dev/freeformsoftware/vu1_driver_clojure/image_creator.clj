(ns dev.freeformsoftware.vu1-driver-clojure.image-creator
  (:require
   [clojure.java.io :as jio])
  (:import
   (java.awt.image BufferedImage)
   (java.awt Color Font Graphics)
   (java.awt.font TextAttribute)
   (javax.imageio ImageIO)
   (java.nio.file Files CopyOption StandardCopyOption)))


(def fan-speed
  (ImageIO/read
   (jio/file "/home/jarrett/Downloads/image_pack/fan-speed.png")))

(def font-sprite-lookup
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789:.")

(defn load-font
  [file size kerning-modifier]
  (-> (Font/createFont Font/TRUETYPE_FONT file)
      (.deriveFont Font/PLAIN (float size))
      (.deriveFont {TextAttribute/TRACKING (float kerning-modifier)})))

(defn render-string!
  "Stateful - modifies the font of the graphics context and renders to the graphics context!
   
   position-mode #{::...top-right ::...top-left ::...baseline-right ::...baseline-left ::...top-center ::...baseline-center}, default baseline-left"
  [^Graphics graphics ^String string x y & [{::keys [font position-mode]}]]
  (when font (.setFont graphics font))
  (let [font-metrics  (.getFontMetrics graphics)
        height        (.getMaxAscent font-metrics)
        width         (.stringWidth font-metrics string)
        height-offset (case position-mode
                        (::position-mode.top-right
                         ::position-mode.top-left
                         ::position-mode.top-center)
                        height
                        0)
        width-offset  (case position-mode
                        (::position-mode.baseline-right
                         ::position-mode.top-right)
                        (- width)

                        (::position-mode.baseline-center
                         ::position-mode.top-center)
                        (- (/ width 2))

                        0)]
    (.drawString graphics
                 string
                 (float (+ x width-offset))
                 (float (+ y height-offset)))))

(comment
  (let [raw-image         (ImageIO/read
                           (jio/file (jio/resource "blank-images/blank-dial.png")))
        ;; small-font        (load-font (jio/file (jio/resource "fonts/ninepin/ninepin.ttf")) 8 0.15)
        small-font        (load-font (jio/file (jio/resource "fonts/ninepin/ninepin.ttf")) 16 0.15)
        large-font        (load-font (jio/file (jio/resource "fonts/nostalgia/16-bit-7x9-nostalgia.ttf"))
                                     16
                                     0.04)
        really-large-font (load-font (jio/file (jio/resource "fonts/nostalgia/16-bit-7x9-nostalgia.ttf"))
                                     48
                                     0.04)
       ]
    (doto (.getGraphics raw-image)
      (.setColor Color/BLACK)
      (render-string! "0%"
                      10
                      124 {::font          small-font
                           ::position-mode ::position-mode.top-left})
      (render-string! "100%"
                      195
                      124 {::font          small-font
                           ::position-mode ::position-mode.top-right})
      (render-string! "CPU" 100
                      50
                      {::font          really-large-font
                       ::position-mode ::position-mode.baseline-center})
    )

    (ImageIO/write raw-image "png" (jio/file "test.png"))
    ;; moving, rather than directly overwriting, saves me the trouble of re-opening the image in vscode each time
    ;; the file changes. No need for a jframe - just let vscode auto-reload the file
    (Files/move (.toPath (jio/file "test.png"))
                (.toPath (jio/file "test2.png"))
                (doto (make-array CopyOption 1)
                  (aset 0 StandardCopyOption/REPLACE_EXISTING)))
  )
)