(ns addphone.core
  (:gen-class)
  (:require [addphone.xml.deviceProfile :as deviceProfile]
            [addphone.xml.line :as line]
            [addphone.xml.user :as user]
            [addphone.xml.parseAxlResponse :as parse]
            [addphone.http.client :as client]))

(def americas
  {:ip "10.230.154.5" :ver "10.5"})

;userId description line loc model
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
  (let [argmap (zipmap '(:userId :description :line :loc :model) args)
        userExists? (fn [userId] (parse/exists? (request americas user/getUser userId)))
        lineExists? (fn [line pt] (parse/exists? (request americas line/getLine line pt)))
        deviceProfileExists? (fn [userId] (parse/exists? (request americas deviceProfile/getDeviceProfile userId)))]

    (def proceed?
       (every? false? (list 
                        (echo "Checking if user exists... " userExists? (:userId argmap))
                        (echo "Checking if line exists... " lineExists? (:line argmap) (:loc argmap))
                        (echo "Checking if device profile exists... " deviceProfileExists? (:userId argmap)))))
    (cond 
      (false? proceed?) (println "Cannot proceed, verify the user/profile/extension are not already assigned")
      :else (println (request americas deviceProfile/addDeviceProfile argmap)))))

