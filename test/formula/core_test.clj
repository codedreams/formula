(ns formula.core-test
  (:use clojure.test
        formula.core
        midje.sweet
        hiccup.core)
  (:require [midje.util :refer [testable-privates]]))

(testable-privates formula.core input-field)

(facts "input-field - returns a vector input with custom type name and attrs"
       (fact "should return vector with no value"
             (input-field "text" "username" {:class "user"})
             => [:input {:type "text" :name "username" :id "username"
                         :value nil :class "user"}])
       (fact "return vector with value attr"
             (input-field "password" "password" {:value "foobar"})
             => [:input {:type "password" :name "password" :id "password"
                         :value "foobar"}])
       (fact "return vector with extra attr, placeholder"
             (input-field "text" "search" {:placeholder "Search Now"})
             => [:input {:type "text" :name "search" :id "search"
                         :value nil :placeholder "Search Now"}])
       (fact "values should be escaped"
             (html (input-field "text" "search" {:value "<>" :id nil}))
             => "<input name=\"search\" type=\"text\" value=\"&lt;&gt;\" />")
       (fact "should create button tags"
             (input-field "button" "submit" {:name "" :value "Submit"})
             => [:input {:type "button" :name "" :id "submit" :value "Submit"}]))

(testable-privates formula.core display-error)

(facts "display error - should return field with error or just field"
       (fact "should just return field if no error message"
             (display-error :username [:input {:type "text" :name "username"
                                               :value ""}] {})
             => [:input {:type "text" :name "username" :value ""}])
       (fact "should return error and field if error"
             (display-error :username [:input {:type "text" :name "username"
                                               :value ""}]
                            {:username "username must be present"})
             => [:div 
                 [:input {:type "text" :name "username" :value ""}]
                 [:p {:class "username-error"} "username must be present"]])
       (fact "should not return error if error for another field"
             (display-error :username [:input {:type "text" :name "username"
                                               :value ""}]
                            {:name "name must be present"})
             => [:input {:type "text" :name "username" :value ""}])
       (fact "should wrap fields with specific tag"
             (display-error :username [:input {:type "text" :name "username"}]
                            {:username "bad" :wrap-fields :fieldset})
             => [:fieldset 
                 [:input {:type "text" :name "username"}]
                 [:p {:class "username-error"} "bad"]])
       
       (fact "should wrap errors with specific tag"
             (display-error :username [:input {:type "text" :name "username"}]
                            {:username "bad" :wrap-errors :span})
             => [:div 
                 [:input {:type "text" :name "username"}]
                 [:span {:class "username-error"} "bad"]]))

(facts "generic-input - should be used to produce text, password, email
                       checkbox, radio, hidden, and file fields"
      (fact "should create text field"
            (generic-input :text :search {})
            => [:input {:type :text :name "search" :id "search" :value nil}])
      (fact "should create password field"
            (generic-input :password :search {})
            => [:input {:type :password :name "search" :id "search" :value nil}])
      (fact "should create email field"
            (generic-input :email :search {})
            => [:input {:type :email :name "search" :id "search" :value nil}])
      (fact "should create checkbox field"
            (generic-input :checkbox :search {})
            => [:input {:type :checkbox :name "search" :id "search" :value nil}])
      (fact "should create radio field"
            (generic-input :radio :search {})
            => [:input {:type :radio :name "search" :id "search" :value nil}])
      (fact "should create hidden field"
            (generic-input :hidden :search {})
            => [:input {:type :hidden :name "search" :id "search" :value nil}])
      (fact "should create file field"
            (generic-input :file :search {})
            => [:input {:type :file :name "search" :id "search" :value nil}])
      (fact "should accept extra attributes"
            (generic-input :text :search {:placeholder "Search" :value "Anything"})
            => [:input {:type :text :name "search" :id "search" 
                        :placeholder "Search" :value "Anything"}])
      (fact "should display errors if available"
            (generic-input :text :search {} {:search "File must be jpg"})
            => [:div
                [:input {:type :text :name "search" :id "search" :value nil}]
                [:p {:class "search-error"} "File must be jpg"]])
      (fact "should wrap fields in specific tag"
            (generic-input :email :search {:wrap :h5})
            => [:h5 [:input {:type :email :name "search" :id "search" :value nil}]]
            ))

(facts "text-area - should produce a text area field"
       (fact "should create text area with no error"
             (text-area nil :problems {}) => [:textarea {:name :problems
                                                         :id :problems} ""])
       (fact "should create text area with errors"
             (text-area nil :help {} {:help "help must be present"})
             => [:div
                 [:textarea {:name :help :id :help} ""]
                 [:p {:class "help-error"} "help must be present"]
                 ])
       (fact "should accept extra attributes"
             (text-area nil :help {:placeholder "Questions Here"
                                   :value "<i>hello</i>"})
             => [:textarea {:name :help :id :help :placeholder "Questions Here"}
                 "&lt;i&gt;hello&lt;&#x2F;i&gt;"])
       (fact "should wrap text-area in specific tag"
             (text-area nil :problems {:wrap :span})
             => [:span [:textarea {:name :problems
                                   :id :problems} ""]]))

(facts "button - should return button element"
       (fact "should return button"
             (button nil :submit {}) => [:button {:type "button" :id :submit} nil])
       (fact "should accept extra attributes"
             (button nil :submit {:class "user button"}) =>
             [:button {:type "button" :id :submit :class "user button"} nil])
       (fact "should wrap if specified"
             (button nil :submit {:wrap :div}) =>
             [:div [:button {:type "button" :id :submit} nil]]))

