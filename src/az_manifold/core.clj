(ns az-manifold.core
  (:require
   [amazonica.aws.s3 :as s3]
   [manifold.deferred :as md]
   [environ.core :refer [env]])
  (:gen-class))

(defn aws-creds
  []
  {:access-key (env :access-key)
   :secret-key (env :secret-key)})

(defn list-bucket
  [creds]
  (md/chain
   (md/future (s3/list-buckets creds))
   (fn [ret]
     (prn "number of buckets" (count ret)))))

(defn -main
  [& args]
  (let [creds (aws-creds)]
    ;; (prn creds)
    (doseq [x (range 6)]
      @(list-bucket creds))))
