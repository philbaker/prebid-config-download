(ns prebid-config-download.core
  (:require ["playwright$default" :refer [chromium]]
            ["fs" :as fs]
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

(defn set-checkboxes [{:keys [page selector input-selector items checked]}]
  (p/loop [x (dec (count items))]
    (when (>= x 0)
      (p/recur (dec x)
               (p/-> (.locator (.locator page selector
                                         #js {:has (.locator page (str "text=" (get items x)))})
                               (str "input" input-selector))
                     (.first)
                     (.setChecked checked))))))

(defn read-edn-file [file-path]
  (let [content (fs/readFileSync file-path "utf8")]
    (if (empty? content)
      []
      (read-string content))))

(defn append-hash-map! [file-path config datetime]
  (let [current-data (read-edn-file file-path)
        new-data (assoc (into {} (dissoc config :headless :file-prefix)) :created-at datetime)
        updated-data (conj current-data new-data)]
    (fs/writeFileSync file-path (pr-str updated-data))))

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
    (set-checkboxes {:page page
                     :selector ".adapters .checkbox label"
                     :input-selector ""
                     :items (:prebid-adapters config)
                     :checked true})

    ; Check matched analytics checkboxes
    (set-checkboxes {:page page
                     :selector ".checkbox label"
                     :input-selector ".analytics-check-box"
                     :items (:analytics-adapters config)
                     :checked true})

    ; Uncheck matched recommended module checkboxes
    (set-checkboxes {:page page
                     :selector ".checkbox label"
                     :input-selector ""
                     :items (:recommended-modules-disable config)
                     :checked false})

    ; Check matched general module checkboxes
    (set-checkboxes {:page page
                     :selector ".checkbox label"
                     :input-selector ""
                     :items (:general-modules config)
                     :checked true})

    ; Check matched vendor specific module checkboxes
    (set-checkboxes {:page page
                     :selector ".checkbox label"
                     :input-selector ""
                     :items (:vendor-specific-modules config)
                     :checked true})

    ; Check matched user id modules
    (set-checkboxes {:page page
                     :selector ".checkbox label"
                     :input-selector ""
                     :items (:user-id-modules config)
                     :checked true})

    ; Download prebid file, take screenshot and append config info to logs
    (p/do
      (defp download-event
        (p/-> (.waitForEvent page "download")
              (p/then (fn [download] (.path download)))
              (p/then (fn [x] (fs/copyFileSync x (str "output/" (:file-prefix config) (:prebid-version config) ".js"))))
              (p/then (fn [] (.screenshot page #js {:path (str "output/" datetime "-prebid-config.png") :fullPage true})))
              (p/then (fn [] (append-hash-map! "output/log.edn" config datetime)))
              (p/then (fn [] (fs/writeFileSync "output/log.json" (.stringify js/JSON (clj->js (read-string (str (fs/readFileSync "output/log.edn"))))))))
              (p/then (fn [] (.close browser)))
              (p/then (fn [] (println "download complete - files saved to /output")))))
      (defp download-file
        (p/-> (.locator page "button.btn.btn-lg.btn-primary")
              (.first)
              (.click))))))

(download)
