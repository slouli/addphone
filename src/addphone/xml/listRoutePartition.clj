(ns addphone.xml.listRoutePartition
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))


(defn listRoutePartition
  "Eventually should make parameters a map that takes named args"
  [searchCriteria]
  {:name "listRoutePartition"
   :xml (xml/element :ns:listRoutePartition {:sequence "?"}
          (xml/element :searchCriteria {}
            (xml/element :name {} searchCriteria))
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :name {})))})
