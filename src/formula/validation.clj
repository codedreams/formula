(ns formula.validation)

(def user {:username "" :password "abc" :confirmation "a"})

(def call
  "Changes strings into function calls"
  #(resolve (symbol (name %))))

(defn message
  "Creates messages"
  [k message & [other-name]]
  (let [other (when other-name (name (first other-name)))]
    (clojure.string/trim (str (name k) " " message " " other))))

(defn present
  "Checks if value is present"
  [field-key vali-map]
  (when (empty? (field-key vali-map))
    {field-key (message field-key "must be present")}))

(defn confirm
  "Checks to see if values are the same"
  [field-key confirms vali-map]
  (let [value (field-key vali-map)
        no-match (seq (remove #(= value (% vali-map)) confirms))]
    (when no-match
      {field-key (message field-key "must match" no-match)})))

(defn exclusion
  "Checks to see if value is not in coll"
  [field-key exclusions vali-map]
  (let [value (field-key vali-map)
        match (some #(= value %) exclusions)]
    (when match
      {field-key (message field-key
                          (str value " is not an acceptable term"))})))

(defn sender 
  "Sends rules to correct functions. Once an validation error has
   occurred for a specific field, then the rest of the rules are
   ignored."
  [field rules vali-map]
  (let [vali #(if (map? %)
                ((call (apply key %)) field (apply val %) vali-map)
                ((call %) field vali-map))]
    ;;; loops to stop execution once it has an error
    (loop [rules rules errors {}]
      (if (or (empty? rules) (seq errors))
        errors
        (recur (rest rules)
               (conj errors (vali (first rules))))))))

(defn validate 
  "Takes rules vector, to be validated map, and custom messages (optional).
   Each field's rules are ran until an validation error occurs.  Once all
   rules are ran, the messages get replaced with custom messages"
  [rule-vec vali-map & [messages]]
  (let [errors (for [[field & rules] rule-vec
                     :let [result (sender field rules vali-map)]
                     :when (contains? vali-map field)]
                 result)
        errors (into {} errors)]
    (conj errors (select-keys messages (keys errors)))))



(validate [[:password :present]
           [:username :present]
           [:confirmation :present {:confirm [:password]}]
           ;[:password :present {:length {:min 3 :max 5}}]
           ;[:confirmation :present {:confirm :password :present "what"}]
           ]
          user {:username "bad"})

