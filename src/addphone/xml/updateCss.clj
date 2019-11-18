(ns addphone.xml.updateCss
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn updateCss
  [name & partitions]
  {:name "updateCss"                     
   :xml (xml/element :ns:updateCss {:sequence "?"}
          (xml/element :name {} name)
          (xml/element :members {}
            (map-indexed #(xml/element :member {}
                            (xml/element :routePartitionName {} %2)
                            (xml/element :index {} (+ %1 1)))
              partitions)))})
