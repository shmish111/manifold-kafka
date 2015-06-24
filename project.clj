(defproject manifold-kafka "0.0.1"
  :min-lein-version "2.0.0"
  :url "https://github.com/shmish111/manifold-kafka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-kafka/clj-kafka "0.3.1"]
                 [manifold "0.1.0"]]
  :profiles {:dev {:dependencies [[commons-io/commons-io "2.4"]
                                  [midje "1.6.3"]
                                  [org.slf4j/slf4j-simple "1.7.12"]]}}
  :description "Manifold producer and consumer for clj-kafka"
            :main manifold-kafka.producer)
