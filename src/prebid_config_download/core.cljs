(ns prebid-config-download.core
  (:require ["playwright$default" :refer [chromium]]
            ["fs" :as fs]
            [clojure.string :as str]
            [clojure.edn :refer [read-string]]
            [borkdude.deflet :refer [defletp defp]]
            [promesa.core :as p]))

(when-not (fs/existsSync "output")
  (fs/mkdirSync "output"))

(when-not (fs/existsSync "output/log.edn") 
  (fs/writeFileSync "output/log.edn" ""))

; Set up basic config if one doesn't exist
(when-not (fs/existsSync "config.edn") 
  (fs/writeFileSync "config.edn" "{:prebid-version \"7.38.0\" :prebid-adapters [\"adWMG\" \"aja\" \"ablida\"] :file-prefix \"prebid-\" :headless false}"))

(def config (read-string (str (fs/readFileSync "config.edn"))))
(def datetime (.toISOString (js/Date.)))
(declare browser context page select-version x download-file download-event)

(defn download []
  (defletp
    ; Browser setup
    (defp browser (.launch chromium #js {:headless (:headless config)}))
    (defp context (.newContext browser))
    (defp page (.newPage context))
    (.goto page "https://docs.prebid.org/download.html")

    (defp select-version (p/-> (.locator page "select#version_selector")
                               (.selectOption (:prebid-version config))))

    ; Check matched adapter checkboxes
    (p/loop [x (dec (count (:prebid-adapters config)))]
      (when (> x -1)
        x
        (p/recur (- x 1)
                 (p/-> 
                   (.locator
                     (.locator page ".adapters .checkbox label" #js 
                               {:has (.locator page (str "text=" (get (:prebid-adapters config) x)))})
                     "input")
                   (.first)
                   (.setChecked true)))))

    ; ; Check matched analytics checkboxes
    (p/loop [x (dec (count (:analytics-adapters config)))]
      (when (> x -1)
        x
        (p/recur (- x 1)
                 (p/-> 
                   (.locator
                     (.locator page ".checkbox label" #js 
                               {:has (.locator page (str "text=" (get (:analytics-adapters config) x)))})
                     "input")
                   (.first)
                   (.setChecked true)))))

    ; Uncheck matched recommended module checkboxes
    (p/loop [x (dec (count (:recommended-modules-disable config)))]
      (when (> x -1)
        x
        (p/recur (- x 1)
                 (p/-> 
                   (.locator
                     (.locator page ".checkbox label" #js 
                               {:has (.locator page (str "text=" (get (:recommended-modules-disable config) x)))})
                     "input")
                   (.first)
                   (.setChecked false)))))

    ; Check matched vendor specific module checkboxes
    (p/loop [x (dec (count (:vendor-specific-modules config)))]
      (when (> x -1)
        x
        (p/recur (- x 1)
                 (p/-> 
                   (.locator
                     (.locator page ".checkbox label" #js 
                               {:has (.locator page (str "text=" (get (:vendor-specific-modules config) x)))})
                     "input")
                   (.first)
                   (.setChecked true)))))

    ; Check matched user id modules
    (p/loop [x (dec (count (:user-id-modules config)))]
      (when (> x -1)
        x
        (p/recur (- x 1)
                 (p/-> 
                   (.locator
                     (.locator page ".checkbox label" #js 
                               {:has (.locator page (str "text=" (get (:user-id-modules config) x)))})
                     "input")
                   (.first)
                   (.setChecked true)))))

    ; Download prebid file, take screenshot and append config info to logs
    (p/do 
      (defp download-event
        (p/-> (.waitForEvent page "download")
              (p/then (fn [download] (.path download)))
              (p/then (fn [x] (fs/copyFileSync x (str "output/" (:file-prefix config) (:prebid-version config) ".js"))))
              (p/then (fn [] (.screenshot page #js {:path (str "output/" datetime "-prebid-config.png") :fullPage true})))
              (p/then (fn [] (fs/appendFileSync "output/log.edn" (str (assoc (into {} (dissoc config :headless :file-prefix)) :created-at datetime) "\n"))))
              (p/then (fn [] (fs/writeFileSync "output/log.json" (.stringify js/JSON (clj->js (read-string (str (fs/readFileSync "output/log.edn"))))))))
              (p/then (fn [] (.close browser)))
              (p/then (fn [] (println "download complete - files saved to /output")))))
      (defp download-file
        (p/-> (.locator page "button.btn.btn-lg.btn-primary")
              (.first)
              (.click))))))

(download)
