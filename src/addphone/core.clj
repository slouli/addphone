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

"""
;Associate placeholder lines with phones
(defn updatePhone
  [& args]
  (println (apply (partial request americas phone/updatePhone) args)))
"""

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

(defn updateAlpharetta
  [& args]
  (let [updatePhoneList (rsc/getResource "updateAlpharetta.edn")
        requestfn (partial request americas updatePhone/updatePhone)]
    (println (map #(apply requestfn %) updatePhoneList))))

(defn updateAlpharettaDp
  [& args]
  (let [updatePhoneList (rsc/getResource "updateAlpharetta.edn")
        requestfn (partial request americas updateDeviceProfile/updateDeviceProfile)]
    (println (map #(apply requestfn %) updatePhoneList))))

(defn updateAlpharettaUsers
  [& args]
  (let [updatePhoneList (rsc/getResource "updateAlpharetta.edn")
        requestfn (partial request americas updateUser/updateUser)]
    (println (map #(requestfn (first %)) updatePhoneList))))

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
  

(defn balanceVc
  [& args]
  (def americasAllVc (new_request americas listUser/listUserGeneral {:userIdPrefix "vc-"}))
  (def emeaAllVc (new_request emea listUser/listUserGeneral {:userIdPrefix "vc-"}))
  (def apacAllVc (new_request apac listUser/listUserGeneral {:userIdPrefix "vc-"}))
  
  (defn stripHomed
    [vcElement]
    [(vcElement 0) (vcElement 2) (vcElement 3) (vcElement 4)])
    
  ;For comprehensions would have been nicer here.  Maybe rewrite sometime.
  (def americasHomedVcs (set (map stripHomed (filter #(= "true" (% 1)) americasAllVc))))
  (def emeaHomedVcs (set (map stripHomed (filter #(= "true" (% 1)) emeaAllVc))))
  (def apacHomedVcs (set (map stripHomed (filter #(= "true" (% 1)) apacAllVc))))
  
  (def americasAllVcNoHome (set (map stripHomed americasAllVc)))
  (def emeaAllVcNoHome(set (map stripHomed emeaAllVc)))
  (def apacAllVcNoHome (set (map stripHomed apacAllVc)))
  
  (def allVcUnits (union americasHomedVcs emeaHomedVcs apacHomedVcs))
  (def missingAmericas (difference allVcUnits americasAllVcNoHome))
  (def missingEMEA (difference allVcUnits emeaAllVcNoHome))
  (def missingAPAC (difference allVcUnits apacAllVcNoHome))
  (println missingAmericas)
  (println)
  (println missingEMEA)
  (println)
  (println missingAPAC))
  ;(println (difference americasAllVcNoHome americasHomedVcs americasHomedVcs)))
  ;(println (difference americasHomedVcs emeaHomedVcs)))

(defn bulkCreateJabber
  [& args]
  (comment
    Items that need updating between each cluster
      1. CSS-.*-Dev or Device
      2. The cluster being contacted
      3. The voicemail service profile in updateUser
      4. Update the license user in addPhone)
     
  (def pt2dpmap 
    {;;AMERICAS
     "PT-Addison-Dev" "DP-Addison"
     "PT-Alpharetta-Dev" "DP-Alpharetta"
     "PT-Austin-Dev" "DP-Austin"
     "PT-Balitmore-Dev" "DP-Baltimore-IPC"
     "PT-Bellevue-Dev" "DP-Bellevue"
     "PT-Brentwood-Dev" "DP-Brentwood"
     "PT-Brookpark-Dev" "DP-Brookpark"
     "PT-Burlington-Dev" "DP-Burlington-IPC"
     "PT-Calgary-Dev" "DP-Calgary"
     "PT-Chicago-Dev" "DP-Chicago"
     "PT-Denver-Dev" "DP-Denver-IPC"
     "PT-Dulles-Dev" "DP-Dulles"
     "PT-Gaithersburg-Dev" "DP-Gaithersburg"
     "PT-Hilliard-Dev" "DP-Hilliard"
     "PT-Irvine-Dev" "DP-Irvine"
     "PT-Kingston-Dev" "DP-Kingston"
     "PT-Latham-Dev" "DP-Latham"
     "PT-Lexington-Dev" "DP-Lexington"
     "PT-Montreal-Dev" "DP-Montreal"
     "PT-NYC-Dev" "DP-NYC"
     "PT-Norwood-Dev" "DP-Norwood"
     "PT-Ottawa-Dev" "DP-Ottawa"
     "PT-OverlandPark-Dev" "DP-OverlandPark"
     "PT-Pasadena-Dev" "DP-Pasadena"
     "PT-Peterborough-Dev" "DP-Peterborough"
     "PT-Pleasanton-Dev" "DP-Pleasanton"
     "PT-RichmondHill-Dev" "DP-RichmondHill"
     "PT-Rochester-Dev" "DP-Rochester"
     "PT-Roseville-Dev" "DP-Roseville"
     "PT-SanMateo-Dev" "DP-SanMateo"
     "PT-SaoPaulo-Dev" "DP-SaoPaulo"
     "PT-Scottsdale-Dev" "DP-Scottsdale"
     "PT-Southfield-Dev" "DP-Southfield"
     "PT-Tallahassee-Dev" "DP-Tallahassee"
     "PT-Tampa-Dev" "DP-Tampa"
     "PT-TintonFalls-Dev" "DP-TintonFalls"
     "PT-Toronto-Dev" "DP-Toronto"
     "PT-Tucson-Dev" "DP-Tucson"
     "PT-Waterloo-Dev" "DP-Waterloo"
     "PT-Waukesha-Dev" "DP-Waukesha"
     ;;APAC
     "PT-BEJ-Dev" "DP-Beijing"
     "PT-Bangalore-Dev" "DP-Bangalore"
     "PT-Hyderabad-Dev" "DP-Hyderabad"
     "PT-Manila-Dev" "DP-Manila"
     "PT-Melbourne-Dev" "DP-Melbourne"
     "PT-SHA-Dev" "DP-Shanghai"
     "PT-Seoul-Dev" "DP-Seoul"
     "PT-Singapore-Dev" "DP-Singapore"
     "PT-Tokyo-Dev" "DP-Tokyo"
     ;;EMEA
     "PT-BAD-Dev" "DP-Baden"
     "PT-BAR-Dev" "DP-Barcelona"
     "PT-COR-Dev" "DP-Cork"
     "PT-DUS-Dev" "DP-Duesseldorf"
     "PT-FRA-Dev" "DP-Frankfurt"
     "PT-GBG-Dev" "DP-Gothenburg"
     "PT-HAM-Dev" "DP-Hamburg"
     "PT-HEL-Dev" "DP-Helsinki"
     "PT-HFD-Dev" "DP-Hoofddorp"
     "PT-HUE-Dev" "DP-Huerth"
     "PT-HVR-Dev" "DP-Hannover"
     "PT-JOH-Dev" "DP-Joburg"
     "PT-KLA-Dev" "DP-Klagenfurt"
     "PT-KON-Dev" "DP-Konstanz"
     "PT-KPT-Dev" "DP-Kempten"
     "PT-LDN-Dev" "DP-London"
     "PT-MAD-Dev" "DP-Madrid"
     "PT-MIL-Dev" "DP-Milan"
     "PT-MUC-Dev" "DP-Munich"
     "PT-OLD-Dev" "DP-Oldenburg"
     "PT-PAR-Dev" "DP-Paris"
     "PT-POZ-Dev" "DP-Poznan"
     "PT-PRA-Dev" "DP-Prague"
     "PT-PRE-Dev" "DP-Preston"
     "PT-PUT-Dev" "DP-Putten"
     "PT-RDG-Dev" "DP-Reading"
     "PT-RHE-Dev" "DP-Rheinbach"
     "PT-ROM-Dev" "DP-Rome"
     "PT-ROT-Dev" "DP-Rotterdam"
     "PT-SAB-Dev" "DP-StAlbans"
     "PT-SPT-Dev" "DP-StPetersburg"
     "PT-STH-Dev" "DP-Stockholm"
     "PT-VIE-Dev" "DP-Vienna"})
  
  (def pt2networklocale
    {;;AMERICAS
     "PT-Addison-Dev" "United States"
     "PT-Alpharetta-Dev" "United States"
     "PT-Austin-Dev" "United States"
     "PT-Baltimore-Dev" "United States"
     "PT-Bellevue-Dev" "United States"
     "PT-Brentwood-Dev" "United States"
     "PT-Brookpark-Dev" "United States"
     "PT-Burlington-Dev" "United States"
     "PT-Calgary-Dev" "Canada"
     "PT-Chicago-Dev" "United States"
     "PT-Denver-Dev" "United States"
     "PT-Dulles-Dev" "United States"
     "PT-Gaithersburg-Dev" "United States"
     "PT-Hilliard-Dev" "United States"
     "PT-Irvine-Dev" "United States"
     "PT-Kingston-Dev" "Canada"
     "PT-Latham-Dev" "United States"
     "PT-Lexington-Dev" "United States"
     "PT-Montreal-Dev" "Canada"
     "PT-NYC-Dev" "United States"
     "PT-Norwood-Dev" "United States"
     "PT-Ottawa-Dev" "Canada"
     "PT-OverlandPark-Dev" "United States"
     "PT-Pasadena-Dev" "United States"
     "PT-Peterborough-Dev" "Canada"
     "PT-Pleasanton-Dev" "United States"
     "PT-RichmondHill-Dev" "Canada"
     "PT-Rochester-Dev" "United States"
     "PT-Roseville-Dev" "United States"
     "PT-SanMateo-Dev" "United States"
     "PT-SaoPaulo-Dev" "Brazil"
     "PT-Scottsdale-Dev" "United States"
     "PT-Southfield-Dev" "United States"
     "PT-Tallahassee-Dev" "United States"
     "PT-Tampa-Dev" "United States"
     "PT-TintonFalls-Dev" "United States"
     "PT-Toronto-Dev" "Canada"
     "PT-Tucson-Dev" "United States"
     "PT-Waterloo-Dev" "Canada"
     "PT-Waukesha-Dev" "United States"
     ;;APAC
     "PT-BEJ-Dev" ""
     "PT-Bangalore-Dev" ""
     "PT-Hyderabad-Dev" ""
     "PT-Manila-Dev" ""
     "PT-Melbourne-Dev" ""
     "PT-SHA-Dev" ""
     "PT-Seoul-Dev" ""
     "PT-Singapore-Dev" ""
     "PT-Tokyo-Dev" "Japan"
     ;;EMEA
     "PT-BAD-Dev" ""
     "PT-BAR-Dev" ""
     "PT-COR-Dev" ""
     "PT-DUS-Dev" ""
     "PT-FRA-Dev" ""
     "PT-GBG-Dev" ""
     "PT-HAM-Dev" ""
     "PT-HEL-Dev" ""
     "PT-HFD-Dev" ""
     "PT-HUE-Dev" "Germany"
     "PT-HVR-Dev" ""
     "PT-JOH-Dev" ""
     "PT-KLA-Dev" ""
     "PT-KON-Dev" ""
     "PT-KPT-Dev" ""
     "PT-LDN-Dev" ""
     "PT-MAD-Dev" ""
     "PT-MIL-Dev" ""
     "PT-MUC-Dev" ""
     "PT-OLD-Dev" ""
     "PT-PAR-Dev" ""
     "PT-POZ-Dev" ""
     "PT-PRA-Dev" ""
     "PT-PRE-Dev" ""
     "PT-PUT-Dev" ""
     "PT-RDG-Dev" "United Kingdom"
     "PT-RHE-Dev" ""
     "PT-ROM-Dev" ""
     "PT-ROT-Dev" ""
     "PT-SAB-Dev" ""
     "PT-SPT-Dev" ""
     "PT-STH-Dev" ""
     "PT-VIE-Dev" ""})
  
  (def pt2loc
    {;;AMERICAS
     "PT-Addison-Dev" "Loc-Addison"
     "PT-Alpharetta-Dev" "Loc-Alpharetta"
     "PT-Austin-Dev" "Loc-Austin"
     "PT-Baltimore-Dev" "Loc-Baltimore"
     "PT-Bellevue-Dev" "Loc-Bellevue"
     "PT-Brentwood-Dev" "Loc-Brentwood"
     "PT-Brookpark-Dev" "Loc-Brookpark"
     "PT-Burlington-Dev" "Loc-Burlington"
     "PT-Calgary-Dev" "Loc-Calgary"
     "PT-Chicago-Dev" "Loc-Chicago"
     "PT-Denver-Dev" "Loc-Denver"
     "PT-Dulles-Dev" "Loc-Dulles"
     "PT-Gaithersburg-Dev" "Loc-Gaithersburg"
     "PT-Hilliard-Dev" "Loc-Hilliard"
     "PT-Irvine-Dev" "Loc-Irvine"
     "PT-Kingston-Dev" "Loc-Kingston"
     "PT-Latham-Dev" "Loc-Latham"
     "PT-Lexington-Dev" "Loc-Lexington"
     "PT-Montreal-Dev" "Loc-Montreal"
     "PT-NYC-Dev" "Loc-NYC"
     "PT-Norwood-Dev" "Loc-Norwood"
     "PT-Ottawa-Dev" "Loc-Ottawa"
     "PT-OverlandPark-Dev" "Loc-OverlandPark"
     "PT-Pasadena-Dev" "Loc-Pasadena"
     "PT-Peterborough-Dev" "Loc-Peterborough"
     "PT-Pleasanton-Dev" "Loc-Pleasanton"
     "PT-RichmondHill-Dev" "Loc-RichmondHill"
     "PT-Rochester-Dev" "Loc-Rochester"
     "PT-Roseville-Dev" "Loc-Roseville"
     "PT-SanMateo-Dev" "Loc-SanMateo"
     "PT-SaoPaulo-Dev" "Loc-SaoPaulo"
     "PT-Scottsdale-Dev" "Loc-Scottsdale"
     "PT-Southfield-Dev" "Loc-Southfield"
     "PT-Tallahassee-Dev" "Loc-Tallahassee"
     "PT-Tampa-Dev" "Loc-Tampa"
     "PT-TintonFalls-Dev" "Loc-TintonFalls"
     "PT-Toronto-Dev" "Loc-Toronto"
     "PT-Tucson-Dev" "Loc-Tucson"
     "PT-Waterloo-Dev" "Loc-Waterloo"
     "PT-Waukesha-Dev" "Loc-Waukesha"
     ;;APAC
     "PT-BEJ-Dev" "Loc-Beijing"
     "PT-Bangalore-Dev" "Loc-Bangalore"
     "PT-Hyderabad-Dev" "Loc-Hyderabad"
     "PT-Manila-Dev" "Loc-Manila"
     "PT-Melbourne-Dev" "Loc-Melbourne"
     "PT-SHA-Dev" "Loc-Shanghai"
     "PT-Seoul-Dev" "Loc-Seoul"
     "PT-Singapore-Dev" "Loc-Singapore"
     "PT-Tokyo-Dev" "Loc-Tokyo"
     ;;EMEA
     "PT-BAD-Dev" "Loc-Baden"
     "PT-BAR-Dev" "Loc-Barcelona"
     "PT-COR-Dev" "Loc-Cork"
     "PT-DUS-Dev" "Loc-Duesseldorf"
     "PT-FRA-Dev" "Loc-Frankfurt"
     "PT-GBG-Dev" "Loc-Gothenburg"
     "PT-HAM-Dev" "Loc-Hamburg"
     "PT-HEL-Dev" "Loc-Helsinki"
     "PT-HFD-Dev" "Loc-Hoofddorp"
     "PT-HUE-Dev" "Loc-Huerth"
     "PT-HVR-Dev" "Loc-Hannover"
     "PT-JOH-Dev" "Loc-Joburg"
     "PT-KLA-Dev" "Loc-Klagenfurt"
     "PT-KON-Dev" "Loc-Konstanz"
     "PT-KPT-Dev" "Loc-Kempten"
     "PT-LDN-Dev" "Loc-London"
     "PT-MAD-Dev" "Loc-Madrid"
     "PT-MIL-Dev" "Loc-Milan"
     "PT-MUC-Dev" "Loc-Munich"
     "PT-OLD-Dev" "Loc-Oldenburg"
     "PT-PAR-Dev" "Loc-Paris"
     "PT-POZ-Dev" "Loc-Poznan"
     "PT-PRA-Dev" "Loc-Prague"
     "PT-PRE-Dev" "Loc-Preston"
     "PT-PUT-Dev" "Loc-Putten"
     "PT-RDG-Dev" "Loc-Reading"
     "PT-RHE-Dev" "Loc-Rheinbach"
     "PT-ROM-Dev" "Loc-Rome"
     "PT-ROT-Dev" "Loc-Rotterdam"
     "PT-SAB-Dev" "Loc-StAlbans"
     "PT-SPT-Dev" "Loc-StPetersburg"
     "PT-STH-Dev" "Loc-Stockholm"
     "PT-VIE-Dev" "Loc-Vienna"})
  
  ;;JAVA IO for Logging
  (defn write-file [logEntry]
    (with-open [w (clojure.java.io/writer "C:/Users/slouli/Documents/Documentation/Jabber_Acct_Creation/APAC.txt" :append true)]
      (.write w logEntry)))
  
  ;;ASYNC boilerplate
  (def deviceProfileChan (chan))
  (def createJabberChan (chan))
  (def getUserChan (chan))
  (def updateUserChan (chan))
  
  (def clusterRequest (partial request apac))
  (def allUsersXML (clusterRequest listUser/listUser {:userId "%"}))
  (def userListXML (clusterRequest listUser/listUserSql))
  (def listLinesXML (clusterRequest listLine/listLine))
  (def listPhonesXML (clusterRequest listPhone/listPhone))
  (def listDeviceProfileXML (clusterRequest listDeviceProfile/listDeviceProfile))
  (def deviceNumplanMapXML (clusterRequest listDeviceNumplanMap/listDeviceNumplanMap))
  
  ;;Creating formatted seqeunces to lookup data
  (def activeUsers (listUser/parseListUsers allUsersXML))
  (def rejectUsers (listUser/parseListUserSql userListXML))
  (def allLines (listLine/parseListLine listLinesXML))
  (def existingJabberUsers (listPhone/parseListPhoneSql listPhonesXML))
  (def deviceProfiles (listDeviceProfile/parseListDeviceProfile listDeviceProfileXML))
  
  
  (def pt2devicecss {})
  
  ;;Filter users who are not LDAP enabled, or who have UCCX accounts
  (def jabberUsers
    (into {} (filter (fn [[k v]] (not (contains? rejectUsers k))) activeUsers)))
  
  ;;Filter staff who already have CSF profile
  (def newJabberAccounts
    (into {} (filter (fn [[k v]] (not (contains? existingJabberUsers k))) jabberUsers)))
  
  ;;Filter out users who don't have a configured device profile
  (def finalJabberCreationList
    (into {} (filter (fn [[k v]] (contains? deviceProfiles k)) newJabberAccounts)))
  
  ;;produce user list from above final list
  (def userList (map name (keys finalJabberCreationList)))
  (println (str "Total Accounts: " (count userList)))
  (println userList)
  
  
  (defn updateEndUser
    [userDeviceTuple]
    (do
      (println "Updated User: " (userDeviceTuple 0) " " (userDeviceTuple 1) "\n")
      (write-file (str "Updated User: " (userDeviceTuple 0) " " (pr-str (userDeviceTuple 1)) "\n"))
      (write-file (updateUser/parseUpdateUser (clusterRequest updateUser/updateUser (userDeviceTuple 0) (userDeviceTuple 1))))))
  
  (defn getEndUser
    [userid]
    (do
      (println "Got devices for:" userid)
      (async/put! updateUserChan (getUser/parseGetUser (clusterRequest getUser/getUser userid)))
      (async/take! updateUserChan updateEndUser)))
  
  
  (defn createJabberDevice
    [argmap]
    (let [pt ((first (:lines argmap)) 2)
          networkLocale (get pt2networklocale pt)
          devicePool (get pt2dpmap pt)
          location (get pt2loc pt)
          css (str "CSS-" ((re-find #"PT-(.*)-Dev" pt) 1) "-Dev")]
      (do
        (println (str "Create Jabber Account: " (:name argmap)))
        (clusterRequest addPhone/addPhone
          (conj argmap {:networkLocale networkLocale
                        :css css
                        :devicePool devicePool
                        :location location}))
        (async/put! getUserChan (:name argmap))
        (async/take! getUserChan getEndUser))))
  
  (defn getDeviceProfileXML
    [userId]
    (let [userData (getDeviceProfile/parseGetDeviceProfile (clusterRequest getDeviceProfile/getDeviceProfile userId))
          lineList (map #(% 1) (:lines userData))
          agentExtension? (map #(re-find #"^888" %) lineList)]
      (try
        (do
          (println userData)
          (cond
            (some #(not (nil? %)) agentExtension?) (println (str "Agent Extension Found: " userId))
            :else (do
                    (async/put! createJabberChan userData)
                    (async/take! createJabberChan createJabberDevice))))
        (catch Exception e (write-file (str "\n\n Caught exception for:" userId "\n\n"))))))
  
 
  
  ;;ASYNC BLOCK FOR GETTING DEVICE PROFILE XML
  (doseq [userId (take 500 userList)]
    (async/put! deviceProfileChan userId))
  
  (doseq [_ (take 500 userList)]
    (async/take! deviceProfileChan getDeviceProfileXML))
  
  (comment
    ;;ASYNC BLOCK FOR CREATING JABBER PROFILES XML
    (doseq [_ (take 100 userList)]
      (async/take! createJabberChan createJabberDevice))
    
    ;;ASYNC BLOCK FOR GETTING END USER DEVICES
    (doseq [_ (take 100 userList)]
      (async/take! getUserChan getEndUser))
    
    ;;ASYNC BLOCK FOR UPDATING END USER DEVICES
    (doseq [_ (take 100 userList)]
      (async/take! updateUserChan updateEndUser)))
  
  (while true
    (Thread/sleep 60000))
  
  
  
  ;;(println allLines)
  ;(println rejectUsers)
  ;;(println newJabberAccounts)
  ;;(println finalJabberCreationList)
  ;;(println deviceNumplanMapXML))
  ;;Validate device pools
  
  ;;Get to cooking the new accounts
  
  ;;How do we get external number masks... FUC
  
  ;;Test Jabber Creation
  
  
  (comment
    (println
      (map (partial clusterRequest addPhone/addPhone) 
        (map conj testDevs (repeat {:networkLocale "Canada" 
                                    :css "CSS-Toronto-Device" 
                                    :devicePool "DP-Toronto" 
                                    :location "Loc-Toronto"})))))
  
  (comment
    (println (addPhone/addPhone {:name "CSFslouliTest" 
                                 :description "Test ACCT" 
                                 :devicePool "DP-Toronto"
                                 :css "CSS-Toronto-Device" 
                                 :location "Loc-Toronto"
                                 :userLocale "English United States"
                                 :networkLocale "Canada"
                                 :lines '(["1" "60362" "PT-Toronto-Dev" "Steve Louli" "416956XXXX"]
                                          ["2" "60368" "PT-Toronto-Dev" "Steve Louli" "416956XXXX"])})))
  
  (comment 
    Cool but, legacy code for filtering userList.  New code is much simpler with maps
    (def jabberUsers (for [user allUsersList
                           :let [vecu (into [] user)]
                           :when (= (match vecu
                                      [userid _ _ "true" "1" _ _] (nil? (some #(= % userid) rejectUsers))
                                      :else false) true)
                           (reverse (into '() vecu))]))))
