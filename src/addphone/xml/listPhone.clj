(ns addphone.xml.listPhone
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))

(defn listPhone
  [& userXml]
  {:name "listPhone"
   :xml (xml/element :ns:listPhone {}
          (xml/element :searchCriteria {}
            (xml/element :name {} "CSF%"))
          (xml/element :returnedTags {}
            (xml/element :name {})))})


(defn parseListPhoneSql
  [xmlResp]
  (let [zipper (zipify xmlResp)
        userList (zip-xml/xml-> zipper :Envelope :Body :listPhoneResponse
                   :return :phone :name zip-xml/text)]
    (into #{} (map #(keyword (str/lower-case %)) (map subs userList (repeat 3))))))
