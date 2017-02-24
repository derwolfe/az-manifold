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
   count))

(defn -main
  [& args]
  (let [creds (aws-creds)
        grab (fn [_] (list-bucket creds))]
    @(md/chain
      (apply md/zip (map grab (range 10)))
      (fn [all-of-them]
        (prn all-of-them)))))
