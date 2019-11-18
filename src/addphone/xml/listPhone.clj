(ns addphone.xml.listPhone
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn listPhone
  [& userXml]
  {:name "listPhone"
   :xml (xml/element :ns:listPhone {}
          (xml/element :searchCriteria {}
            (xml/element :name {} "CSF%"))
          (xml/element :returnedTags {}
            (xml/element :name {})))})


(defn zipify
  [resp]
  (zip/xml-zip (xml/parse-str resp)))

(defn parseListPhoneSql
  [xmlResp]
  (let [zipper (zipify xmlResp)
        userList (zip-xml/xml-> zipper :Envelope :Body :listPhoneResponse
                   :return :phone :name zip-xml/text)]
    (into #{} (map #(keyword (str/lower-case %)) (map subs userList (repeat 3))))))
