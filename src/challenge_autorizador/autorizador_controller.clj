(ns challenge-autorizador.autorizador-controller
  (:gen-class)
  (:require [cheshire.core :as cheshire]
            [challenge-autorizador.logic.validator :as validator]
            [challenge-autorizador.db.atom.account :as db]))

(defn validate-limit
  [transaction]
  (when-not (validator/has-limit? transaction (get @db/atom-account :account))
    "insufficient-limit"))

(defn validate-card []
  (when-not (validator/active-card? (get @db/atom-account :account))
    "card-not-active"))

(defn validate-account []
  (when-not (validator/account-created? (get @db/atom-account :account))
    "account-not-initialized"))

(defn validate-more-than-three-transactions []
  (let [number-transactions-last-two-minutes (db/number-transactions-last-two-minutes)]
    (when-not (validator/within-transaction-limit? number-transactions-last-two-minutes)
      "high-frequency-small-interval")))

(defn validate-similar-transaction
  [transaction]
  (when (db/find-similar-transaction transaction)
    "doubled-transaction"))

(defn validate-transaction
  [{:keys [transaction]}]
  (or
    (validate-account)
    (validate-card)
    (validate-limit transaction)
    (validate-more-than-three-transactions)
    (validate-similar-transaction transaction)))

(defn process-account
  [account]
  (if (validator/account-created? (get @db/atom-account :account))
    (db/assoc-violations "account-already-initialized")
    (db/update-account account)))

(defn process-transaction
  [transaction]
  (if-let [violation (validate-transaction transaction)]
    (db/assoc-violations violation)
    (do
      (db/update-account-limit (get-in transaction [:transaction :amount]))
      (db/update-transactions transaction)))
  @db/atom-account)

(defn process-operation [input-map]
  (cond
    (contains? input-map :account) (process-account input-map)
    (contains? input-map :transaction) (process-transaction input-map)
    :else "Invalid operation!"))

(defn read-operation []
  (println "Enter an operation in json format. Type 'exit' to exit:")
  (let [input (read-line)]                                  ;; Lê a entrada do usuário
    (when (not (= input "exit"))                            ;; Se o usuário digitar "exit", retorna nil para encerrar
      (let [input-map (cheshire/parse-string input true)
            output-map (process-operation input-map)]
        (cheshire/generate-string output-map)))))

(defn -main []
  (let [result (read-operation)]
    (if result
      (do
        (println "Result:" result)
        (recur))                                            ;; Repete o loop
      (println "Program closed."))))                        ;; Quando o usuário digitar 'sair', encerra o programa