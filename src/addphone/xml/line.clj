(ns addphone.xml.line
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getLine
  [line loc]
  {:name "getLine"
   :xml (xml/element :ns:getLine {:sequence "?"}
               (xml/element :pattern {} line)
               (xml/element :routePartitionName {} (str "PT-" loc "-Dev")))})

;;Get this from your home lab that is already created
(defn addLine
  ([{:keys [line description loc]}]
   {:name "addLine"
    :xml (xml/element :ns:addLine {:sequence "?"}
           (xml/element :line {}
             (xml/element :pattern {} line)
             (xml/element :usage {} "Device")
             (xml/element :routePartitionName {} (str "PT-" loc "-Dev"))
             (xml/element :description {} description)
             (xml/element :shareLineAppearanceCssName {} (str "CSS-" loc "-International"))
             (xml/element :voiceMailProfileName {} "VoiceMail_NA")
             (xml/element :alertingName {} description)
             (xml/element :asciiAlertingName {} description)
             (xml/element :callForwardAll {}
               (xml/element :forwardToVoiceMail {} "false")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National"))
               (xml/element :secondaryCallingSearchSpaceName {} (str "CSS-" loc "-Device")))
             (xml/element :callForwardBusy {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardBusyInt {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardNoAnswer {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardNoAnswerInt {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardNoCoverage {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardNoCoverageInt {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardOnFailure {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardNotRegistered {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))
             (xml/element :callForwardNotRegisteredInt {}
               (xml/element :forwardToVoiceMail {} "true")
               (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-National")))))}))
  
