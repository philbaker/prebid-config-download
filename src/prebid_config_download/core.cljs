(ns prebid-config-download.core
  (:require ["playwright$default" :refer [chromium]]
            ["fs" :as fs]
            [clojure.edn :refer [read-string]]
            [borkdude.deflet :refer [defletp defp]]
            [promesa.core :as p]))

(def config (read-string (str (fs/readFileSync "config.edn"))))

(declare browser context page select select-version x download-file download-event)

(defn download []
  (defletp
    (defp browser (.launch chromium #js {:headless (:headless config)}))
    (defp context (.newContext browser))
    (defp page (.newPage context))
    (.goto page "https://docs.prebid.org/download.html")
    (defp select (p/-> (.locator page "select#version_selector")
                       (.locator "option")
                       (.allTextContents)))
    (defp select-version (p/-> (.locator page "select#version_selector")
                               (.selectOption (:prebid-version config))))
    (p/loop [x (dec (count (:prebid-adapters config)))]
      (when (> x -1)
        x
        (p/recur (- x 1)
                 (p/-> 
                   (.locator
                     (.locator page ".adapters .checkbox label" #js 
                               {:has (.locator page (str "text=" (get (:prebid-adapters config) x)))})
                     "input")
                   (.setChecked true)))))
    (p/do 
      (defp download-event
        (p/-> (.waitForEvent page "download")
              (p/then (fn [download] (.path download)))
              (p/then (fn [x] (fs/copyFileSync x (str (:file-prefix config) (:prebid-version config) ".js"))))
              (p/then (fn [] (.screenshot page #js {:path "screenshot.png", :fullPage true})))
              (p/then (fn [] (.close browser)))
              (p/then (fn [] (println "download complete")))))
      (defp download-file
        (p/-> (.locator page "button.btn.btn-lg.btn-primary")
              (.first)
              (.click))))))

(download)
