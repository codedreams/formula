(ns formula.validation)

(def user {:username "hello" :password "abc" :confirmation "abc"
           :gender nil})

(def call
  "Changes strings into function calls"
  #(resolve (symbol (name %))))

(defn message
  "Creates messages"
  [k custom message]
  (clojure.string/trim (or (when custom
                             (format custom (name k)))
                           (format message (name k)))))

(defn present
  "Checks if key is present"
  [field-key vali-map & [messages]]
  (let [custom (:present (field-key messages))]
    (when (false? (get vali-map field-key false))
      {field-key (message field-key custom "%s must be present")})))

(defn allow-nil
  "Checks to see if value is nil"
  [field-key answer vali-map & [messages]]
  (let [custom (:allow-nil (field-key messages))
        value (field-key vali-map)
        match (when-not answer (nil? value))]
    (when match
      {field-key (message field-key custom "%s can't be empty")})))

(defn allow-blank
  "Checks to see if value is blank"
  [field-key answer vali-map & [messages]]
  (let [custom (:allow-blank (field-key messages))
        value (field-key vali-map)
        match (when-not answer
                (= 0 (count (clojure.string/trim value))))]
    (when match
      {field-key (message field-key custom "%s can't be blank")})))

(defn confirm
  "Checks to see if values are the same"
  [field-key confirms vali-map & [messages]]
  (let [value (field-key vali-map)
        custom (:confirm (field-key messages))
        no-match (seq (remove #(= value (% vali-map)) confirms))]
    (when no-match
      {field-key (message field-key custom
                          (str "%s must match " (name (first no-match))))})))

(defn exclusion
  "Checks to see if value is not in coll"
  [field-key exclusions vali-map & [messages]]
  (let [value (field-key vali-map)
        custom (:exclusion (field-key messages))
        match (some #{value} exclusions)]
    (when match
      {field-key (message field-key custom
                          "%s is not an acceptable term")})))

(defn inclusion
  "Checks to see if value is in coll"
  [field-key inclusions vali-map & [messages]]
  (let [value (field-key vali-map)
        custom (:inclusion (field-key messages))
        match (some #{value} inclusions)]
    (when-not match
      {field-key (message field-key custom
                          "%s is not an acceptable term")})))

(defn formats
  "Checks to see if value is in correct format"
  [field-key regex vali-map & [messages]]
  (let [value (field-key vali-map)
        custom (:formats (field-key messages))
        match (re-find regex value)]
    (when-not match
      {field-key (message field-key custom
                          "%s is not the correct format")})))

(defn length
  "Checks to see if value is the correct length"
  [field-key length-map vali-map & [messages]]
  (let [min (length-map :min)
        max (length-map :max)
        value (field-key vali-map)
        custom (:length (field-key messages))
        result (cond
                (not-any? nil?
                          [min max]) (when-not (some #{(count value)}
                                                     (range min max))
                                       (str "%s should be between "
                                            min " and " max
                                            " characters"))
                          (nil? max) (when-not (>= (count value) min)
                                       (str "%s should be at least"
                                            min "characters"))
                          (nil? min) (when-not (<= (count value) max)
                                       (str "%s should be at most"
                                            max "characters")))]

    {field-key (message field-key custom result)}))

(length :username {:min 30 :max 17} user)


(defn unique
  "Checks to see if value is unique.  check-fn should be a function
   that takes the value as an argument."
  [field-key check-fn vali-map & [messages]])

(defn- sender-loop [field rules vali-map & [messages]]
  (let [vali #(if (map? %)
                ((call (apply key %)) field (apply val %) vali-map %2)
                ((call %) field vali-map %2))]
        (loop [rules rules errors {}]
          (if (or (empty? rules) (seq errors))
            errors
            (recur (rest rules)
                   (conj errors (vali (first rules) messages)))))))

(defn default-check
  "Checks to see if value is nil or blank."
  [field rules vali-map & [messages]]
  (let [allow-nil (some #{:allow-nil} rules)
        allow-blank (some #{:allow-blank} rules)
        rules [(first (filter #{:present} rules))
               {:allow-nil allow-nil}
               {:allow-blank allow-blank}]]
    (sender-loop field rules vali-map messages)))

;;; make automatic check for allow-nil and allow-blank
(defn sender 
  "Sends rules to correct functions. Once an validation error has
   occurred for a specific field, then the rest of the rules are
   ignored."
  [field rules vali-map & [messages]]
  (let [check (default-check field rules vali-map (field messages))
        no-errors (empty? check)
        rules (when no-errors
                (remove #{:allow-nil :allow-blank :present} rules))]
    (if no-errors
      (sender-loop field rules vali-map (field messages))
      check)))

;(sender :password [:present {:formats #"\d+"}] user {:username "bad"})


;;; make custom messages more customized, by including value in string
;;; add something to make to help customize, and then remove after conj
;;; idea - return key and vector, then another function to conj

;;; add two functions for allow-nil, and allow-blank in map
;;; should take allow-nil and allow-blank map last
(defn validate 
  "Takes rules vector, to be validated map, and custom messages (optional).
   Each field's rules are ran until an validation error occurs."
  [rule-vec vali-map & [messages]]
  (let [errors (for [[field & rules] rule-vec
                     :let [result (sender field rules vali-map messages)]]
                 result)
        ;errors (into {} errors)
        ]
    (into {} errors)
    ;(conj errors (select-keys messages (keys errors)))
    ))


(validate [[:password :present {:formats #"\d+"}]
           [:username :present]
           [:gender :present :allow-blank]
           [:confirmation :present {:confirm [:password]}]
           ;[:password :present {:length {:min 3 :max 5}}]
           ]
          user {:username {:present "bad"
                           :allow-nil "%s can't be nil"}})

;; {:password {:present "The password must be present"
;;             :format "The format is wrong for password"}}
