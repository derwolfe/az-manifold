(ns az-manifold.core
  (:require
   [clojure.pprint :as pp]
   [amazonica.aws.s3 :as s3]
   [manifold.deferred :as md]
   [manifold.stream :as ms]
   [environ.core :refer [env]])
  (:gen-class))

(def the-state (atom []))

(defn expensive!
  [arg]
  (Thread/sleep 2000)
  (swap! the-state conj (java.util.Date.))
  arg)

(defn process
  [the-nums]
  ;; fire off x deferreds
  (prn the-nums)
  (apply md/zip
         (map
          (fn [arg]
            (prn arg)
            (md/future (expensive! arg)))
          the-nums)))

(defn staying-alive
  "Create a background thread to keep the entire process running."
  []
  (.start (Thread. (fn [] (.join (Thread/currentThread))) "staying alive")))

(def max-concurrency 20)

(defn -main
  [& args]
  (staying-alive)
  (ms/consume-async #(process %) (->>
                                  (ms/->source (take 50 (range)))
                                  (ms/batch max-concurrency))))
