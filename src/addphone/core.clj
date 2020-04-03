(ns addphone.core
  (:gen-class)
  (:require [addphone.xml.deviceProfile :as deviceProfile]
            [addphone.xml.line :as line]
            [addphone.xml.user :as user]
            [addphone.xml.phone :as phone]
            [addphone.xml.transPattern :as transPattern]
            [addphone.xml.parseAxlResponse :as parseResp]
            [addphone.xml.extensionMobility :as em]
            [addphone.xml.listUser :as listUser]
            [addphone.xml.listCss :as listCss]
            [addphone.xml.listLine :as listLine]
            [addphone.xml.listPhone :as listPhone]
            [addphone.xml.listRoutePartition :as listRoutePartition]
            [addphone.xml.listDeviceNumplanMap :as listDeviceNumplanMap]
            [addphone.xml.listDeviceProfile :as listDeviceProfile]
            [addphone.xml.getAppUser :as getAppUser]
            [addphone.xml.getCss :as getCss]
            [addphone.xml.getDeviceProfile :as getDeviceProfile]
            [addphone.xml.getUser :as getUser]
            [addphone.xml.addCss :as addCss]
            [addphone.xml.addPhone :as addPhone]
            [addphone.xml.updateAppUser :as updateAppUser]
            [addphone.xml.updateCss :as updateCss]
            [addphone.xml.updateUser :as updateUser]
            [addphone.xml.updateDeviceProfile :as updateDeviceProfile]
            [addphone.xml.updatePhone :as updatePhone]
            [addphone.http.client :as client]
            [addphone.getResource :as rsc]
            [addphone.utilities.parsers :as parse]
            [clojure.set :refer [difference union]]
            [clojure.data :as data]
            [clojure.data.xml :as xml]
            [clojure.pprint :as pp]))


(def apac {:ip "10.230.210.51" :ver "10.5"})
(def americas {:ip "10.230.154.5" :ver "10.5"})
(def emea {:ip "10.145.34.51" :ver "10.5"})
(def offices (rsc/getResource "offices.edn"))
(def tranPatternEdn (rsc/getResource "transPatternUpdate.edn"))


(def clusterMap
  {:amer americas
   :emea emea
   :apac apac})   


(defn request
  [clusterStr func & args]
  (let [clusterNode ((keyword clusterStr) clusterMap) 
        funcmap (apply func args)
        name (:name funcmap)
        xml (:xml funcmap)]
   @(client/axl clusterNode name xml)))


(defn new_request
  [cluster func & args]
  (let [funcmap (apply func args)
        name (:name funcmap)
        xml (:xml funcmap)
        parseFn (:parse funcmap)]
   (parseFn @(client/axl cluster name xml))))


(comment
  (defn -main
    "I do nothing by default.  Specify one of my functions below"
    [& args]))


;Extension mobility login function.  Example:
;lein run -m addphone.core/login amer SEP2C31246C6B04 slouli 
(defn login
  [cluster & args]
  (println (apply (partial request cluster em/doDeviceLogin) args)))


;Extension mobility logout function.  Example:
;lein run -m addphone.core/logout amer SEP2C31246C6B04  
(defn logout
  [cluster & args]
  (println (apply (partial request cluster em/doDeviceLogout) args)))


;Add Translation Pattern.  Example:
;lein run -m addphone.core/addTransPattern amer 111111111 60362 "Steve Translation" PT-RichmondHill-Dev CSS-RichmondHill-Internal
(defn addTransPattern
  [cluster & args]
  (println (apply (partial request cluster transPattern/addTransPattern) args)))

