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
            [clojure.core.match :refer [match]]
            [clojure.set :refer [difference union]]
            [clojure.data :as data]
            [clojure.data.xml :as xml]
            [clojure.core.async :as async
             :refer [go chan buffer close! put! take! timeout]]))


(def apac {:ip "10.230.210.51" :ver "10.5"})
(def americas {:ip "10.230.154.5" :ver "10.5"})
(def emea {:ip "10.145.34.51" :ver "10.5"})
(def offices (rsc/getResource "offices.edn"))

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


(defn -main
  "I do nothing by default.  Specify one of my functions below"
  [& args])

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


(defn addDeviceCss
  [cluster cssName]
  (let [ptListXml (request cluster listRoutePartition/listRoutePartition "PT-%-Dev")
        ptList (parse/listRoutePartition ptListXml)]
    (do
      ;;(println ptList)
      (println (apply (partial request americas addCss/addCss cssName) ptList)))))
  

(defn udpateDeviceCss
  "
  Updates the Calling Search Spaces polled by listCss API function call.  The steps below are:
  -cssList: Gets the existing Calling Search Spaces, filtered by CSSs' containing '%Device%' in the name'
  -curCssList: Parses XML stored in cssList.  Produces a list of lists, of the format:
  --'((CSS-Toronto-Device PT-Addison-Dev PT-Toronto-Dev ...) (CSS-RichmondHill-Device PT-Addison-Dev PT-...) ...)
  -newCssList: Appends list of new partitions to each CSS list in curCssList
  
  Finally, the new calling search spaces stored in newCssList are sent to the call manager.
  "
  [cluster & newPts]
  (def cssList (parse/listCss (request cluster listCss/listCss "%Device%")))
  ;;(println (request americas updateCss/updateCss "CSS-Test" "PT-Addison-Dev" "PT-APAC-Outbound"))
  (def curCssList (map #(parse/getCss (request cluster getCss/getCss %)) cssList))
  (def newCssList (map #(distinct (concat %1 %2)) curCssList (repeat newPts)))
  (do
    ;;(println newCssList)
    (println (map #(apply (partial request americas updateCss/updateCss) %) newCssList))))
