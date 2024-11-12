(ns challenge-autorizador.logic-test
  (:require [clojure.test :refer :all]
            [challenge-autorizador.logic.validator :refer :all]))

(deftest active-card?-test
  (testing "Que o cartão está ativo"
    (is (active-card? {:active-card true, :available-limit 1000})))

  (testing "Que o cartão não está ativo"
   (is not (active-card? {:active-card false, :available-limit 1000})))

  (testing "Que o cartão não está ativo quando o mapa não possui a chave :active-card"
      (is not (active-card? {:available-limit 1000}))))

(deftest account-created?-test
  (testing "Que a conta foi criada"
    (is (account-created? {:active-card true, :available-limit 1000})))

  (testing "Que a conta não foi criada"
   (is not (account-created? {:account {}, :violations []})))

  (testing "Que a conta não foi criada quando o mapa estiver vazio"
      (is not (account-created? {}))))