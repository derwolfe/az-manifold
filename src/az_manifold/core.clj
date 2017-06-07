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
  (println "werk werk werk" arg)
  (swap! the-state conj (java.util.Date.))
  arg)

;; setup a consumer
(defn build-ins
  []
  (take 50 (range)))

;; setup a stream
(def inputs (ms/stream 5))
(def finished (ms/stream 5))

;; setup a worker
(defn process1
  [the-num out-st]
  (prn the-num)
  (ms/put!
   out-st
   (md/future (expensive! the-num))))

(defn process2
  [val]
  (println val)
  true)

(defn staying-alive
  "Create a background thread to keep the entire process running."
  []
  (.start (Thread. (fn [] (.join (Thread/currentThread))) "staying alive")))

(defn -main
  [& args]
  (staying-alive)
  ;; consume-async is key as it understands how to wait for deferreds to complete.
  ;; the backgroud thread allows us to let the application continue while the foreground thread
  ;; is simply waiting for background jobs to complete. Not sure why, but it exits without this.
  (ms/consume-async #(process1 % finished) inputs)
  (ms/consume-async #(process2 %) finished)
  (md/chain
   (ms/put-all! inputs (build-ins))
   (fn [result]
     ;; if true, all puts are finished, if false, a failure, otherwise we are still moving through
     ;; the inputs. Shut the system down once we have the all done true value.
     (when (true? result)
       ;; the batching should be relatively evident based on the timestamp groupings being interleaved
       (pp/pprint @the-state)
       (System/exit 0)))))
