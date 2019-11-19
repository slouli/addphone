(ns addphone.xml.getDeviceProfile
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))


(defn parseLine
  [linesLoc]
  (let [lineLocs (zip-xml/xml-> linesLoc :lines :line)
        vectorize (fn [lineLoc] 
                    [(zip-xml/xml1-> lineLoc :line :index zip-xml/text)
                     (zip-xml/xml1-> lineLoc :line :dirn :pattern zip-xml/text)
                     (zip-xml/xml1-> lineLoc :line :dirn :routePartitionName zip-xml/text)
                     (zip-xml/xml1-> lineLoc :line :display zip-xml/text)
                     (zip-xml/xml1-> lineLoc :line :e164Mask zip-xml/text)])]                     
    (map vectorize lineLocs)))
    

(defn parseGetDeviceProfile
  [xmlResp]
  (let [zipper (zipify xmlResp)
        name (zip-xml/xml1-> zipper :Envelope :Body :getDeviceProfileResponse
               :return :deviceProfile :name zip-xml/text)
        descr (zip-xml/xml1-> zipper :Envelope :Body :getDeviceProfileResponse
                :return :deviceProfile :description zip-xml/text)
        userLocale (zip-xml/xml1-> zipper :Envelope :Body :getDeviceProfileResponse
                     :return :deviceProfile :userLocale zip-xml/text)
        linesLoc (zip-xml/xml1-> zipper :Envelope :Body :getDeviceProfileResponse
                   :return :deviceProfile :lines)]    
    {:name name
     :description descr
     :userLocale userLocale
     :lines (parseLine linesLoc)}))
    


(defn getDeviceProfile
  [userid]
  {:name "getDeviceProfile"
   :xml (xml/element :ns:getDeviceProfile {:sequence "?"}
          (xml/element :name {} userid)
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :name {})
            (xml/element :description {})
            (xml/element :userLocale {})
            (xml/element :lines {}
              (xml/element :line {}
                (xml/element :index {})
                (xml/element :display {})
                (xml/element :e164Mask {})
                (xml/element :dirn {}
                  (xml/element :pattern {})
                  (xml/element :routePartitionName {}))))))})
