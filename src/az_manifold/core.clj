(ns az-manifold.core
  (:require
   [clojure.pprint :as pp]
   [amazonica.aws.s3 :as s3]
   [manifold.deferred :as md]
   [manifold.stream :as ms]
   [environ.core :refer [env]])
  (:gen-class))


(defn expensive!
  [arg]
  (let [now (java.util.Date.)]
    (Thread/sleep 2000)
    {arg now}))

(def output (ms/stream))

(defn process
  [the-nums out]
  ;; fire off x deferreds
  (prn the-nums)
  (md/chain
   (apply md/zip
          (map
           (fn [arg]
             (prn arg)
             (md/future (expensive! arg)))
           the-nums))
   (fn [vals]
     (ms/put-all! out vals))))

(defn staying-alive
  "Create a background thread to keep the entire process running."
  []
  (.start (Thread. (fn [] (.join (Thread/currentThread))) "staying alive")))

(def max-concurrency 20)

(defn -main
  [& args]
  (staying-alive)
  (ms/consume-async #(process % output) (->>
                                         (ms/->source (take 60 (range)))
                                         (ms/batch max-concurrency)))
  (ms/consume-async #(pp/pprint %) output))
  #_(pp/pprint (ms/stream->seq output))
