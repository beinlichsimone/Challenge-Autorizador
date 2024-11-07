(ns challenge-autorizador.teste
  (:gen-class)
  (:require [cheshire.core :as cheshire]))

(def conta (atom {:account {}, :violations []}))            ;; Inicializando com um mapa vazio

(def transacoes (atom []))

(defn mapa-para-json [m]
  (cheshire/generate-string m))

(defn json-para-mapa [m]
  (cheshire/parse-string m true))

(defn assoc-violations [violations]
  (swap! conta
         (fn [account]
           (if (empty? violations)
             account
             (update account :violations conj violations)))))

(defn atualiza-conta [input]
  (swap! conta update :account merge (get input :account))) ;; Atualiza corretamente o campo :account

(defn cartao-ativo? []
  (if (get-in @conta [:account :active-card])
    true
    (do (assoc-violations ["card-not-active"]) false)))     ;; Corrigido: passamos uma lista de violação

(defn tem-limite? [valor]
  (if (>= (get-in @conta [:account :available-limit]) valor)
    true
    (do (assoc-violations ["insufficient-limit"]) false)))

(defn conta-criada? []
  (if (contains? (:account @conta) :available-limit)
    true
    (do (assoc-violations ["account-not-initialized"]) false)))

;; Função para converter uma string ISO 8601 em timestamp (milissegundos)
(defn parse-time-to-timestamp [time-str]
  (-> time-str
      java.time.Instant/parse
      .toEpochMilli))

(defn numero-transacoes-ultimos-dois-minutos []
  (let [now (System/currentTimeMillis)]                     ;; Obtém o timestamp atual
    ;; Filtra transações que ocorreram nos últimos 2 minutos
    (count (filter #(> now (- (parse-time-to-timestamp (get-in % [:transaction :time])) 120000)) @transacoes))))

(defn mais-que-tres-transacoes? []
  (if (< (numero-transacoes-ultimos-dois-minutos) 3)
    true
    (do (assoc-violations ["high-frequency-small-interval" @transacoes]) false)))

(defn localiza-transacao-similar? [transaction]
  (let [amount (get-in transaction [:transaction :amount])
        merchant (get-in transaction [:transaction :merchant])
        now (System/currentTimeMillis)]                     ;; Obtém o timestamp atual
    (some #(and (= (get-in % [:transaction :amount]) amount)
                (= (get-in % [:transaction :merchant]) merchant)
                (> now (- (parse-time-to-timestamp (get-in % [:transaction :time])) 120000))) ;; Verifica se a transação está dentro do intervalo de 2 minutos
          @transacoes)))

(defn transacao-similar? [transaction]
  (if (not (localiza-transacao-similar? transaction))
          true
          (do (assoc-violations ["high-frequency-small-interval" @transacoes]) false)))

(defn valida-transacao [transaction]
  (and
    (conta-criada?)
    (cartao-ativo?)
    (tem-limite? (get-in transaction [:transaction :amount]))
    (mais-que-tres-transacoes?)
    (transacao-similar? transaction)))

(defn atualiza-limite-conta [valor]
  (swap! conta update-in [:account :available-limit] (fn [old-limit] (- old-limit valor))))

(defn atualiza-transacoes [transaction]
  (swap! transacoes conj transaction))

(defn processa-conta [account]
  (if (contains? (:account @conta) :available-limit)
    (do
      (mapa-para-json (assoc account :violations ["account-already-initialized"])))
    (do
      (atualiza-conta account)
      (mapa-para-json @conta))))

(defn processa-transacao [transaction]
  (str "Processed transaction: " @conta)
  (if (valida-transacao transaction)
    (do
      ;; Incrementa o valor de :amount na lista global
      (atualiza-limite-conta (get-in transaction [:transaction :amount]))
      (atualiza-transacoes transaction)
      (str "Processed transaction: " @conta))
    (str "Transaction not authorized: " @conta)))

(defn ler-operacao []
  (println "Digite uma operação no formato json. Digite 'sair' para encerrar:")
  (let [entrada (read-line)]                                ;; Lê a entrada do usuário
    (if (= entrada "sair")
      nil                                                   ;; Se o usuário digitar "sair", retorna nil para encerrar
      (let [mapa-entrada (json-para-mapa entrada)]
        (cond
          (contains? mapa-entrada :account) (processa-conta mapa-entrada)
          (contains? mapa-entrada :transaction) (processa-transacao mapa-entrada)
          :else "Operação inválida!")))))

(defn -main []
  (loop []
    (let [resultado (ler-operacao)]
      (if resultado
        (do
          (println "Resultado:" resultado)
          (recur))                                          ;; Repete o loop
        (println "Programa encerrado.")))))                 ;; Quando o usuário digitar 'sair', encerra o programa
