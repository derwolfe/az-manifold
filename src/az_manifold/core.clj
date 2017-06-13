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
  [the-nums out batch-size]
  ;; fire off max-concurrency deferreds
  (prn "batch!")
  (md/chain
   (apply md/zip
          (map
           (fn [arg]
             (prn arg)
             (md/future (expensive! arg)))
           the-nums))
   (fn [vals]
     (let [more? (if (= (count the-nums) batch-size)
                   true
                   false)
           ret @(ms/put-all! out vals)]
       (when (not more?)
         (ms/close! out))
       ret))))

(defn -main
  [& args]
  (let [output (ms/stream)
        ;; how to close this?
        ;; why isn't consume-async
        vals (concat (take 25 (range 1 100 1)) nil)
        batches (->>
                 (ms/->source vals)
                 (ms/batch max-concurrency))
        _ (ms/consume-async #(process % output max-concurrency) batches)
        finished (vec (ms/stream->seq output))]
    (pp/pprint finished)
    (prn "DONE")))
