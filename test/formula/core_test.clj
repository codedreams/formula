(ns formula.core-test
  (:use clojure.test
        formula.core
        hiccup.core))

;; (pprint (html (fform [:post "/login"]
;;                      [[:textarea :username {:class "usern" :value "<i>"}]
;;                       [:password :password {:class "pass"}]]
;;                      {:username "bad" :password "bad"})))

;; (fform [:post "/login"]
;;        [[:dropdown :username {:class "usern" :options ["hi" "do"]}]]
;;        {:username "bad"})

;; (fform [:post "/login"]
;;        [[:text :username {:class "usern"}]
;;         [:password :password {:class "pass"}]]
;;        {:username "bad"})

;; (pprint (html (form-fields [[:text :username {:class "usern"}]]
;;                            {:username "bad"})))



