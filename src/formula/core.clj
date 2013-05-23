(ns formula.core
  (:require [trust.escape :refer [escape-html]]
            [hiccup.util :refer [to-uri]]
            [hiccup.def :refer [defelem]]
            [hiccup.form :refer [hidden-field select-options]]))

(defn- input-field
  "For different input fields"
  [type name attrs]
  (let [field-attrs (dissoc attrs :in-tag)
        field-attrs (conj {:type type :name name :id name :value nil} field-attrs)]
    (if (= type :checkbox)
      [:input field-attrs (:in-tag attrs)]
      [:input field-attrs])))

(defn- display-error
  "Displays errors if map contains a message"
  [attr-key field attrs errors]
  (let [str-name (name attr-key)
        error-tag (:error-tag errors)
        error-field [(keyword (str (name (or error-tag :span))
                                   "." str-name "-error"))
                     (attr-key errors)]
        wrap-error (:wrap-error errors)
        field-error (if wrap-error [wrap-error error-field] error-field)
        wrap-both (:wrap-both errors)
        label (when (:label attrs) [:label {:for str-name} (:label attrs)])
        error-result (if wrap-both
                       (vec (remove nil? [wrap-both label field field-error]))
                       (vec (remove nil? [:div label field field-error])))
        result (if wrap-both
                 (vec (remove nil? [wrap-both label field])) field)]
    (if (attr-key errors)
      (if (:no-errors errors) result error-result)
      result)))

(defelem generic-input
  "Generic input builder used for
   text, password, email, checkbox, radio, hidden, file"
  [type attr-key attrs & [errors]]
  (let [str-name (name attr-key)
        m-attrs (dissoc attrs :wrap :label)
        field (input-field type str-name m-attrs)
        field (if (:wrap attrs) [(:wrap attrs) field] field)
        result (display-error attr-key field attrs errors)]
    (if (:wrap-outer errors)
      [(errors :wrap-outer) result]
      result)))

;; (defelem label
;;   "Creates label tags for elements"
;;   [_ attr-key attrs & [errors]]
;;   (let [m-attrs (conj (dissoc attrs :value) {:for (name attr-key)})
;;         field [:label m-attrs (:value attrs)]]
;;     field))


(defelem text-area
  "Creates text area element - escapes value"
  [_ attr-key attrs & [errors]]
  (let [m-attrs (dissoc attrs :value :wrap :label)
        field [:textarea (conj {:name attr-key
                                :id attr-key} m-attrs)
               (escape-html (:value attrs))]
        field (if (:wrap attrs) [(:wrap attrs) field] field)]
    (display-error attr-key field attrs errors)))

(defelem button
  "Creates button element"
  [_ attr-key attrs & [errors]]
  (let [m-attrs (dissoc attrs :value :wrap :label)
        field [:button (conj {:type "button" :id attr-key} m-attrs)
               (:value attrs)]
        field (if (:wrap attrs) [(:wrap attrs) field] field)]
    field))

(defelem drop-down
  "Creates a drop down using the <select> tag"
  [_ attr-key attrs & [errors]]
  (let [m-attrs (dissoc attrs :selected :options :wrap :label)
        field [:select {:name attr-key :id attr-key}
               (select-options (:options attrs) (:selected attrs))]
        field (if (:wrap attrs) [(:wrap attrs) field] field)]
    (display-error attr-key field attrs errors)))

(def field-map
  "Functions for specific fields.
   - Options available but not include are
     text, password, email, checkbox, radio, hidden, file"
  {:textarea text-area :dropdown drop-down :button-tag button})

(defelem form-fields
  "Calls field functions"
  [field-vec errors]
  (let [deal (fn [[field attr-key attrs]]
               ((or (field field-map) generic-input) field attr-key attrs errors))
        body (map deal field-vec)
        wrap-all (:wrap-all errors)
        body (if wrap-all (seq [(into [wrap-all] body)]) body)]
    (if (:generic-error errors)
      (concat (seq [[:p {:class "generic-error"} (:generic-error errors)]])
              body)
      body)))

(defelem fform
  "Creates a form that points to a particular method and route.
   Also creates listed fields and displays errors"
  
  [[method action] field-vec & [errors]]
  
  (let [method-str (.toUpperCase (name method))
        action-uri (to-uri action)
        csrf (:csrf errors)]
    
    (-> (if (contains? #{:get :post} method)
          [:form {:method method-str, :action action-uri}
           csrf]
          [:form {:method "POST", :action action-uri}
           (hidden-field "_method" method-str)
           csrf])
        (concat (form-fields field-vec errors))
        (vec))))



