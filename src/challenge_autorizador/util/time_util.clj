(ns challenge_autorizador.util.time-util)

;; Função para converter uma string ISO 8601 em timestamp (milissegundos)
(defn parse-time-to-timestamp
  [time-str]
  (-> time-str
      java.time.Instant/parse
      .toEpochMilli))