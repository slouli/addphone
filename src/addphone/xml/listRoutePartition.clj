(ns addphone.xml.listRoutePartition
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn zipify
  [resp]
  (zip/xml-zip (xml/parse-str resp)))

(defn parseListRoutePartition
  [xmlResp]
  (let [zipper (zipify xmlResp)
        ptNameList (zip-xml/xml-> zipper :Envelope :Body :listRoutePartitionResponse
                     :return :routePartition :name zip-xml/text)]
    ptNameList))

(defn listRoutePartition
  "Eventually should make parameters a map that takes named args"
  [& searchCriteria]
  {:name "listRoutePartition"
   :xml (xml/element :ns:listRoutePartition {:sequence "?"}
          (xml/element :searchCriteria {}
            (xml/element :name {} "PT-%-Dev"))
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :name {})))})
