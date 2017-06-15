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
     (let [more? (= (count the-nums) batch-size)
           ret (ms/put-all! out vals)]
       (when (not more?)
         (ms/close! out))
       ret))))

(defn using-consume-async
  []
  (let [output (ms/stream)
        vals (concat (take 25 (range 1 100 1)) nil)
        batches (->>
                 (ms/->source vals)
                 (ms/batch max-concurrency))
        _ (ms/consume-async #(process % output max-concurrency) batches)
        finished (vec (ms/stream->seq output))]
    (pp/pprint finished)
    (prn "consume async DONE")))

(defn using-connect-via
  []
  (let [output (ms/stream)
        vals (concat (take 25 (range 1 100 1)) nil)
        batches (->>
                 (ms/->source vals)
                 (ms/batch max-concurrency))
        p2 (fn [val out]
             (prn "batch!")
             (md/chain
              (apply md/zip
                     (map
                      (fn [arg]
                        (prn arg)
                        (md/future (expensive! arg)))
                      val))
              #(ms/put-all! out %)))
        _ (ms/connect-via batches #(p2 % output) output)
        finished (doall (ms/stream->seq output))]
    (pp/pprint finished)
    (prn "connect-via DONE")))

(defn -main
  [& args]
  (using-connect-via))
