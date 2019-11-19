(ns addphone.xml.listDeviceProfile
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))


(defn parseListDeviceProfile
  [xmlResp]
  (let [zipper (zipify xmlResp)
        profileList (zip-xml/xml-> zipper :Envelope :Body :listDeviceProfileResponse
                      :return :deviceProfile :name zip-xml/text)]
    (into #{} (map keyword profileList))))

(defn listDeviceProfile
  [& userXml]
  {:name "listDeviceProfile"
   :xml (xml/element :ns:listDeviceProfile {:sequence "?"}
          (xml/element :searchCriteria {}
            (xml/element :name {} "%"))
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :name {})))})
