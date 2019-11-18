(ns addphone.xml.listLine
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn zipify
  [resp]
  (zip/xml-zip (xml/parse-str resp)))

(defn parseListLine
  [xmlResp]
  (let [zipper (zipify xmlResp)
        uuid (zip-xml/xml-> zipper :Envelope :Body :listLineResponse
                      :return :line (zip-xml/attr :uuid))
        patternList (zip-xml/xml-> zipper :Envelope :Body :listLineResponse
                      :return :line :pattern zip-xml/text)
        ptList (zip-xml/xml-> zipper :Envelope :Body :listLineResponse
                 :return :line :routePartitionName zip-xml/text)
        css (zip-xml/xml-> zipper :Envelope :Body :listLineResponse
                 :return :line :shareLineAppearanceCssName zip-xml/text)]
    (zipmap 
      (map #(keyword ((re-find #"\{(.*)\}" %) 1)) uuid) 
      (map list patternList ptList css))))

(defn listLine
  [& userXml]
  {:name "listLine"
   :xml (xml/element :ns:listLine {:sequence "?"}
          (xml/element :searchCriteria {}
            (xml/element :pattern {} "%"))
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :pattern {})
            (xml/element :description {})
            (xml/element :routePartitionName {})
            (xml/element :shareLineAppearanceCssName {})))})
