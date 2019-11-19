(ns addphone.xml.getCss
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))


(defn getCss
  [name]
  {:name "getCss"
   :xml (xml/element :ns:getCss {:sequence "?"}
          (xml/element :name {} name)
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :name {})
            (xml/element :members {}
              (xml/element :member {}
                (xml/element :routePartitionName {})
                (xml/element :index {})))))})
