(ns formula.core
  (:require [trust.escape :refer :all]
            [hiccup.util :refer [to-uri]]
            [hiccup.def :refer [defelem]]
            [hiccup.form :refer [hidden-field select-options]]))

(defn- input-field
  "For different input fields - escapes value"
  [type name attrs]
  [:input (conj {:type type
                 :name name
                 :id name
                 :value (escape-html (:value attrs))} attrs)])

(defn- display-error
  "Displays errors if map contains a message"
  [f-name field errors]
  (let [str-name (name f-name)]
    (if (f-name errors)
      [:div
       [:p {:class (str str-name "-error")}
        (f-name errors)] field]
      field)))

(defelem generic-input
  "Generic input builder used for
   text, password, email, checkbox, radio, hidden, file"
  [type f-name attrs & [errors]]
  (let [str-name (name f-name)
        field (input-field type str-name attrs)]
    (display-error f-name field errors)))

(defelem text-area
  "Creates text area element - escapes value"
  [_ f-name attrs & [errors]]
  (let [m-attrs (dissoc attrs :value)
        field [:textarea (conj {:name f-name
                                :id f-name} m-attrs)
               (escape-html (:value attrs))]]
    (display-error f-name field errors)))

(defelem button
  "Creates button element"
  [type f-name attrs & [errors]]
  (let [field [:input (conj {:type f-name
                             :value (:value attrs)} attrs)]]
    field))

(defelem drop-down
  "Creates a drop down using the <select> tag"
  [_ f-name attrs & [errors]]
  (let [m-attrs (dissoc attrs :selected :options)
        field [:select {:name f-name :id f-name}
               (select-options (:options attrs) (:selected attrs))]]
    (display-error f-name field errors)))

(def field-map
  "Functions for specific fields.
   - Options available but not include are
     text, password, email, checkbox, radio, hidden, file"
  {:textarea text-area :dropdown drop-down :button button})

(defelem form-fields
  "Calls field functions"
  [field-vec errors]
  (let [deal (fn [[field f-name attrs]]
               ((or (field field-map) generic-input) field f-name attrs errors))
        body (map deal field-vec)]
    body))

(defelem fform
  "Creates a form that points to a particular method and route.
   Also creates listed fields and displays errors"
  
  [[method action] field-vec & [errors]]
  
  (let [method-str (.toUpperCase (name method))
        action-uri (to-uri action)]
    
    (-> (if (contains? #{:get :post} method)
          [:form {:method method-str, :action action-uri}]
          [:form {:method "POST", :action action-uri}
           (hidden-field "_method" method-str)])
        (concat (form-fields field-vec errors))
        (vec))))



