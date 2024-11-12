(ns challenge-autorizador.db.atom.account
  (:require [challenge_autorizador.util.time-util :as time-util]))

(def atom-account (atom {:account {}, :violations []}))    ;; Inicializando com um mapa vazio
(def atom-transaction (atom []))

(defn assoc-violations [violations]
  (swap! atom-account update :violations conj violations))

(defn update-account
  [{:keys [account]}]
  (swap! atom-account update :account merge account))

(defn update-account-limit
  [valor]
  (swap! atom-account update-in [:account :available-limit] (fn [old-limit] (- old-limit valor))))

(defn update-transactions
  [transaction]
  (swap! atom-transaction conj transaction))

(defn number-transactions-last-two-minutes []
  (let [now (System/currentTimeMillis)]  ;; Obtém o timestamp atual
    (->> @atom-transaction  ;; Começa a trabalhar com a lista de transações
         (filter #(> now (- (time-util/parse-time-to-timestamp (get-in % [:transaction :time])) 120000)))  ;; Filtra as transações dos últimos 2 minutos
         (count))))  ;; Conta as transações

(defn find-similar-transaction
  [transaction]
  (let [amount (get transaction :amount)
        merchant (get transaction :merchant)
        now (System/currentTimeMillis)]                     ;; Obtém o timestamp atual
    (some #(and (= (:amount (:transaction %)) amount)
                (= (:merchant (:transaction %)) merchant)
                (> now (- (time-util/parse-time-to-timestamp (:time (:transaction %))) 120000))) ;; Verifica se a transação está dentro do intervalo de 2 minutos
          @atom-transaction)))