(def updateTransPattern
  (map #(apply (partial request amer transPattern/updateTransPattern) %) tranPatternEdn))
  

;Add devices to applicatin user "pguser".
;Useful for bulk contact center updates where lots of searching and clicking would be necessary.
;Example:
;lein run addphone.core/addPgDevices amer CSFslouli CSFogonzal CSFjwarhurs CSF...
(defn addPgDevices
  [cluster & deviceList]
  (let [appuser "pguser"
        ;;newDevFile "pgUserDevices.edn"
        appUserXml (request cluster getAppUser/getAppUser appuser)
        currentDevAssoc (parse/getAppUser appUserXml)
        newDevAssoc (distinct (concat currentDevAssoc deviceList))
        requestfn (partial request cluster updateAppUser/updateAppUser appuser)]
    (do
      ;;(println newDevAssoc)
      (println (apply requestfn newDevAssoc)))))


(defn tableConv
  "
  Converts a list of the format:
  (CSS-Loc-Dev PT1 PT2...) (CSS_NewLoc-Dev PT1 PT2...) to a list of hash maps
  ({:CSS-Loc-Dev PT1 :CSS-NewLoc-Dev PT1} {:CSS-Loc-Dev PT1 PT2 ...} ...)
  Since there will be unbalanced lists between the old and new, the extra rows
  for the old PT will list 'NEW' to indicate that that row is a new entry.
  "
  [oldCssList newCssList]
  (let [oldKeyword (keyword (str "OLD_" (first oldCssList)))
        newKeyword (keyword (str "NEW_" (first newCssList)))
        oldTable (for [pt (rest oldCssList)
                       :let [y {oldKeyword pt}]]
                   y)
        newTable (for [pt (rest newCssList)
                       :let [y {newKeyword pt}]]
                   y)]
    (map conj 
      (concat oldTable (repeat {oldKeyword "NEW"})) 
      newTable)))


(defn getPartitions
  [cluster & pts]
  (let [ptGetRequest (partial request cluster listRoutePartition/listRoutePartition)]
    (loop [findPts pts
           foundPts '()]
      (cond
        (empty? findPts) foundPts
        :else (recur 
                (rest findPts)
                (distinct (concat foundPts (parse/listRoutePartition (ptGetRequest (first findPts))))))))))


(defn addDeviceCss
  [cluster cssName & ptQueries]
  (let [addRequest (partial request cluster addCss/addCss cssName)
        ptList (apply (partial getPartitions cluster) ptQueries)]
    (do
      (def table (tableConv (list cssName) (conj ptList cssName)))
      (pp/print-table table)
      (print "Execute this change? [n]: ")
      (flush)
      (def userInput (read-line))
      (cond
        (= userInput "y") (println (apply addRequest ptList))
        :else (println "Did not execute the change.")))))


(defn updateDeviceCss
  "
  Updates the Calling Search Spaces polled by listCss API function call.  The steps below are:
  -cssList: Gets the existing Calling Search Spaces, filtered by CSSs' containing '%Device%' in the name'
  -curCssList: Parses XML stored in cssList.  Produces a list of lists, of the format:
  --'((CSS-Toronto-Device PT-Addison-Dev PT-Toronto-Dev ...) (CSS-RichmondHill-Device PT-Addison-Dev PT-...) ...)
  -newCssList: Appends list of new partitions to each CSS list in curCssList
  
  Finally, the new calling search spaces stored in newCssList are sent to the call manager.
  "
  [cluster cssFilter & ptQueries]
  (let [cssList (parse/listCss (request cluster listCss/listCss cssFilter))
        curCssList (map #(parse/getCss (request cluster getCss/getCss %)) cssList)
        ptList (apply (partial getPartitions cluster) ptQueries)
        newCssList (map #(distinct (concat %1 %2)) curCssList (repeat ptList))
        updateRequest (partial request cluster updateCss/updateCss)]
    (do
      (def tables (map #(tableConv %1 %2) curCssList newCssList))
      (doall (map #(pp/print-table %) tables))
      (print "Execute this change? [n]: ")
      (flush)
      (def userInput (read-line))
      (cond
        (= userInput "y") (println (map #(apply updateRequest %) newCssList))
        :else (println "Did not execute the change.")))))
