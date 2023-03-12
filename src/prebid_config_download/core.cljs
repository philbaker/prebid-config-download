(ns prebid-config-download.core
  (:require ["playwright$default" :refer [chromium]]
            ["fs" :as fs]
            [clojure.test :as t :refer [deftest is async]]
            [borkdude.deflet :refer [defletp defp]]
            [promesa.core :as p]))

(def prebid-version "7.38.0")
(def prebid-adapters ["appnexus" "ablida"])

(defp browser (.launch chromium #js {:headless false}))
(defp context (.newContext browser))
(defp page (.newPage context))
(.goto page "https://docs.prebid.org/download.html")
(defp select (p/-> (.locator page "select#version_selector")
                   (.locator "option")
                   (.allTextContents)))
(defp select-version (p/-> (.locator page "select#version_selector")
                           (.selectOption prebid-version)))

(defp select-bidder
  (for [adapter prebid-adapters] 
    (p/-> (.locator page (str "input#" adapter "BidAdapter"))
          (.first)
          (.setChecked true))))

(-> (.waitForEvent page "download")
    (.then (fn [download] (.path download)))
    (.then (fn [x] (println (str (fs/readFileSync x))))))

(defp download-file
  (p/-> (.locator page "button.btn.btn-lg.btn-primary")
        (.first)
        (.click)))
