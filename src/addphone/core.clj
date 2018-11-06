(ns addphone.core
  (:gen-class)
  (:require [addphone.xml.deviceProfile :as deviceProfile]
            [addphone.xml.line :as line]
            [addphone.xml.user :as user]
            [addphone.xml.parseAxlResponse :as parse]
            [addphone.http.client :as client]))

(def americas
  {:ip "10.230.154.5" :ver "10.5"})

(defn request
  [cluster func & args]
  (let [funcmap (apply func args)
        name (:name funcmap)
        xml (:xml funcmap)]
  @(client/axl cluster name xml)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [userExists? (fn [userId] (parse/exists? (request americas user/getUser userId)))
        lineExists? (fn [line pt] (parse/exists? (request americas line/getLine line pt)))
        deviceProfileExists? (fn [userId] (parse/exists? (request americas deviceProfile/getDeviceProfile userId)))]

  (defn argmap
    [args]
    (let [keys '(:userId :fname :line :location)]
      (zipmap keys args)))

  (println (or (userExists? (first args))
               (lineExists? "1234" "PT-Toronto-Dev")
               (deviceProfileExists? (first args))))
  (println (argmap args))))
