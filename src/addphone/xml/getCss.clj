(ns addphone.xml.getCss
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn zipify
  [resp]
  (zip/xml-zip (xml/parse-str resp)))

(defn parseGetCss
  [xmlResp]
  (let [zipper (zipify xmlResp)
        cssName (zip-xml/xml1-> zipper :Envelope :Body :getCssResponse
                  :return :css :name zip-xml/text)
        ptList (zip-xml/xml-> zipper :Envelope :Body :getCssResponse
                 :return :css :members :member :routePartitionName zip-xml/text)
        ptIdxList (zip-xml/xml-> zipper :Envelope :Body :getCssResponse
                    :return :css :members :member :index zip-xml/text)]
    (conj (map second (sort-by #(Integer/parseInt (first %)) (map list ptIdxList ptList))) cssName)))

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
