(defproject manifold-kafka "0.0.1-SNAPSHOT"
  :min-lein-version "2.0.0"
  :url "https://github.com/shmish111/manifold-kafka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-kafka/clj-kafka "0.2.8-0.8.1.1"]
                 [zookeeper-clj "0.9.3"]
                 [manifold "0.1.0-beta3"]]
  :plugins [[lein-expectations "0.0.8"]]
  :profiles {:dev {:dependencies [[commons-io/commons-io "2.4"]
                                  [expectations "1.4.45"]
                                  [org.slf4j/slf4j-simple "1.6.4"]]}}
  :description "Manifold producer and consumer for clj-kafka")
