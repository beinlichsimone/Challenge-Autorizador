(ns challenge-autorizador.autorizador-controller
  (:gen-class)
  (:require [cheshire.core :as cheshire]
            [challenge-autorizador.logic.validator :as validator]
            [challenge-autorizador.db.atom.account :as db]))

(defn validate-limit
  [transaction]
  (if (validator/has-limit? transaction (get @db/atom-account :account))
    true
    (do (db/assoc-violations "insufficient-limit") false)))

(defn validate-card []
  (if (validator/active-card? (get @db/atom-account :account))
    true
    (do (db/assoc-violations "card-not-active") false)))

(defn validate-account []
  (if (validator/account-created? (get @db/atom-account :account))
    true
    (do (db/assoc-violations "account-not-initialized") false)))

(defn validate-more-than-three-transactions []
  (let [number-transactions-last-two-minutes (db/number-transactions-last-two-minutes)]
    (if (validator/within-transaction-limit? number-transactions-last-two-minutes)
      true
      (do (db/assoc-violations "high-frequency-small-interval") false))))

(defn validate-similar-transaction
  [transaction]
  (if (not (db/find-similar-transaction transaction))
    true
    (do (db/assoc-violations "doubled-transaction") false)))

(defn validate-transaction
  [{:keys [transaction]}]
  (and
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
  (if (validate-transaction transaction)
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