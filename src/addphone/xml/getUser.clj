(ns addphone.xml.getUser
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn zipify
  [resp]
  (zip/xml-zip (xml/parse-str resp)))

(defn parseGetUser
  [xmlResp]
  (let [zipper (zipify xmlResp)
        userid (zip-xml/xml1-> zipper :Envelope :Body :getUserResponse
                 :return :user :userid zip-xml/text)
        devices (zip-xml/xml-> zipper :Envelope :Body :getUserResponse
                  :return :user :associatedDevices :device zip-xml/text)]
    [userid devices]))
    

(defn getUser
  [userid]
  {:name "getUser"
   :xml (xml/element :ns:getUser {:sequence "?"}
          (xml/element :userid {} userid)
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :userid {})
            (xml/element :associatedDevices {}
              (xml/element :device {}))))})
