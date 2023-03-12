(ns prebid-config-download.core
  (:require ["playwright$default" :refer [chromium]]
            ["fs" :as fs]
            [borkdude.deflet :refer [defletp defp]]
            [promesa.core :as p]))

(declare browser context page select select-version x download-file)
(def prebid-version "7.38.0")
(def prebid-adapters ["adWMG" "aja" "appnexus" "ablida"])

(defn download []
  (defletp
    (defp browser (.launch chromium #js {:headless false}))
    (defp context (.newContext browser))
    (defp page (.newPage context))
    (.goto page "https://docs.prebid.org/download.html")
    (defp select (p/-> (.locator page "select#version_selector")
                       (.locator "option")
                       (.allTextContents)))
    (defp select-version (p/-> (.locator page "select#version_selector")
                               (.selectOption prebid-version)))
    (p/loop [x (dec (count prebid-adapters))]
      (when (> x -1)
        x
        (p/recur (- x 1)
                 (p/-> 
                   (.locator
                     (.locator page ".adapters label" #js 
                               {:has (.locator page (str "text=" (get prebid-adapters x)))})
                     "input")
                   (.setChecked true)))))

    (do
      (-> (.waitForEvent page "download")
          (.then (fn [download] (.path download)))
          (.then (fn [x] (println (str (fs/readFileSync x))))))

      (defp download-file
        (p/-> (.locator page "button.btn.btn-lg.btn-primary")
              (.first)
              (.click))))))

(comment
  (download)
  )
