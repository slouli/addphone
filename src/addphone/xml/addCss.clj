(ns addphone.xml.addCss
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn addCss
  [newCssName & ptList]
  {:name "addCss"                     
   :xml (xml/element :ns:addCss {:sequence "?"}
          (xml/element :css {}
            (xml/element :name {}  newCssName)
            (xml/element :description {} newCssName)
            (xml/element :members {}
              (map-indexed #(xml/element :member {}
                              (xml/element :routePartitionName {} %2)
                              (xml/element :index {} (+ %1 1))) ptList))))})
