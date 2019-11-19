(ns addphone.xml.parseAxlResponse
  (:require [clojure.data.zip.xml :as zip-xml]
            [clojure.data.xml :as xml]
            [addphone.utilities.zip :refer [zipify]]))

(defn exists?
  [resp]
  (let [zipper (zipify resp)
        axlCode (zip-xml/xml1-> zipper :Body :Fault :detail
                  :axlError :axlcode zip-xml/text)]
    (not (= axlCode "5007"))))
