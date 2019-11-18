(ns addphone.core
  (:gen-class)
  (:require [addphone.xml.deviceProfile :as deviceProfile]
            [addphone.xml.line :as line]
            [addphone.xml.user :as user]
            [addphone.xml.phone :as phone]
            [addphone.xml.transPattern :as transPattern]
            [addphone.xml.parseAxlResponse :as parse]
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
            [clojure.core.match :refer [match]]
            [clojure.set :refer [difference union]]
            [clojure.data :as data]
            [clojure.data.xml :as xml]
            [clojure.core.async :as async
             :refer [go chan buffer close! put! take! timeout]]))

(def apac {:ip "10.230.210.51" :ver "10.5"})
(def americas {:ip "10.230.154.5" :ver "10.5"})
(def waterloo {:ip "10.2.92.50" :ver "8.5"})
(def emea {:ip "10.145.34.51" :ver "10.5"})
(def offices (rsc/getResource "offices.edn"))

(defn request
  [cluster func & args]
  (let [funcmap (apply func args)
        name (:name funcmap)
        xml (:xml funcmap)]
   @(client/axl cluster name xml)))

(defn new_request
  [cluster func & args]
  (let [funcmap (apply func args)
        name (:name funcmap)
        xml (:xml funcmap)
        parseFn (:parse funcmap)]
   (parseFn @(client/axl cluster name xml))))

(defn echo
  [msg f & args]
  (do
    (print msg)
    (def result (apply f args))
    (println result)
    result))

(defn -main
  "I create the device profile/end user account/jabber accounts for new user onboarding"
  [& args]
  (let [argmap (zipmap '(:userId :description :line :loc) args)  ;Create hashmap of inputs from cmdline
        officeMap ((keyword (:loc argmap)) offices)              ;Get office hashmap
        phone (merge argmap officeMap)                           ;Merge data into one map
        userExists? (fn [userId] (parse/exists? (request americas user/getUser userId)))
        lineExists? (fn [line pt] (parse/exists? (request americas line/getLine line pt)))
        deviceProfileExists? (fn [userId] (parse/exists? (request americas deviceProfile/getDeviceProfile userId)))
        phoneExists? (fn [name] (parse/exists? (request americas phone/getPhone name)))]
    
    (def proceed?
      (every? false? (list 
                       (echo "Checking if user exists... " userExists? (:userId argmap))
                       (echo "Checking if line exists... " lineExists? (:line argmap) (:loc argmap))
                       (echo "Checking if device profile exists... " deviceProfileExists? (:userId argmap))
                       (echo "Checking if Jabber phone exists... " phoneExists? (str "CSF" (:userId argmap)))
                       (echo "Checking if IPC phone exists... " phoneExists? (str "SEP" (:userId argmap))))))
    (cond 
      (false? proceed?) (println "Cannot proceed, verify the user/profile/extension are not already assigned")
      :else (do
              (println (request americas line/addLine phone))
              (println (if (contains? phone :deviceProfile) (request americas deviceProfile/addDeviceProfile phone) "SKIP"))
              (println (if (contains? phone :phone) (request americas phone/addPhone phone) "SKIP"))
              (println (request americas user/addUser phone))
              (println (request americas user/updateUser phone))
              (println)
              (println "User Detail...")
              (println "UserId: " (:userId phone))
              (println "Ext/PIN: " (:line phone))
              (println)
              (println "IPC Details")
              (println "-IP Communicator: " (str "SEP" (:userId phone)))
              (println "-TFTP Server 1: " "10.230.122.5")
              (println "-TFTP Server 2: " "10.230.154.6")
              (println)
              (println "Voicemail PIN: 258369")
              (println (str "set-aduser " (:userId phone) " -replace @{'ipPhone'='*1" (:line phone) "'}"))))))



;Extension mobility login function.  To run use:
;lein run -m addphone.core/login name userId 
(defn login
  [& args]
  (println (apply (partial request americas em/doDeviceLogin) args)))

(defn logout
  [& args]
  (println (apply (partial request americas em/doDeviceLogout) args)))

;Add phones to the new cluster
(defn addPhone
  [& args]
  (println (request americas phone/addPhone (zipmap '(:mac :description :loc :userLocale :networklocale :phone) args))))

;Add placeholder lines for new office
(defn addLine
  [& args]
  (println  (request americas line/addLine (zipmap '(:line :description :loc :prime?) args))))

;Add Translation Pattern
(defn addTransPattern
  [& args]
  (println (apply (partial request americas transPattern/addTransPattern) args)))

;Update Translation Pattern
(defn updateTransPattern
  [& args]
  (let [transPatternList (rsc/getResource "translationPatternUpdate.edn")
        requestfn (partial request americas transPattern/updateTransPattern)]
    (doall
      (println (map #(apply requestfn %) transPatternList)))))

(defn doSpecialLogin
  [& args]
  (let [ipcList (rsc/getResource "ipcLogin.edn")
        requestfn (partial request waterloo em/doDeviceLogin)]
    (doall
      (println (map #(apply requestfn %) ipcList)))))

(defn addPgDevices
  [& deviceList]
  (let [appuser "testapp"
        newDevFile "pgUserDevices.edn"
        targetCluster apac
        currentAppUser (request targetCluster getAppUser/getAppUser appuser)
        currentDevAssoc (getAppUser/parseGetAppUser currentAppUser)
        newDevAssoc (distinct (concat currentDevAssoc (rsc/getResource newDevFile)))
        requestfn (partial request targetCluster updateAppUser/updateAppUser appuser)]
    (println (apply requestfn newDevAssoc))))


(defn addDeviceCss
  [cssName]
  (def ptList 
    (listRoutePartition/parseListRoutePartition (request americas listRoutePartition/listRoutePartition)))
  (println (apply (partial request americas addCss/addCss cssName) ptList)))
  

(defn udpateDeviceCss
  "
  Updates the Calling Search Spaces polled by listCss API function call.  The steps below are:
  -cssList: Gets the existing Calling Search Spaces, filtered by CSSs' containing '%Device%' in the name'
  -curCssList: Parses XML stored in cssList.  Produces a list of lists, of the format:
  --'((CSS-Toronto-Device PT-Addison-Dev PT-Toronto-Dev ...) (CSS-RichmondHill-Device PT-Addison-Dev PT-...) ...)
  -newCssList: Appends list of new partitions to each CSS list in curCssList
  
  Finally, the new calling search spaces stored in newCssList are sent to the call manager.
  "
  [& newPts]
  (def cssList (listCss/parseListCss (request americas listCss/listCss "%Device%")))
  ;;(println (request americas updateCss/updateCss "CSS-Test" "PT-Addison-Dev" "PT-APAC-Outbound"))
  (def curCssList (map #(getCss/parseGetCss (request americas getCss/getCss %)) cssList))
  (def newCssList (map #(distinct (concat %1 %2)) curCssList (repeat newPts)))
  ;;(println (request americas getCss/getCss "CSS-Toronto-Device"))
  ;;(println (first curCssList)))
  ;;(println (first newCssList)))
  ;;(println newCssList))
  (println (map #(apply (partial request americas updateCss/updateCss) %) newCssList)))
