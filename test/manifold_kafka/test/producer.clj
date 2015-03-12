(ns manifold-kafka.test.producer
  (:require [manifold.stream :as s]
            [midje.sweet :refer :all]
            [clj-kafka.core :refer [with-resource to-clojure]]
            [clj-kafka.consumer.zk :as zk]
            [manifold-kafka.producer :refer [producer]]
            [manifold-kafka.test.utils :refer [with-test-broker]]
            [clj-kafka.consumer.seq :as zks]))

(def producer-config {"metadata.broker.list" "localhost:9999"
                      "serializer.class"     "kafka.serializer.DefaultEncoder"
                      "partitioner.class"    "kafka.producer.DefaultPartitioner"})

(def test-broker-config {:zookeeper-port 2182
                         :kafka-port     9999
                         :topic          "test"})

(def consumer-config {"zookeeper.connect"  "localhost:2182"
                      "group.id"           "clj-kafka.test.consumer"
                      "auto.offset.reset"  "smallest"
                      "auto.commit.enable" "false"})

(defn string-value
  [k]
  (fn [m]
    (String. (k m) "UTF-8")))

(defn test-message
  []
  (.getBytes "Hello, world"))

(defn send-and-receive
  [msg]
  (with-test-broker test-broker-config
                    (with-resource [c (zk/consumer consumer-config)]
                                   zk/shutdown
                                   (with-resource [p (producer producer-config "test")]
                                                  s/close!
                                                  (let [s (zk/stream c "test" 1)]
                                                    (s/put! p msg)
                                                    (first (zks/kafka-seq s)))))))

(fact "should produce simple message"
      (let [result (send-and-receive (test-message))]
        result => (contains {:topic     "test"
                             :offset    0
                             :partition 0})
        (-> result :value (String. "UTF-8")) => "Hello, world"))

(defn send-and-receive-json
  [msg]
  (with-test-broker test-broker-config
                    (with-resource [c (zk/consumer consumer-config)]
                                   zk/shutdown
                                   (with-resource [p (producer producer-config "test" :serializer :json)]
                                                  s/close!
                                                  (let [s (zk/stream c "test" 1)]
                                                    (s/put! p msg)
                                                    (first (zks/kafka-seq s)))))))

(fact "should produce json message"
      (let [result (send-and-receive-json {:yo "Hello, world"})]
        result => (contains {:topic     "test"
                             :offset    0
                             :partition 0})
        (-> result :value (String. "UTF-8")) => "{\"yo\":\"Hello, world\"}"))
