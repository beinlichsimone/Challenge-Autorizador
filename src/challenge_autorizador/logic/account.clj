(ns challenge-autorizador.logic.account)

(defn inactive-card? [account]
  (not (:active-card account)))

(defn invalid-account? [account]
  (not (contains? account :available-limit)))

(defn update-account-limit [acc valor]
  (update-in acc [:account :available-limit] (fn [old-limit] (- old-limit valor))))