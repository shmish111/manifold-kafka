# manifold-kafka

[https://github.com/ztellman/manifold](Manifold) producers and consumers for [https://github.com/pingles/clj-kafka](clj-kafka)

Current build status: [![Build Status](https://travis-ci.org/shmish111/manifold-kafka.png)](https://travis-ci.org/shmish111/manifold-kafka)

The idea of manifold-kafka is to be able to use kafka as if it were a simple manifold stream.

## Usage

[![Clojars Project](http://clojars.org/manifold-kafka/latest-version.svg)](http://clojars.org/manifold-kafka)

### Producer

```clj
(require '[manifold-kafka.producer :refer [producer]]
         '[manifold.stream :refer [put! close!]])

(def config {"metadata.broker.list" "localhost:9999"
             "serializer.class" "kafka.serializer.DefaultEncoder"
             "partitioner.class" "kafka.producer.DefaultPartitioner"})

(def p (producer config "test"))

;; a simple message
(put! p (.getBytes "this is my message"))

;; a keyed message
(put! p {:key "msg-key" :val (.getBytes "this is my message")})

(close! p)
```

### Consumer

```clj
(require '[manifold-kafka.consumer :refer [input-stream]]
         '[manifold.stream :refer [take! close!]])

(def config {"zookeeper.connect" "localhost:2182"
             "group.id" "clj-kafka.consumer"
             "auto.offset.reset" "smallest"
             "auto.commit.enable" "false"})

(def c (input-stream consumer-config "test"))

@(take! c)

(close! c)
```