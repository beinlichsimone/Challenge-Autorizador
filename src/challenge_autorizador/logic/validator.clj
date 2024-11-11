(ns challenge-autorizador.logic.validator)

(defn active-card?
  [account]
  (get account :active-card))

(defn has-limit?
  [{:keys [amount]}
   {:keys [available-limit]}]
  (>= available-limit amount))

(defn account-created?
  [account]
  (contains? account :available-limit))

(defn within-transaction-limit?
  [num-transactions]
  (< num-transactions 3))
