(ns az-manifold.core
  (:require
   [clojure.pprint :as pp]
   [manifold.deferred :as md]
   [manifold.stream :as ms])
  (:gen-class))


(defn expensive!
  [arg]
  (let [now (java.util.Date.)]
    (Thread/sleep 2000)
    {arg now}))

(def max-concurrency 3)

(defn process
  [the-nums out]
  (prn "batch!")
  (md/chain
   (apply md/zip
          (map
           (fn [arg]
             (prn arg)
             (md/future (expensive! arg)))
           the-nums))
   #(ms/put-all! out %)))

(defn -main
  [& args]
  (let [output (ms/stream)
        vals (concat (take 25 (range 1 100 1)) nil)
        batches (->>
                 (ms/->source vals)
                 (ms/batch max-concurrency))
        _ (ms/connect-via batches #(process % output) output)
        finished (doall (ms/stream->seq output))]
    (pp/pprint finished)
    (prn "connect-via DONE")))
