(ns addphone.xml.line
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getLine
  [linePattern routePartitionName]
  {:name "getLine"
   :xml (xml/element :ns:getLine {:sequence "?"}
               (xml/element :pattern {} linePattern)
               (xml/element :routePartitionName {} routePartitionName))})