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

(def max-concurrency 3)

(defn process
  [the-nums out]
  ;; fire off max-concurrency deferreds
  (md/chain
   (apply md/zip
          (map
           (fn [arg]
             ;;(prn arg)
             (md/future (expensive! arg)))
           the-nums))
   (fn [vals]
     (println "got:" vals)
     (ms/put-all! out vals))))

(defn -main
  [& args]
  (let [output (ms/stream)
        close (ms/consume-async #(process % output) (->>
                                                     (ms/->source (take 25 (range 1 100 1)))
                                                     (ms/batch max-concurrency)))
        finished (doall (ms/stream->seq output))]
    (pp/pprint finished)))