(facts "drop-down - should return drop-down element"
      (fact "should return dropdown"
            (drop-down nil :friends {:options ["so" "say"]})
            => [:select {:name :friends :id :friends}
                '([:option {:selected false} "so"]
                   [:option {:selected false} "say"])])

      (fact "should return error if present"
            (drop-down nil :friends
                       {:options ["so" "say"]} {:friends "not an option"})
            => [:div [:select {:name :friends :id :friends}
                       '([:option {:selected false} "so"]
                           [:option {:selected false} "say"])]
                [:p {:class "friends-error"} "not an option"]])
      
      (fact "should return dropdown with select attribute"
            (drop-down nil :friends {:options ["so" "say"] :selected "say"})
            => [:select {:name :friends :id :friends}
                '([:option {:selected false} "so"]
                   [:option {:selected true} "say"])])

      (fact "should wrap dropdown if specified"
            (drop-down nil :friends {:options ["so" "say"] :selected "say"
                                     :wrap :span})
            => [:span [:select {:name :friends :id :friends}
                       '([:option {:selected false} "so"]
                           [:option {:selected true} "say"])]]))

(facts "field-map - should have correct functions"
       (fact "should have correct keys"
             (every? #{:textarea :dropdown :button-tag} (keys field-map)) => true)
       (fact "should have 3 key value pairs"
             (count field-map) => 3))

(facts "Form fields - should call field functions"
       (fact "should return fields"
             (form-fields [[:text :username {:class "user" :value "<i>"}]
                           [:password :password]]
                          {:username "must be present" :password "doesn't match"})
             => '([:div
                   [:input {:type :text :class "user" :value "<i>"
                            :id "username" :name "username"}]
                   [:p {:class "username-error"} "must be present"]]
                    [:div
                     [:input {:type :password :id "password"
                              :name "password" :value nil}]
                     [:p {:class "password-error"} "doesn't match"]])))

(facts "fform - should create forms"
       (fact "should create post form"
             (fform [:post "/login"]
                    [[:textarea :username {:class "usern" :value "<i>"}]
                     [:password :password {:class "pass"}]]
                    {:username "bad" :password "bad"})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:div
                  [:textarea {:class "usern" 
                              :id :username :name :username} "&lt;i&gt;"]
                  [:p {:class "username-error"} "bad"]]
                 [:div
                  [:input {:type :password :id "password" :class "pass"
                           :name "password" :value nil}]
                  [:p {:class "password-error"} "bad"]]])
       
       (fact "should create get form"
             (fform [:get "/login"]
                    [[:text :search {:value "<i>" :placeholder "Search"}]]
                    {:username "bad" :password "bad"})
             => [:form {:action (java.net.URI. "/login") :method "GET"}
                 [:input {:type :text :id "search" :name "search" :value "<i>"
                          :placeholder "Search"}]]))

(facts "fform - should not display error if :no-errors is present"
       (fact "shouldn't display errors"
             (fform [:post "/login"]
                    [[:textarea :username {:class "usern" :value "<i>"}]
                     [:password :password {:class "pass"}]]
                    {:no-errors true :username "bad" :password "pass"})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:textarea {:class "usern" 
                             :id :username :name :username} "&lt;i&gt;"]
                 [:input {:type :password :id "password" :class "pass"
                          :name "password" :value nil}]]))

(facts "fform - should return generic error message above
        all fields if :generic-error is present"
       (fact "should return one error above fields"
             (fform [:post "/login"]
                    [[:textarea :username {:class "usern" :value "<i>"}]
                     [:password :password {:class "pass"}]]
                    {:generic-error "wrong"})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:p {:class "generic-error"} "wrong"]
                 [:textarea {:class "usern" 
                             :id :username :name :username} "&lt;i&gt;"]
                 [:input {:type :password :id "password" :class "pass"
                          :name "password" :value nil}]]))


(facts "fform - should wrap errors in specific error tag"
       (fact "should wrap errors in specified tag"
             (fform [:post "/login"]
                    [[:textarea :username {}]
                     [:password :password {}]]
                    {:password "bad" :username "bad" :wrap-errors :span})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:div
                  [:textarea {:id :username :name :username} ""]
                  [:span {:class "username-error"} "bad"]]
                 [:div
                  [:input {:type :password :id "password" :name "password"
                           :value nil}]
                  [:span {:class "password-error"} "bad"]]]))

(facts "fform - should wrap fields in specific error tag"
       (fact "should wrap error and field in specific tag"
             (fform [:post "/login"]
                    [[:textarea :username {}]
                     [:password :password {}]]
                    {:password "bad" :username "bad" :wrap-errors :span
                     :wrap-fields :fieldset})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:fieldset
                  [:textarea {:id :username :name :username} ""]
                  [:span {:class "username-error"} "bad"]]
                 [:fieldset
                  [:input {:type :password :id "password" :name "password"
                           :value nil}]
                  [:span {:class "password-error"} "bad"]]]))

(facts "fform- should wrap field tags if wrap is present"
       (fact "should wrap fields if wrap is a rule"
             (fform [:post "/login"]
                    [[:textarea :username {:wrap :p}]
                     [:password :password {:wrap :p}]]
                    {:password "bad" :username "bad" :wrap-errors :span
                     :wrap-fields :fieldset})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:fieldset
                  [:p [:textarea {:id :username :name :username} ""]]
                  [:span {:class "username-error"} "bad"]]
                 [:fieldset
                  [:p [:input {:type :password :id "password" :name "password"
                               :value nil}]]
                  [:span {:class "password-error"} "bad"]]]))
