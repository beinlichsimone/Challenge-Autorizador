(ns challenge-autorizador.logic.transaction
  (:require [challenge-autorizador.logic.account :as logic.account]
            [challenge_autorizador.util.time-util :as time-util]))

(defn insufficient-limit?
  [{:keys [available-limit]}
   {:keys [amount]}]
  (> amount available-limit))

(defn number-transactions-last-two-minutes [transactions]
  (let [now (System/currentTimeMillis)]                     ;; Obtém o timestamp atual
    (->> transactions
         (filter #(> now (- (time-util/parse-time-to-timestamp (get-in % [:transaction :time])) 120000))) ;; Filtra as transações dos últimos 2 minutos
         (count))))

(defn more-than-three-transactions? [transactions]
  (let [number-transactions-last-two-minutes (number-transactions-last-two-minutes transactions)]
    (> number-transactions-last-two-minutes 3)))

(defn find-similar-transaction
  [transactions
   transaction]
  (let [amount (get transaction :amount)
        merchant (get transaction :merchant)
        now (System/currentTimeMillis)]                     ;; Obtém o timestamp atual
    (some #(and (= (:amount (:transaction %)) amount)
                (= (:merchant (:transaction %)) merchant)
                (> now (- (time-util/parse-time-to-timestamp (:time (:transaction %))) 120000))) ;; Verifica se a transação está dentro do intervalo de 2 minutos
          transactions)))

(defn similar-transaction?
  [transactions
   transaction]
  (find-similar-transaction transactions transaction))

(defn validate-transaction
  [contexto {:keys [transaction]}]
  (if (logic.account/invalid-account? (:account contexto))
    ["account-not-initialized"]
    (cond-> []
            (logic.account/invalid-account? (:account contexto)) (conj "account-not-initialized")
            (logic.account/inactive-card? (:account contexto)) (conj "card-not-active")
            (insufficient-limit? (:account contexto) transaction) (conj "insufficient-limit")
            (similar-transaction? (:transactions contexto) transaction) (conj "doubled-transaction")
            (more-than-three-transactions? (:transactions contexto)) (conj "high-frequency-small-interval"))))

