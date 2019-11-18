(ns addphone.xml.listCss
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn zipify
  [resp]
  (zip/xml-zip (xml/parse-str resp)))

(defn parseListCss
  [xmlResp]
  (let [zipper (zipify xmlResp)
        cssNameList (zip-xml/xml-> zipper :Envelope :Body :listCssResponse
                      :return :css :name zip-xml/text)]
    cssNameList))

(defn listCss
  "Eventually should make parameters a map that takes named args"
  [searchCriteria]
  {:name "listCss"
   :xml (xml/element :ns:listCss {:sequence "?"}
          (xml/element :searchCriteria {}
            (xml/element :name {} searchCriteria))
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :name {})
            (xml/element :partitionUsage {})))})
