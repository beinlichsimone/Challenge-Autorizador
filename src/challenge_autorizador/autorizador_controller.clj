(ns challenge-autorizador.autorizador-controller
  (:require [cheshire.core :as cheshire]
            [challenge-autorizador.logic.transaction :as logic.transaction]
            [challenge-autorizador.logic.account :as logic.account]))

(defn update-acc
  [acc transaction]
  (-> acc
      (logic.account/update-account-limit (get-in transaction [:transaction :amount]))
      (update :transactions conj transaction)))

(defn process-transaction [acc transaction]
  (if-let [violations (seq (logic.transaction/validate-transaction acc transaction))]
    (update acc :violations concat violations)
    (update-acc acc transaction)))

(defn process-account [{:keys [account] :as acc} line]
  (if (:active-card account)
    (update acc :violations conj "account-already-initialized")
    (assoc acc :account (:account line))))

(defn process-output [updated-acc]
  (cheshire/generate-string (dissoc updated-acc :transactions)))

(defn process-operation
  [acc
   operation]
  (cond
    (:account operation) (process-account acc operation)
    (:transaction operation) (process-transaction acc operation)
    :else acc))

(defn process-line [acc line]
  (let [line-map (cheshire/parse-string line true)
        updated-acc (process-operation acc line-map)]
    (println (process-output updated-acc))
    updated-acc))

(defn read-lines-and-process []
  (reduce process-line {:account      {}
                        :violations   []
                        :transactions []}
          (take-while some? (repeatedly read-line))))

(defn -main []
  (println "Waiting input...")
  (read-lines-and-process))
