(ns addphone.core
  (:gen-class)
  (:require [addphone.xml.deviceProfile :as deviceProfile]
            [addphone.xml.line :as line]
            [addphone.xml.user :as user]
            [addphone.xml.phone :as phone]
            [addphone.xml.parseAxlResponse :as parse]
            [addphone.http.client :as client]
            [addphone.getResource :as rsc]))

(def americas {:ip "10.230.154.5" :ver "10.5"})
(def offices (rsc/getResource "offices.edn"))

(defn request
  [cluster func & args]
  (let [funcmap (apply func args)
        name (:name funcmap)
        xml (:xml funcmap)]
   @(client/axl cluster name xml)))

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
              (println (request americas deviceProfile/addDeviceProfile phone))
              (println (request americas phone/addPhone phone))
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

