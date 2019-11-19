(ns addphone.utilities.parsers
  (:require [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))

(defn predList
  [responseTag & preds]
  (concat (list :Envelope :Body responseTag :return) preds (list zip-xml/text)))
  
(defn xmlSingleton
  [xmlZipper]
  (partial zip-xml/xml1-> xmlZipper))

(defn xmlAll
  [xmlZipper]
  (partial zip-xml/xml-> xmlZipper))


(defn getAppUser
  [xmlResponse]
  (let [zipper (zipify xmlResponse)
        name (apply (xmlSingleton zipper) (predList :getAppUserResponse :appUser :userid))
        deviceList (apply (xmlAll zipper) (predList :getAppUserResponse :appUser :associatedDevices :device))]
    deviceList))


(defn getCss
  [xmlResponse]
  (let [zipper (zipify xmlResponse)
        cssName (apply (xmlSingleton zipper) (predList :getCssResponse :css :name))
        ptList (apply (xmlAll zipper) (predList :getCssResponse :css :members :member :routePartitionName))
        ptIdxList (apply (xmlAll zipper) (predList :getCssResponse :css :members :member :index))
        zippedIdxPt (map list ptIdxList ptList)
        sortedZippedIdxPt (sort-by #(Integer/parseInt (first %)) zippedIdxPt)]
    (conj 
      (map second sortedZippedIdxPt)
      cssName)))


(defn listCss
  [xmlResponse]
  (let [zipper (zipify xmlResponse)
        cssNameList (apply (xmlAll zipper) (predList :listCssResponse :css :name))]
    cssNameList))


(defn listRoutePartition
  [xmlResponse]
  (let [zipper (zipify xmlResponse)
        ptNameList (apply (xmlAll zipper) (predList :listRoutePartitionResponse :routePartition :name))]
    ptNameList))

