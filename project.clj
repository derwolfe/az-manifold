(defproject az-manifold "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [amazonica "0.3.86"]
                 [manifold "0.1.7-alpha5"]
                 [environ "1.1.0"]]
  :plugins [[lein-environ "1.0.2"]]
  :main ^:skip-aot az-manifold.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})